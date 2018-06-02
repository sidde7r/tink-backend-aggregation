package se.tink.backend.aggregationcontroller.v1.rpc.system.update;

public class UpdateDocumentResponse {
    private String documentIdentifier;
    private String token;
    private String fullUrl;
    private boolean successfullyStored;

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

    public void setSuccessfullyStored(boolean successfullyStored) {
        this.successfullyStored = successfullyStored;
    }
}
