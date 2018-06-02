package se.tink.backend.aggregation.aggregationcontroller.v1.rpc;

import java.util.UUID;

public class UpdateDocumentResponse {
    private String documentIdentifier;
    private String token;
    private String fullUrl;
    private boolean successfullyStored;

    public static UpdateDocumentResponse createUnSuccessful() {

        UpdateDocumentResponse response = new UpdateDocumentResponse();
        response.successfullyStored = false;

        return response;
    }

    public static UpdateDocumentResponse createSuccessful(String documentIdentifier, UUID token, String fullUrl) {

        UpdateDocumentResponse response = new UpdateDocumentResponse();
        response.successfullyStored = true;
        response.documentIdentifier = documentIdentifier;
        response.token = token.toString();
        response.fullUrl = fullUrl;

        return response;
    }

    public String getDocumentIdentifier() {
        return documentIdentifier;
    }

    public void setDocumentIdentifier(String documentIdentifier) {
        this.documentIdentifier = documentIdentifier;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getFullUrl() {
        return fullUrl;
    }

    public void setFullUrl(String fullUrl) {
        this.fullUrl = fullUrl;
    }

    public boolean isSuccessfullyStored() {
        return successfullyStored;
    }
}
