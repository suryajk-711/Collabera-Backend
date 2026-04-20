package com.lending.aireview.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Client for the existing Document Service.
 *
 * STUBBED: In production, this would:
 * 1. Call Document Service POST /documents to store the file
 * 2. Document Service uploads to S3 and returns a documentId + S3 presigned URL
 * 3. On retrieval, call GET /documents/{documentId} to get the presigned URL,
 *    then fetch the PDF bytes from S3
 */
@Service
public class DocumentServiceClient {

    private static final Logger log = LoggerFactory.getLogger(DocumentServiceClient.class);

    /**
     * Pretends to upload a document and returns a generated document ID.
     * In production: POST to Document Service with file bytes, get back documentId.
     */
    public String storeDocument(String applicationId, byte[] fileBytes, String fileName) {
        log.info("[STUB] Storing document for applicationId={} fileName={} size={}bytes",
            applicationId, fileName, fileBytes.length);

        // Mock: generate a deterministic doc ID
        // In production: return the documentId from Document Service response
        return "DOC-" + applicationId + "-" + System.currentTimeMillis();
    }

    /**
     * Pretends to fetch a document from S3 via Document Service.
     * In production: GET /documents/{documentId} => presigned S3 URL => fetch bytes.
     */
    public byte[] getDocument(String documentId) {
        log.info("[STUB] Fetching document documentId={} from Document Service", documentId);

        // Mock: return empty bytes (we use the uploaded file directly in the controller)
        // In production: fetch real PDF bytes from S3 presigned URL
        return new byte[0];
    }
}