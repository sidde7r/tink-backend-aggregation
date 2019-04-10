package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DetailsEntity {
    @JsonProperty("RecipientName")
    private String recipientName;

    @JsonProperty("RecipientAccountNumber")
    private String recipientAccountNumber;

    @JsonProperty("RecipientType")
    private String recipientType;

    @JsonProperty("ReferenceType")
    private String referenceType;

    @JsonProperty("Reference")
    private String reference;

    @JsonProperty("HasEgiroInvoice")
    private boolean hasEgiroInvoice;

    @JsonProperty("EgiroUniqueId")
    private String egiroUniqueId;

    @JsonProperty("SwishAmount")
    private double swishAmount;

    @JsonProperty("HasCurrencyExchangeFee")
    private boolean hasCurrencyExchangeFee;

    @JsonProperty("SourceSystemDate")
    private String sourceSystemDate;

    @JsonProperty("TrnAmount")
    private String trnAmount;

    @JsonProperty("CurrencyCode")
    private String currencyCode;

    @JsonProperty("FormatedRate")
    private String formatedRate;

    @JsonProperty("Rate")
    private double rate;

    @JsonProperty("MerchantCity")
    private String merchantCity;

    @JsonProperty("MerchantName")
    private String merchantName;

    @JsonProperty("CurrencyExchangeFee")
    private String currencyExchangeFee;

    @JsonProperty("BankName")
    private String bankName;

    @JsonProperty("SwishReferenceId")
    private String swishReferenceId;

    @JsonProperty("SwishRecipientDescription")
    private String swishRecipientDescription;

    @JsonProperty("SwishSenderDescription")
    private String swishSenderDescription;

    @JsonProperty("SwishMessage")
    private String swishMessage;

    @JsonProperty("SwishTime")
    private String swishTime;

    public String getRecipientName() {
        return recipientName;
    }

    public String getRecipientAccountNumber() {
        return recipientAccountNumber;
    }

    public String getRecipientType() {
        return recipientType;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public String getReference() {
        return reference;
    }

    public boolean isHasEgiroInvoice() {
        return hasEgiroInvoice;
    }

    public String getEgiroUniqueId() {
        return egiroUniqueId;
    }

    public double getSwishAmount() {
        return swishAmount;
    }

    public boolean isHasCurrencyExchangeFee() {
        return hasCurrencyExchangeFee;
    }

    public String getSourceSystemDate() {
        return sourceSystemDate;
    }

    public String getTrnAmount() {
        return trnAmount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getFormatedRate() {
        return formatedRate;
    }

    public double getRate() {
        return rate;
    }

    public String getMerchantCity() {
        return merchantCity;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public String getCurrencyExchangeFee() {
        return currencyExchangeFee;
    }

    public String getBankName() {
        return bankName;
    }

    public String getSwishReferenceId() {
        return swishReferenceId;
    }

    public String getSwishRecipientDescription() {
        return swishRecipientDescription;
    }

    public String getSwishSenderDescription() {
        return swishSenderDescription;
    }

    public String getSwishMessage() {
        return swishMessage;
    }

    public String getSwishTime() {
        return swishTime;
    }
}
