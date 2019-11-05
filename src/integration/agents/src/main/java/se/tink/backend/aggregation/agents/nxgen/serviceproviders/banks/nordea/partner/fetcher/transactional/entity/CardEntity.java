package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardEntity {
    @JsonProperty("card_number")
    private String cardNumber;

    @JsonProperty("merchant_city")
    private String merchantCity;

    @JsonProperty("merchant_country")
    private String merchantCountry;
}
