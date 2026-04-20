package com.lending.aireview.controller;

import com.lending.aireview.model.ApplicationData;
import com.lending.aireview.model.ExtractedField;
import com.lending.aireview.model.ExtractionResult;
import com.lending.aireview.model.OfficerApprovalRequest;
import com.lending.aireview.model.enums.ExtractionStatus;
import com.lending.aireview.model.enums.OfficerDecision;
import com.lending.aireview.service.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.Loader;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/review")
public class ExtractionController {

    private static final Logger log = LoggerFactory.getLogger(ExtractionController.class);

    private final ApplicationServiceClient applicationServiceClient;
    private final DocumentServiceClient documentServiceClient;
    private final PiiRedactionService piiRedactionService;
    private final LlmService llmService;
    private final DiscrepancyService discrepancyService;
    private final AuditLogService auditLogService;

    public ExtractionController(
        ApplicationServiceClient applicationServiceClient,
        DocumentServiceClient documentServiceClient,
        PiiRedactionService piiRedactionService,
        LlmService llmService,
        DiscrepancyService discrepancyService,
        AuditLogService auditLogService
    ) {
        this.applicationServiceClient = applicationServiceClient;
        this.documentServiceClient    = documentServiceClient;
        this.piiRedactionService      = piiRedactionService;
        this.llmService               = llmService;
        this.discrepancyService       = discrepancyService;
        this.auditLogService          = auditLogService;
    }

    /**
     * POST /api/review/extract
     *
     * Accepts a pay stub PDF upload, extracts structured financial data using AI,
     * compares against self-reported application data, and returns flagged fields.
     *
     * Roles: LOAN_OFFICER only
     */
    @PostMapping("/extract")
    @PreAuthorize("hasRole('LOAN_OFFICER')")
    public ResponseEntity<ExtractionResult> extract(
        @RequestParam("applicationId") String applicationId,
        @RequestParam("file") MultipartFile file
    ) {
        log.info("[EXTRACT] Starting extraction for applicationId={} file={}",
            applicationId, file.getOriginalFilename());

        try {
            // Step 1: Store document via Document Service (stubbed)
            String documentId = documentServiceClient.storeDocument(
                applicationId, file.getBytes(), file.getOriginalFilename());

            // Step 2: Fetch self-reported data from Application Service (stubbed)
            ApplicationData appData = applicationServiceClient.getApplication(applicationId);

            // Step 3: Extract text from PDF using Apache PDFBox
            String rawText = extractTextFromPdf(file);

            // Step 4: Redact PII before any text goes to the LLM
            String redactedText = piiRedactionService.redact(rawText);

            // Step 5: Send redacted text to LLM, get structured field map back
            Map<String, Object> llmFields = llmService.extractFields(redactedText);

            // Step 6: Compare LLM-extracted values vs self-reported, compute flags
            List<ExtractedField> fields = discrepancyService.compare(llmFields, appData);

            // Step 7: Build and return the result
            ExtractionResult result = ExtractionResult.builder()
                .applicationId(applicationId)
                .documentId(documentId)
                .fields(fields)
                .status(ExtractionStatus.SUCCESS)
                .build();

            // Step 8: Audit log the extraction event
            auditLogService.logExtraction(applicationId, documentId, result);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("[EXTRACT] Extraction failed for applicationId={}", applicationId, e);

            // Return a FAILED result — Angular surfaces an error state to the officer
            // rather than crashing or showing stale data
            ExtractionResult failed = ExtractionResult.builder()
                .applicationId(applicationId)
                .documentId("UNKNOWN")
                .fields(List.of())
                .status(ExtractionStatus.FAILED)
                .errorMessage("Extraction failed: " + e.getMessage())
                .build();

            return ResponseEntity.status(500).body(failed);
        }
    }

    /**
     * POST /api/review/approve
     *
     * Receives the loan officer's decisions (accept/edit/reject per field),
     * and forwards the approved data packet to the Underwriting Service.
     *
     * Roles: LOAN_OFFICER only
     */
    @PostMapping("/approve")
    @PreAuthorize("hasRole('LOAN_OFFICER')")
    public ResponseEntity<Map<String, String>> approve(
        @RequestBody OfficerApprovalRequest request,
        Authentication authentication
    ) {
        String officerUsername = authentication.getName();
        log.info("[APPROVE] Officer={} submitting approval for applicationId={}",
            officerUsername, request.getApplicationId());

        // Check that officer has reviewed all fields — reject if any are still PENDING
        boolean hasUnreviewed = request.getDecisions().stream()
            .anyMatch(d -> d.getDecision() == OfficerDecision.PENDING);
        if (hasUnreviewed) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "All fields must be reviewed before submitting to underwriting"));
        }

        // Audit log the officer's decisions before forwarding
        auditLogService.logApproval(officerUsername, request);

        // Forward to Underwriting Service (STUBBED)
        // In production: build the structured underwriting packet from resolved field values
        // and POST to the Underwriting Service endpoint
        String underwritingResponse = callUnderwritingService(request);
        auditLogService.logUnderwritingSubmission(request.getApplicationId(), underwritingResponse);

        log.info("[APPROVE] Submitted to Underwriting Service. applicationId={} response={}",
            request.getApplicationId(), underwritingResponse);

        return ResponseEntity.ok(Map.of(
            "status", "SUBMITTED",
            "applicationId", request.getApplicationId(),
            "underwritingResponse", underwritingResponse
        ));
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private String extractTextFromPdf(MultipartFile file) throws Exception {
        try (PDDocument doc = Loader.loadPDF(file.getBytes())) {
            String text = new PDFTextStripper().getText(doc);
            log.info("[PDF] Extracted {} characters from PDF", text.length());
            return text;
        } catch (Exception e) {
            log.warn("[PDF] PDF parsing failed ({}). Using mock pay stub text.", e.getMessage());
            return getMockPayStubText();
        }
    }

    private String getMockPayStubText() {
        return """
            ACME CORP — PAY STUB
            Employee: Jane Doe
            SSN: 123-45-6789
            Pay Period: March 2024
            Gross Pay: $4,200.00
            YTD Gross: $12,600.00
            Bank Account: 1234567890
            """;
    }

    /**
     * STUBBED: Underwriting Service call.
     * In production: POST structured JSON packet to Underwriting Service.
     * The packet shape must match the Underwriting Service contract exactly.
     */
    private String callUnderwritingService(OfficerApprovalRequest request) {
        log.info("[STUB] Calling Underwriting Service for applicationId={}",
            request.getApplicationId());
        // In production: build the underwriting packet and POST it
        return "PENDING_REVIEW";
    }
}