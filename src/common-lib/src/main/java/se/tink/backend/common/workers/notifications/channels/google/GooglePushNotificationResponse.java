package se.tink.backend.common.workers.notifications.channels.google;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GooglePushNotificationResponse {

    @JsonProperty("multicast_id")
    private String multicastId;
    private Integer success;
    private Integer failure;
    private Integer canonical_ids;
    private List<GooglePushNotificationResponseResult> results;

    public String getMulticastId() {
        return multicastId;
    }

    public void setMulticastId(String multicastId) {
        this.multicastId = multicastId;
    }

    public Integer getSuccess() {
        return success;
    }

    public void setSuccess(Integer success) {
        this.success = success;
    }

    public Integer getFailure() {
        return failure;
    }

    public void setFailure(Integer failure) {
        this.failure = failure;
    }

    public Integer getCanonical_ids() {
        return canonical_ids;
    }

    public void setCanonical_ids(Integer canonical_ids) {
        this.canonical_ids = canonical_ids;
    }

    public List<GooglePushNotificationResponseResult> getResults() {
        return results;
    }

    public void setResults(List<GooglePushNotificationResponseResult> results) {
        this.results = results;
    }

    @Override
    public String toString() {

        return MoreObjects.toStringHelper(this)
                .add("success", success)
                .add("failure", failure)
                .add("canonical_ids", canonical_ids)
                .add("results", results == null ? "null" : Iterables.toString(results))
                .toString();
    }
}

