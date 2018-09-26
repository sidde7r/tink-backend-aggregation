package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MerchantEntity {
    @JsonProperty("MerchantName")
    private String merchantName;
    @JsonProperty("MerchantCategoryCodeDescription")
    private String merchantCategoryCodeDescription;
}
