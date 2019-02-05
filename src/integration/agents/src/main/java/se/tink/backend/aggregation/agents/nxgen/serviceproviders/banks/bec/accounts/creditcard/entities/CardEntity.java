package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.creditcard.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardEntity {
    private String cardName;
    private String cardNumber;
    private String cardType;
    private String status;
    private String statusText;
    private String cardId;
    private boolean hasGeoSec;
    private String imageUrl;
    private String urlDetails;
    private boolean isInWallet;

    public String getCardName() {
        return cardName;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCardType() {
        return cardType;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusText() {
        return statusText;
    }

    public String getCardId() {
        return cardId;
    }

    public boolean getHasGeoSec() {
        return hasGeoSec;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getUrlDetails() {
        return urlDetails;
    }

    public boolean getInWallet() {
        return isInWallet;
    }

    public boolean isCardActive() {
        return BecConstants.CreditCard.STATUS_ACTIVE.equalsIgnoreCase(statusText);
    }
}
