package se.tink.backend.aggregation.agents.nxgen.se.banks.collector.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionEntity {

    private String transactionType;

    private BigDecimal amount;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date created;

    @JsonIgnore
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(new ExactCurrencyAmount(amount, "SEK"))
                .setDate(created)
                .setDescription(transactionType)
                .setPending(false)
                .build();
    }
}
