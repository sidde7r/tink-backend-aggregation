package se.tink.backend.aggregation.agents.nxgen.be.creditcards.amex.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TimelineBERequest {

    private boolean cmlEnabled;

    public boolean isCmlEnabled() {
        return cmlEnabled;
    }

    public void setCmlEnabled(boolean cmlEnabled) {
        this.cmlEnabled = cmlEnabled;
    }

}
