package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.creditcard.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class CreditCardTransactionEntity {
    @JsonProperty("Currency")
    private String currency;

    @JsonProperty("Date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private Date transactionDate;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("Amount")
    private double value;

    public Transaction toTinkTransaction() {
        ExactCurrencyAmount transactionAmount = ExactCurrencyAmount.of(value, currency);
        return Transaction.builder()
                .setDescription(description)
                .setDate(transactionDate)
                .setAmount(transactionAmount)
                .build();
    }
}
