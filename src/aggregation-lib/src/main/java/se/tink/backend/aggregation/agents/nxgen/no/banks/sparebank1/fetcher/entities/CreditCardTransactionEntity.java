package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1AmountUtils;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.libraries.date.DateUtils;

@JsonObject
public class CreditCardTransactionEntity {
    private String transactionReference;
    private String postingDate;
    private String transactionDate;
    private String transactionText;
    private String transactionTypeDescription;
    private String transactionCurrency;
    private String transactionAmount;
    private String transactionAmountFraction;
    private String billingAmount;
    private String billingAmountFraction;
    private String merchantCategory;
    private String merchantCity;
    private String cardNumber;
    private String cardId;
    private String exchangeRate;
    private String exchangeRateFraction;
    private Boolean foreignCurrency;
    private Boolean disputeAllowed;
    private Boolean fraudClaimed;
    private Boolean authorization;
    @JsonProperty("_links")
    private HashMap<String, LinkEntity> links;
    private String externalAccountNo;

    @JsonIgnore
    public CreditCardTransaction toTinkTransaction() {
        return CreditCardTransaction.builder()
                .setAmount(Sparebank1AmountUtils.constructAmount(billingAmount, billingAmountFraction))
                .setDate(DateUtils.parseDate(postingDate == null ? transactionDate : postingDate))
                .setDescription(transactionText)
                .setPending(postingDate == null)
                .build();
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public String getPostingDate() {
        return postingDate;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public String getTransactionText() {
        return transactionText;
    }

    public String getTransactionTypeDescription() {
        return transactionTypeDescription;
    }

    public String getTransactionCurrency() {
        return transactionCurrency;
    }

    public String getTransactionAmount() {
        return transactionAmount;
    }

    public String getTransactionAmountFraction() {
        return transactionAmountFraction;
    }

    public String getBillingAmount() {
        return billingAmount;
    }

    public String getBillingAmountFraction() {
        return billingAmountFraction;
    }

    public String getMerchantCategory() {
        return merchantCategory;
    }

    public String getMerchantCity() {
        return merchantCity;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCardId() {
        return cardId;
    }

    public String getExchangeRate() {
        return exchangeRate;
    }

    public String getExchangeRateFraction() {
        return exchangeRateFraction;
    }

    public Boolean getForeignCurrency() {
        return foreignCurrency;
    }

    public Boolean getDisputeAllowed() {
        return disputeAllowed;
    }

    public Boolean getFraudClaimed() {
        return fraudClaimed;
    }

    public Boolean getAuthorization() {
        return authorization;
    }

    public HashMap<String, LinkEntity> getLinks() {
        return links;
    }

    public String getExternalAccountNo() {
        return externalAccountNo;
    }
}
