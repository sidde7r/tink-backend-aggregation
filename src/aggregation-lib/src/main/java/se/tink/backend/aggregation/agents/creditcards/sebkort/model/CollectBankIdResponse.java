package se.tink.backend.aggregation.agents.creditcards.sebkort.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CollectBankIdResponse {
    private String progressStatus;
    private String completeUrl;
    private ErrorEntity error;

    public String getProgressStatus() {
        return progressStatus;
    }

    public void setProgressStatus(String progressStatus) {
        this.progressStatus = progressStatus;
    }

    public String getCompleteUrl() {
        return completeUrl;
    }

    public void setCompleteUrl(String completeUrl) {
        this.completeUrl = completeUrl;
    }

    public ErrorEntity getError() {
        return error;
    }

    public void setError(ErrorEntity error) {
        this.error = error;
    }
}
