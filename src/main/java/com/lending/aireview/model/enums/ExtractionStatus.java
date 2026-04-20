package com.lending.aireview.model.enums;

public enum ExtractionStatus {
    SUCCESS,    // All fields extracted and compared successfully
    PARTIAL,    // Some fields extracted, others failed (e.g., LLM couldn't parse a field)
    FAILED      // Extraction failed entirely — LLM error, bad PDF, etc.
}