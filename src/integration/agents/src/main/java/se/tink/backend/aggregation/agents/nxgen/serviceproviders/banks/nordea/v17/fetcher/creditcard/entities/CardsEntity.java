package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.creditcard.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.serializer.NordeaHashMapDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardsEntity {
    @JsonDeserialize(using = NordeaHashMapDeserializer.Double.class)
    private Double balance;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String cardId;
    @JsonDeserialize(using = NordeaHashMapDeserializer.Double.class)
    private Double creditLimit;
    @JsonDeserialize(using = NordeaHashMapDeserializer.Double.class)
    private Double fundsAvailable;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String ownerName;

    public Double getCurrentBalance() {

        if (creditLimit == null) {
            return getFundsAvailable();
        }

        return getFundsAvailable() - creditLimit;
    }

    public Double getFundsAvailable() {
        if (fundsAvailable == null || fundsAvailable == 0) {
            return balance;
        }
        return fundsAvailable;
    }

    public Double getBalance() {
        return balance;
    }

    public String getCardId() {
        return cardId;
    }

    public Double getCreditLimit() {
        return creditLimit;
    }

    public String getOwnerName() {
        return ownerName;
    }
}
