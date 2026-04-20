package com.lending.aireview.model;

import com.lending.aireview.model.enums.ExtractionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtractionResult {

    private String applicationId;
    private String documentId;

    /** All extracted fields with comparison flags */
    private List<ExtractedField> fields;

    /** Overall extraction status */
    private ExtractionStatus status;

    /**
     * Error message populated only when status = FAILED or PARTIAL.
     * Surfaced in Angular so the officer knows what went wrong.
     */
    private String errorMessage;

    /** ISO-8601 timestamp of when extraction ran */
    @Builder.Default
    private String extractedAt = Instant.now().toString();
}