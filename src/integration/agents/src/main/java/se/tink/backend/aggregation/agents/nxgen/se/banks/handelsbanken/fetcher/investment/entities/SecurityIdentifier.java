package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SecurityIdentifier {
    private String isinCode;
    private String currency;
    private String country;

    @JsonProperty("mic")
    private String market;

    public String getIsinCode() {
        return isinCode;
    }

    public String getCurrency() {
        return currency;
    }

    public String getCountry() {
        return country;
    }

    public String getMarket() {
        return market;
    }
}
