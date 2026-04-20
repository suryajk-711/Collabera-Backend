package com.lending.aireview.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Self-reported data from the Application Service.
 * In production this would be fetched via REST from the Application Service.
 * Here it is mocked.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationData {
    private String applicationId;
    private String applicantName;
    private String selfReportedEmployer;
    private double selfReportedMonthlyIncome;
    private String selfReportedPayPeriod;   // e.g. "2024-03"
}