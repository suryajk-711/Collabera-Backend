package com.lending.aireview.service;

import com.lending.aireview.model.ApplicationData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Client for the existing Application Service.
 *
 * STUBBED: In production, this would make a REST call to the Application Service
 * (e.g., GET /applications/{applicationId}) using a WebClient or RestTemplate
 * with service-to-service auth (e.g., mTLS or internal OAuth2 client credentials).
 */
@Service
public class ApplicationServiceClient {

    private static final Logger log = LoggerFactory.getLogger(ApplicationServiceClient.class);

    public ApplicationData getApplication(String applicationId) {
        log.info("[STUB] Calling Application Service for applicationId={}", applicationId);

        // Mock: return hardcoded self-reported data
        // In production: webclient.get().uri("/applications/{id}", applicationId)...
        return ApplicationData.builder()
            .applicationId(applicationId)
            .applicantName("Jane Doe")
            .selfReportedEmployer("Acme Corp")
            .selfReportedMonthlyIncome(6000.00)
            .selfReportedPayPeriod("2024-03")
            .build();
    }
}