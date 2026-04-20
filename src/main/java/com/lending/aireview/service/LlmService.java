package com.lending.aireview.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Sends redacted pay stub text to the LLM API and parses the structured response.
 *
 * STUBBED: The actual HTTP call to OpenAI/Anthropic is replaced with a mock response.
 *
 * In production:
 *  - Use OpenAI with response_format: { type: "json_object" } to enforce JSON output
 *  - Or use Anthropic tool_use to enforce a strict response schema
 *  - Wrap the call in a retry with exponential backoff (transient LLM failures are common)
 *  - Set a timeout (LLM calls can take 5 - 15s for long documents)
 *  - Log prompt tokens and cost per extraction for observability
 */
@Service
public class LlmService {

    private static final Logger log = LoggerFactory.getLogger(LlmService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Builds the prompt and calls the LLM.
     * Returns a parsed map of field name => extracted value.
     *
     * Expected LLM response shape:
     * {
     *   "employer": "Acme Corp",
     *   "monthlyIncome": 4200.00,
     *   "payPeriod": "2024-03",
     *   "ytdTotal": 12600.00
     * }
     *
     * IMPORTANT: monthlyIncome and ytdTotal must be numbers, not strings.
     * The LLM sometimes returns "4,200.00" (string with comma) — always validate
     * and parse the response schema before using downstream. This is a real failure
     * mode; see Section B2 of the written doc.
     */
    public Map<String, Object> extractFields(String redactedPayStubText) {
        String prompt = buildPrompt(redactedPayStubText);
        log.info("[LLM] Sending prompt to LLM API. Text length={}", redactedPayStubText.length());

        // --- STUBBED LLM CALL ---
        // In production, replace this block with:
        //
        // HttpClient client = HttpClient.newHttpClient();
        // String requestBody = """
        //     {
        //       "model": "gpt-4o",
        //       "response_format": { "type": "json_object" },
        //       "messages": [{ "role": "user", "content": "%s" }]
        //     }
        //     """.formatted(prompt.replace("\"", "\\\""));
        //
        // HttpRequest request = HttpRequest.newBuilder()
        //     .uri(URI.create("https://api.openai.com/v1/chat/completions"))
        //     .header("Authorization", "Bearer " + apiKey)
        //     .header("Content-Type", "application/json")
        //     .POST(HttpRequest.BodyPublishers.ofString(requestBody))
        //     .build();
        //
        // HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        // Then parse response.body() => data.choices[0].message.content => JSON map

        return getMockLlmResponse();
    }

    private String buildPrompt(String redactedText) {
        return """
            You are a financial document parser. Extract information from the pay stub below.
            
            Return ONLY a valid JSON object matching this exact schema. No explanation, no markdown.
            Schema:
            {
              "employer": <string — exact employer name>,
              "monthlyIncome": <number — gross monthly income in dollars, no commas>,
              "payPeriod": <string — in format YYYY-MM>,
              "ytdTotal": <number — year-to-date gross total in dollars, no commas>
            }
            
            If a field cannot be found, use null for its value.
            
            Pay stub text:
            %s
            """.formatted(redactedText);
    }

    /**
     * Mock LLM response simulating a discrepancy scenario:
     * Applicant claimed $6,000/month. Pay stub shows $4,200/month.
     */
    private Map<String, Object> getMockLlmResponse() {
        log.info("[LLM] Returning mock response (stub)");
        return Map.of(
            "employer",      "Acme Corp",
            "monthlyIncome", 4200.00,
            "payPeriod",     "2024-03",
            "ytdTotal",      12600.00
        );
    }
}