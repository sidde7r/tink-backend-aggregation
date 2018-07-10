package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TimelineResponse {
    private TimelineEntity timeline;

    public TimelineEntity getTimeline() {
        return timeline;
    }
}
