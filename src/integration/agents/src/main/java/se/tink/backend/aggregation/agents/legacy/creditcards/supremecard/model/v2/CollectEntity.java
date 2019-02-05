package se.tink.backend.aggregation.agents.creditcards.supremecard.model.v2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CollectEntity {
    @JsonProperty(value = "completeUrl", required = false)
    private String completeUrl;

    @JsonProperty(value = "error", required = false)
    private ErrorEntity error;

    @JsonProperty("progressStatus")
    private String progressStatus;

    public String getCompleteUrl() {
        return completeUrl;
    }

    public ErrorEntity getError() {
        return error;
    }

    public String getProgressStatus() {
        return progressStatus;
    }

    public boolean isAuthenticated() {
        return Objects.equal(progressStatus, "COMPLETE") && Objects.equal(getError(), null);
    }

    public void setCompleteUrl(String completeUrl) {
        this.completeUrl = completeUrl;
    }

    public void setError(ErrorEntity error) {
        this.error = error;
    }

    public void setProgressStatus(String progressStatus) {
        this.progressStatus = progressStatus;
    }

    public boolean shouldContinuePolling() {
        return Objects.equal(getError(), null) &&
                (Objects.equal(progressStatus, "OUTSTANDING_TRANSACTION") ||
                Objects.equal(progressStatus, "USER_SIGN") ||
                Objects.equal(progressStatus, "STARTED"));
    }
}
