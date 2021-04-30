package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionEntity {
    @JsonProperty("AccountId")
    private String accountId;

    @JsonProperty("TransactionId")
    private String transactionId;

    @JsonProperty("LastFourDigits")
    private String lastFourDigits;

    @JsonProperty("IndicatorExtraCard")
    private String indicatorExtraCard;

    @JsonProperty("CountryCode")
    private String countryCode;

    @JsonProperty("TransactionDate")
    private String transactionDate;

    @JsonProperty("BillingAmount")
    private String billingAmount;

    @JsonProperty("BillingCurrency")
    private String billingCurrency;

    @JsonProperty("SourceAmount")
    private String sourceAmount;

    @JsonProperty("SourceCurrency")
    private String sourceCurrency;

    @JsonProperty("EmbossingName")
    private String embossingName;

    @JsonProperty("ProcessingTime")
    private String processingTime;

    @JsonProperty("CreditDebitIndicator")
    private String creditDebitIndicator;

    @JsonProperty("Status")
    private String booked;

    @JsonProperty("TransactionInformation")
    private String transactionInformation;

    @JsonProperty("MerchantDetails")
    private MerchantEntity merchantDetails;

    @JsonIgnore
    private final SimpleDateFormat transactionFormat = new SimpleDateFormat("yyyy-MM-dd");

    private Date toTransactionDate() {
        try {
            return transactionFormat.parse(transactionDate);
        } catch (ParseException e) {
            throw new IllegalStateException("Cannot parse date!", e);
        }
    }

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(toTinkAmount())
                .setDescription(transactionInformation)
                .setDate(toTransactionDate())
                .setPayload(
                        TransactionPayloadTypes.TRANSFER_ACCOUNT_NAME_EXTERNAL, getMerchantName())
                .setPayload(TransactionPayloadTypes.MESSAGE, getMerchantCategoryCodeDescription())
                .build();
    }

    private String getMerchantCategoryCodeDescription() {
        return Optional.ofNullable(merchantDetails)
                .map(MerchantEntity::getMerchantCategoryCodeDescription)
                .orElse("N/A");
    }

    private String getMerchantName() {
        return Optional.ofNullable(merchantDetails)
                .map(MerchantEntity::getMerchantName)
                .orElse("N/A");
    }

    private ExactCurrencyAmount toTinkAmount() {

        ExactCurrencyAmount result =
                ExactCurrencyAmount.of(Double.parseDouble(billingAmount), billingCurrency);

        return result.negate();
    }

    public String getCreditCardHolderName() {
        return Optional.ofNullable(embossingName).orElse("N/A");
    }
}
