package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TransactionEntity {
    private String transactionId;
    private boolean booked;
    private double amount;
    private String currency;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bookingDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date transactionDate;

    private String interestDate;
    private String title;
    private double balanceAfter;
    private TransactionTypeEntity transactionType;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(ExactCurrencyAmount.of(amount, currency))
                .setPending(!booked)
                .setDescription(title)
                .setDate(getDate())
                .build();
    }

    private Date getDate() {
        return booked ? bookingDate : transactionDate;
    }
}
