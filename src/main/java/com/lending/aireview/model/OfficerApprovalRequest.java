package com.lending.aireview.model;

import com.lending.aireview.model.enums.OfficerDecision;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Payload sent by Angular when the officer submits their review.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfficerApprovalRequest {

    private String applicationId;
    private String documentId;

    /** One entry per field the officer has reviewed */
    private List<FieldDecision> decisions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldDecision {
        private String fieldKey;
        private OfficerDecision decision;
        /** Non-null only when decision = EDITED */
        private String officerOverrideValue;
    }
}