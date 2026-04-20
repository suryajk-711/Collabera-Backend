package com.lending.aireview.service;

import com.lending.aireview.model.ExtractionResult;
import com.lending.aireview.model.OfficerApprovalRequest;
import com.lending.aireview.model.enums.DiscrepancyFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Records audit events for all AI extractions and officer decisions.
 *
 * STUBBED: Currently logs to SLF4J (console/file).
 *
 * In production for a regulated financial environment:
 *  - Write to an immutable audit table in PostgreSQL (append-only, no UPDATE/DELETE)
 *  - Include: timestamp, userId, applicationId, documentId, action, before/after values
 *  - Ship logs to SIEM (e.g., Splunk, AWS CloudTrail) for compliance reporting
 *  - Retain per regulatory requirement (e.g., 7 years for mortgage records under RESPA)
 */
@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    public void logExtraction(String applicationId, String documentId, ExtractionResult result) {
        long mismatches = result.getFields().stream()
            .filter(f -> f.getFlag() == DiscrepancyFlag.MISMATCH)
            .count();

        log.info("[AUDIT] EXTRACTION | applicationId={} documentId={} status={} fields={} mismatches={} extractedAt={}",
            applicationId,
            documentId,
            result.getStatus(),
            result.getFields().size(),
            mismatches,
            result.getExtractedAt());
    }

    public void logApproval(String officerUsername, OfficerApprovalRequest request) {
        log.info("[AUDIT] APPROVAL | officer={} applicationId={} documentId={} decisions={}",
            officerUsername,
            request.getApplicationId(),
            request.getDocumentId(),
            request.getDecisions());
    }

    public void logUnderwritingSubmission(String applicationId, String underwritingResponse) {
        log.info("[AUDIT] UNDERWRITING_SUBMIT | applicationId={} response={}",
            applicationId,
            underwritingResponse);
    }
}