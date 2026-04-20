package com.lending.aireview.model;

import com.lending.aireview.model.enums.DiscrepancyFlag;
import com.lending.aireview.model.enums.OfficerDecision;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtractedField {

    /** Human-readable label: "Monthly Income", "Employer", etc. */
    private String fieldName;

    /** Internal key used by downstream systems: "monthlyIncome", "employer" */
    private String fieldKey;

    /** Value the AI extracted from the pay stub */
    private String aiExtractedValue;

    /** Value the applicant self-reported on their loan application */
    private String selfReportedValue;

    /** MATCH | MISMATCH | MISSING */
    private DiscrepancyFlag flag;

    /**
     * Confidence score from the LLM (0.0 - 1.0).
     * Mocked here. In production, derived from LLM logprobs or a secondary
     * verification prompt.
     */
    private double confidenceScore;

    /** PENDING until the loan officer acts on this field */
    @Builder.Default
    private OfficerDecision decision = OfficerDecision.PENDING;

    /**
     * Only set when officer chooses EDITED — their corrected value
     * replaces aiExtractedValue in the underwriting packet.
     */
    private String officerOverrideValue;

    /**
     * Returns the value that should flow to underwriting:
     * officer's override if edited, otherwise the AI extracted value.
     */
    public String getResolvedValue() {
        if (decision == OfficerDecision.EDITED && officerOverrideValue != null) {
            return officerOverrideValue;
        }
        return aiExtractedValue;
    }
}