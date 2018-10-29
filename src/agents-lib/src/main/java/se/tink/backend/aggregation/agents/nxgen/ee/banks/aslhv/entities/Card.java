package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Card {

    @JsonProperty("owner_name")
    private String ownerName;

    @JsonProperty("additional")
    private boolean additional;

    @JsonProperty("credit_interest_amount")
    private double creditInterestAmount;

    @JsonProperty("credit_reserved_amount")
    private double creditReservedAmount;

    @JsonProperty("product_code")
    private String productCode;

    @JsonProperty("type")
    private String type;

    @JsonProperty("credit_free_amount")
    private double creditFreeAmount;

    @JsonProperty("limit_portfolio_id")
    private int limitPortfolioId;

    @JsonProperty("product_name")
    private String productName;

    @JsonProperty("card_id")
    private int cardId;

    @JsonProperty("secondary_number")
    private String secondaryNumber;

    @JsonProperty("number")
    private String number;

    @JsonProperty("valid_until")
    private String validUntil;

    @JsonProperty("business_name_on_card")
    private Object businessNameOnCard;

    @JsonProperty("product_class_code")
    private String productClassCode;

    @JsonProperty("service_portfolio_id")
    private int servicePortfolioId;

    @JsonProperty("credit_limit_amount")
    private double creditLimitAmount;

    @JsonProperty("color_code")
    private String colorCode;

    @JsonProperty("name_on_card")
    private String nameOnCard;

    @JsonProperty("status")
    private String status;

    public String getOwnerName() {
        return ownerName;
    }

    public boolean isAdditional() {
        return additional;
    }

    public double getCreditInterestAmount() {
        return creditInterestAmount;
    }

    public double getCreditReservedAmount() {
        return creditReservedAmount;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getType() {
        return type;
    }

    public double getCreditFreeAmount() {
        return creditFreeAmount;
    }

    public int getLimitPortfolioId() {
        return limitPortfolioId;
    }

    public String getProductName() {
        return productName;
    }

    public int getCardId() {
        return cardId;
    }

    public String getSecondaryNumber() {
        return secondaryNumber;
    }

    public String getNumber() {
        return number;
    }

    public String getValidUntil() {
        return validUntil;
    }

    public Object getBusinessNameOnCard() {
        return businessNameOnCard;
    }

    public String getProductClassCode() {
        return productClassCode;
    }

    public int getServicePortfolioId() {
        return servicePortfolioId;
    }

    public double getCreditLimitAmount() {
        return creditLimitAmount;
    }

    public String getColorCode() {
        return colorCode;
    }

    public String getNameOnCard() {
        return nameOnCard;
    }

    public String getStatus() {
        return status;
    }
}
