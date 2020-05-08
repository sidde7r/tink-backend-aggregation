package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionEntity {

    private long id;
    private String accountNumber;
    private BigDecimal amount;
    private String currency;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date transactionDate;

    public long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDescription() {
        return description;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    @JsonIgnore
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(ExactCurrencyAmount.of(amount, currency))
                .setDate(transactionDate)
                .setDescription(description)
                .build();
    }
}
