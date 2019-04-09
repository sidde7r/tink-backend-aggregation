package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ExchangeRateItem {

    @JsonProperty("currencyTo")
    private String currencyTo;

    @JsonProperty("currencyFrom")
    private String currencyFrom;

    @JsonProperty("rateFrom")
    private String rateFrom;

    public String getCurrencyTo() {
        return currencyTo;
    }

    public void setCurrencyTo(final String currencyTo) {
        this.currencyTo = currencyTo;
    }

    public String getCurrencyFrom() {
        return currencyFrom;
    }

    public void setCurrencyFrom(final String currencyFrom) {
        this.currencyFrom = currencyFrom;
    }

    public String getRateFrom() {
        return rateFrom;
    }

    public void setRateFrom(final String rateFrom) {
        this.rateFrom = rateFrom;
    }
}
