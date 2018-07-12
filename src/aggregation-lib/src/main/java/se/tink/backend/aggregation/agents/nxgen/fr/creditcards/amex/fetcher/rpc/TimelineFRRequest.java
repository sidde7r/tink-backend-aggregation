package se.tink.backend.aggregation.agents.nxgen.fr.creditcards.amex.fetcher.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.rpc.TimelineRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TimelineFRRequest extends TimelineRequest {
    private boolean payWithPointsEnabled;

    public boolean getPayWithPointsEnabled() {
        return payWithPointsEnabled;
    }

    public void setPayWithPointsEnabled(boolean payWithPointsEnabled) {
        this.payWithPointsEnabled = payWithPointsEnabled;
    }
}
