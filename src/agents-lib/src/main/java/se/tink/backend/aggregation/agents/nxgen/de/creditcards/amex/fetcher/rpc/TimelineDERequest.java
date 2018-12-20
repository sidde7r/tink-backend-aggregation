package se.tink.backend.aggregation.agents.nxgen.de.creditcards.amex.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TimelineRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TimelineDERequest extends TimelineRequest {


    private boolean cmlEnabled;
    private boolean goodsSvcOfferEnabled;
    private boolean payWithPointsEnabled;
    private boolean payYourWayEnabled;
    private boolean pushEnabled;
    @JsonProperty("sortedIndex")
    private String sortedIndexUK;

    public boolean isCmlEnabled() {
        return cmlEnabled;
    }

    public void setCmlEnabled(boolean cmlEnabled) {
        this.cmlEnabled = cmlEnabled;
    }

    public boolean isGoodsSvcOfferEnabled() {
        return goodsSvcOfferEnabled;
    }

    public TimelineDERequest setGoodsSvcOfferEnabled(boolean goodsSvcOfferEnabled) {
        this.goodsSvcOfferEnabled = goodsSvcOfferEnabled;
        return this;
    }

    public boolean isPayWithPointsEnabled() {
        return payWithPointsEnabled;
    }

    public TimelineDERequest setPayWithPointsEnabled(boolean payWithPointsEnabled) {
        this.payWithPointsEnabled = payWithPointsEnabled;
        return this;
    }

    public boolean isPayYourWayEnabled() {
        return payYourWayEnabled;
    }

    public TimelineDERequest setPayYourWayEnabled(boolean payYourWayEnabled) {
        this.payYourWayEnabled = payYourWayEnabled;
        return this;
    }

    public boolean isPushEnabled() {
        return pushEnabled;
    }

    public TimelineDERequest setPushEnabled(boolean pushEnabled) {
        this.pushEnabled = pushEnabled;
        return this;
    }

    public TimelineDERequest setSortedIndex(String sortedIndex) {
        this.sortedIndexUK = sortedIndex;
        return this;
    }
}
