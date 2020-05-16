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
}
