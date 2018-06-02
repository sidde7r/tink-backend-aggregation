package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IDControlConfiguration {

    @JsonProperty("dryRunDataRefresh")
    private boolean dryRunDataRefresh = true;

    @JsonProperty("retryDataRefreshUntilSuccessful")
    private boolean retryDataRefreshUntilSuccessful;

    @JsonProperty("daysToFetchChangesFor")
    private int daysToFetchChangesFor;

    public boolean isDryRunDataRefresh() {
        return dryRunDataRefresh;
    }

    public boolean isRetryDataRefreshUntilSuccessful() {
        return retryDataRefreshUntilSuccessful;
    }

    public int getDaysToFetchChangesFor() {
        return daysToFetchChangesFor;
    }

}
