package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.text.ParseException;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

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

    private Amount toTinkAmount() {
        return new Amount(billingCurrency, Double.parseDouble(billingAmount));
    }

    private Date toTransactionDate() {
        try {
            return ICSConstants.Date.TRANSACTION_FORMAT.parse(transactionDate);
        } catch (ParseException e) {
            throw new IllegalStateException("Cannot parse date!");
        }
    }

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(toTinkAmount())
                .setDescription(transactionInformation)
                .setDate(toTransactionDate())
                .setExternalId(transactionId)
                .build();
    }
}
