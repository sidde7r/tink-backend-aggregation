package se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.fetcher.entities;

import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardDetailsEntity {
    private String cardHolderName;
    private String pcidssToken;
    private String maskedCardNo;
    private Number isPrimaryCard;
    private String issueDate;
    private String expiryDate;
    private String cardStatus;

    public String getCardHolderName() {
        return Optional.ofNullable(cardHolderName).orElse("");
    }
}
