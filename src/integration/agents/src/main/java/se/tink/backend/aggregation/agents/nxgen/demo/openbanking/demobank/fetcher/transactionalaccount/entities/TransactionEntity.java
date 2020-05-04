package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Date;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class TransactionEntity {
    @JsonProperty("id")
    private Integer id;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date date;

    @JsonProperty("description")
    private String description;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("pending")
    private Boolean pending;

    @JsonProperty("currency")
    private String currency;

    @JsonIgnore
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(exactCurrencyAmount())
                .setDescription(description)
                .setDate(date)
                .setPending(pending)
                .build();
    }

    public ExactCurrencyAmount exactCurrencyAmount() {
        return ExactCurrencyAmount.of(amount, currency);
    }
}
