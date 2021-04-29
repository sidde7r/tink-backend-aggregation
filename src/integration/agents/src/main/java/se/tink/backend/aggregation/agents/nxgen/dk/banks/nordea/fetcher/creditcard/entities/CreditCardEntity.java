package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CreditCardEntity {

    @JsonIgnore private static final String CREDIT_CARD_CATEGORY = "credit";

    private String cardId;
    private String cardCategory;
    private String principalCardholderName;

    public String getCardId() {
        return cardId;
    }

    public String getPrincipalCardholderName() {
        return principalCardholderName;
    }

    public boolean isCreditCard() {
        return CREDIT_CARD_CATEGORY.equals(cardCategory);
    }
}
