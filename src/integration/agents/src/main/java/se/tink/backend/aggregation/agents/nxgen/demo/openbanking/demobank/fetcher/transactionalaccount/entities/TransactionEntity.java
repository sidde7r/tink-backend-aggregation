package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class TransactionEntity {
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("date")
    private String date;

    @JsonProperty("description")
    private String description;

    @JsonProperty("amount")
    private Integer amount;

    @JsonProperty("pending")
    private Boolean pending;

    @JsonIgnore
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(exactCurrencyAmount())
                .setDescription(description)
                .build();
    }

    public ExactCurrencyAmount exactCurrencyAmount() {
        return ExactCurrencyAmount.of(BigDecimal.valueOf(amount), "GBP");
    }
}
