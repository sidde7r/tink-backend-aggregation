package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities.pension;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PremiumsEntity {

    @JsonProperty("PremiumAmount")
    private double premiumAmount;

    @JsonProperty("PremiumTypeName")
    private String premiumTypeName;
}
