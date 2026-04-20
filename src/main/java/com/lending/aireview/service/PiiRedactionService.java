package com.lending.aireview.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * Redacts PII from extracted PDF text before it is sent to any external LLM API.
 *
 * TRADE-OFFS of this regex approach (document in Section A):
 *  PRO:  Zero external calls, no latency, fully deterministic, no data leaves the service
 *  CON:  Regex misses non-standard formats (e.g., SSN with spaces "123 45 6789"),
 *        cannot redact PII that appears as a narrative ("John's account ending 4532"),
 *        and requires manual pattern maintenance.
 *
 * PRODUCTION UPGRADE: Replace or augment with AWS Comprehend (DetectPiiEntities)
 * or Microsoft Presidio — both purpose-built for financial document PII detection.
 */
@Service
public class PiiRedactionService {

    private static final Logger log = LoggerFactory.getLogger(PiiRedactionService.class);

    // SSN: 123-45-6789 or 123 45 6789
    private static final Pattern SSN_PATTERN =
        Pattern.compile("\\b\\d{3}[-\\s]\\d{2}[-\\s]\\d{4}\\b");

    // Date of birth: MM/DD/YYYY or MM-DD-YYYY
    private static final Pattern DOB_PATTERN =
        Pattern.compile("\\b(0[1-9]|1[0-2])[/\\-](0[1-9]|[12]\\d|3[01])[/\\-](19|20)\\d{2}\\b");

    // Bank/account numbers: 8-17 consecutive digits
    private static final Pattern ACCOUNT_NUMBER_PATTERN =
        Pattern.compile("\\b\\d{8,17}\\b");

    // Routing numbers: exactly 9 digits (ABA format)
    private static final Pattern ROUTING_NUMBER_PATTERN =
        Pattern.compile("\\b\\d{9}\\b");

    public String redact(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return rawText;
        }

        String redacted = rawText;
        redacted = SSN_PATTERN.matcher(redacted).replaceAll("[SSN-REDACTED]");
        redacted = DOB_PATTERN.matcher(redacted).replaceAll("[DOB-REDACTED]");
        redacted = ACCOUNT_NUMBER_PATTERN.matcher(redacted).replaceAll("[ACCOUNT-REDACTED]");
        redacted = ROUTING_NUMBER_PATTERN.matcher(redacted).replaceAll("[ROUTING-REDACTED]");

        log.info("[PII] Redaction complete. Original length={} Redacted length={}",
            rawText.length(), redacted.length());

        return redacted;
    }
}