package se.tink.backend.aggregation.agents.nxgen.fi.creditcards.amex.fetcher.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TimelineRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TimelineFIRequest extends TimelineRequest {
    private boolean payWithPointsEnabled;

    public boolean getPayWithPointsEnabled() {
        return payWithPointsEnabled;
    }

    public void setPayWithPointsEnabled(boolean payWithPointsEnabled) {
        this.payWithPointsEnabled = payWithPointsEnabled;
    }
}
