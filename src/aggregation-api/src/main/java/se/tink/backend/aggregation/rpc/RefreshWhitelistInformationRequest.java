package se.tink.backend.aggregation.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RefreshWhitelistInformationRequest extends WhitelistRequest {
    // opt-in flag indicates if we want the user to select the account to aggregate
    // if true, user should be able to reselect the accounts to aggregate
    // if false, we aggregate the accounts that is in the request
    @JsonProperty
    private boolean optIn;

    public void setOptIn(boolean optIn) {
        this.optIn = optIn;
    }

    public boolean isOptIn() {
        return optIn;
    }
}
