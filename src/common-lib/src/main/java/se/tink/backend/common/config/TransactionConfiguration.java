package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TransactionConfiguration {

    @JsonProperty
    private boolean enforceRemovalOfPendingAfterExpired = false;

    public boolean isEnforceRemovalOfPendingAfterExpired() {
        return enforceRemovalOfPendingAfterExpired;
    }
}
