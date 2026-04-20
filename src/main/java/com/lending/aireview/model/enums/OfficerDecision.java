package com.lending.aireview.model.enums;

public enum OfficerDecision {
    PENDING,    // Officer hasn't reviewed this field yet
    ACCEPTED,   // Officer confirmed the AI-extracted value is correct
    EDITED,     // Officer overrode the AI value with their own
    REJECTED    // Officer rejected this field entirely (flag for manual review)
}