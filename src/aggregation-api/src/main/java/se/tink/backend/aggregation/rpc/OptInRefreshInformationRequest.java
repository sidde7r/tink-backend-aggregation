package se.tink.backend.aggregation.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OptInRefreshInformationRequest extends RefreshInformationRequest{
    @JsonProperty
    private boolean optIn;

    public void setOptIn(boolean optIn) {
        this.optIn = optIn;
    }

    public boolean isOptIn() {
        return optIn;
    }
}
