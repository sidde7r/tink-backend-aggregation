package se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.creditcard.entity;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CreditCardEntity {

    private static final String CREDIT_CARD_CATEGORY = "credit";

    @Getter private String cardId;
    private String cardCategory;

    public boolean isCreditCard() {
        return CREDIT_CARD_CATEGORY.equals(cardCategory);
    }
}
