package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.creditcard.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardDetailsResponse {
    private String cardId;
    private String cardNumber;
    private String cardName;
    private String cardHolderName;
    private String cardType;
    private String expiryDate;
    private String expiryDateTxt;
    private String accountNumber;
    private String accountName;
    private String accountOwner;
    private String status;
    private String statusText;
    private String regNo;
    private String imageUrl;
    private boolean canBlock;
    private boolean canActivate;
    private boolean canUnblock;
    private boolean activationRequiresGeoSec;

    public String getCardId() {
        return cardId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCardName() {
        return cardName;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public String getCardType() {
        return cardType;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public String getExpiryDateTxt() {
        return expiryDateTxt;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getAccountOwner() {
        return accountOwner;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusText() {
        return statusText;
    }

    public String getRegNo() {
        return regNo;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isCanBlock() {
        return canBlock;
    }

    public boolean isCanActivate() {
        return canActivate;
    }

    public boolean isCanUnblock() {
        return canUnblock;
    }

    public boolean isActivationRequiresGeoSec() {
        return activationRequiresGeoSec;
    }
}
