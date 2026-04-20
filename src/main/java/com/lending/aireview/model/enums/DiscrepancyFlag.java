package com.lending.aireview.model.enums;

public enum DiscrepancyFlag {
    MATCH,      // AI value matches self-reported value (within tolerance)
    MISMATCH,   // AI value differs from self-reported value — needs officer review
    MISSING     // Field exists in pay stub but applicant didn't report it (or vice versa)
}