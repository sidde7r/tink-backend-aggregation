package se.tink.backend.aggregation.agents.nxgen.se.creditcards.amex.fetcher.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.fetcher.rpc.TimelineRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TimelineSERequest extends TimelineRequest {
    private boolean cmlEnabled;

    public boolean isCmlEnabled() {
        return cmlEnabled;
    }

    public void setCmlEnabled(boolean cmlEnabled) {
        this.cmlEnabled = cmlEnabled;
    }
}
