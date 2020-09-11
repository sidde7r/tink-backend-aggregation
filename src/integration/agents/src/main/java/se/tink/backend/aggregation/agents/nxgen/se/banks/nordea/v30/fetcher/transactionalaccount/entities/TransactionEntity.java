package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionEntity {
    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty private boolean booked;
    @JsonProperty private double amount;
    @JsonProperty private String currency;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("booking_date")
    private Date bookingDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("interest_date")
    private Date interestDate;

    @JsonProperty private String title;

    @JsonProperty("balance_after")
    private double balanceAfter;

    @JsonProperty("transaction_type")
    private TransactionTypeEntity transactionType;

    @JsonIgnore
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(ExactCurrencyAmount.of(amount, currency))
                .setPending(!booked)
                .setDescription(title)
                .setDate(bookingDate)
                .build();
    }
}
