package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.creditcards.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardEntity {

    private String cardNumber;
    private String cardNumberMasked;
    private String cardType;
    private String expiryDate;
    private String cardPrimaryIndicator;
    private String embossingLine1;
    private boolean isClosedCard;
    private String encryptedCardNumber;
    private String ownerSystem;
    private String migrationStatus;
    private ProductEntity product;
    private ComboCardEntity comboCard;
    private boolean combo;

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCardNumberMasked() {
        return cardNumberMasked;
    }

    public String getCardType() {
        return cardType;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public String getCardPrimaryIndicator() {
        return cardPrimaryIndicator;
    }

    public String getEmbossingLine1() {
        return embossingLine1;
    }

    public boolean isClosedCard() {
        return isClosedCard;
    }

    public String getEncryptedCardNumber() {
        return encryptedCardNumber;
    }

    public String getOwnerSystem() {
        return ownerSystem;
    }

    public String getMigrationStatus() {
        return migrationStatus;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public ComboCardEntity getComboCard() {
        return comboCard;
    }

    public boolean isCombo() {
        return combo;
    }
}
