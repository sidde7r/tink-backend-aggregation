package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class StockEntity {

    @JsonProperty("onboardingStatus")
    private String onboardingStatus;

    @JsonProperty("countryCode")
    private String countryCode;
}
