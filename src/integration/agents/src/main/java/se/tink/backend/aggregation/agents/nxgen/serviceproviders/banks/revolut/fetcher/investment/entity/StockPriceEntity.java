package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class StockPriceEntity {

    @JsonProperty("last")
    private int last;

    @JsonProperty("nextOpenUtcTimestamp")
    private int nextOpenUtcTimestamp;

    @JsonProperty("previous")
    private int previous;

    @JsonProperty("instrument")
    private String instrument;

    @JsonProperty("status")
    private String status;

    public int getLast() {
        return last;
    }

    public int getNextOpenUtcTimestamp() {
        return nextOpenUtcTimestamp;
    }

    public int getPrevious() {
        return previous;
    }

    public String getInstrument() {
        return instrument;
    }

    public String getStatus() {
        return status;
    }
}
