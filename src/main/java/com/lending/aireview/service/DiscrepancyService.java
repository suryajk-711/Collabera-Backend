package com.lending.aireview.service;

import com.lending.aireview.model.ApplicationData;
import com.lending.aireview.model.ExtractedField;
import com.lending.aireview.model.enums.DiscrepancyFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Compares LLM-extracted values against applicant self-reported values
 * and assigns a DiscrepancyFlag to each field.
 */
@Service
public class DiscrepancyService {

    private static final Logger log = LoggerFactory.getLogger(DiscrepancyService.class);

    /**
     * Income tolerance: flag mismatch only if the difference exceeds 5%.
     * This avoids flagging rounding differences (e.g., $5,999.50 vs $6,000).
     * The threshold should be configurable in production (application.properties).
     */
    private static final double INCOME_TOLERANCE_PERCENT = 0.05;

    public List<ExtractedField> compare(Map<String, Object> llmFields, ApplicationData appData) {
        List<ExtractedField> result = new ArrayList<>();

        // --- Employer ---
        String aiEmployer = getString(llmFields, "employer");
        String reportedEmployer = appData.getSelfReportedEmployer();
        result.add(ExtractedField.builder()
            .fieldName("Employer")
            .fieldKey("employer")
            .aiExtractedValue(aiEmployer)
            .selfReportedValue(reportedEmployer)
            .flag(compareStrings(aiEmployer, reportedEmployer))
            .confidenceScore(0.97)
            .build());

        // --- Monthly Income ---
        double aiIncome = getDouble(llmFields, "monthlyIncome");
        double reportedIncome = appData.getSelfReportedMonthlyIncome();
        result.add(ExtractedField.builder()
            .fieldName("Monthly Income")
            .fieldKey("monthlyIncome")
            .aiExtractedValue(String.valueOf(aiIncome))
            .selfReportedValue(String.valueOf(reportedIncome))
            .flag(compareIncome(aiIncome, reportedIncome))
            .confidenceScore(0.95)
            .build());

        // --- Pay Period ---
        String aiPayPeriod = getString(llmFields, "payPeriod");
        String reportedPayPeriod = appData.getSelfReportedPayPeriod();
        result.add(ExtractedField.builder()
            .fieldName("Pay Period")
            .fieldKey("payPeriod")
            .aiExtractedValue(aiPayPeriod)
            .selfReportedValue(reportedPayPeriod)
            .flag(compareStrings(aiPayPeriod, reportedPayPeriod))
            .confidenceScore(0.92)
            .build());

        // --- YTD Total (applicant doesn't self-report this — always MISSING on self-reported side) ---
        double aiYtd = getDouble(llmFields, "ytdTotal");
        result.add(ExtractedField.builder()
            .fieldName("YTD Total")
            .fieldKey("ytdTotal")
            .aiExtractedValue(String.valueOf(aiYtd))
            .selfReportedValue(null)
            .flag(DiscrepancyFlag.MISSING)   // expected — applicants don't self-report YTD
            .confidenceScore(0.90)
            .build());

        long mismatchCount = result.stream()
            .filter(f -> f.getFlag() == DiscrepancyFlag.MISMATCH).count();
        log.info("[DISCREPANCY] Comparison complete. Fields={} Mismatches={}",
            result.size(), mismatchCount);

        return result;
    }

    private DiscrepancyFlag compareStrings(String ai, String reported) {
        if (ai == null || reported == null) return DiscrepancyFlag.MISSING;
        return ai.trim().equalsIgnoreCase(reported.trim())
            ? DiscrepancyFlag.MATCH
            : DiscrepancyFlag.MISMATCH;
    }

    private DiscrepancyFlag compareIncome(double ai, double reported) {
        if (ai == 0 || reported == 0) return DiscrepancyFlag.MISSING;
        double diff = Math.abs(ai - reported) / reported;
        return diff <= INCOME_TOLERANCE_PERCENT
            ? DiscrepancyFlag.MATCH
            : DiscrepancyFlag.MISMATCH;
    }

    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }

    private double getDouble(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return 0.0;
        // Guard against LLM returning "4,200.00" (string with comma) instead of 4200.00
        // This is a real LLM failure mode — always sanitize numeric fields
        if (val instanceof Number) return ((Number) val).doubleValue();
        try {
            return Double.parseDouble(val.toString().replaceAll("[,$]", ""));
        } catch (NumberFormatException e) {
            log.warn("[DISCREPANCY] Could not parse numeric field '{}' value='{}'. Defaulting to 0.", key, val);
            return 0.0;
        }
    }
}