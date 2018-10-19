package se.tink.backend.aggregation.agents.nxgen.at.creditcards.amex.fetcher.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.rpc.TimelineRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TimelineATRequest extends TimelineRequest {

    private boolean cmlEnabled;

    public boolean isCmlEnabled() {
        return cmlEnabled;
    }

    public void setCmlEnabled(boolean cmlEnabled) {
        this.cmlEnabled = cmlEnabled;
    }

}
