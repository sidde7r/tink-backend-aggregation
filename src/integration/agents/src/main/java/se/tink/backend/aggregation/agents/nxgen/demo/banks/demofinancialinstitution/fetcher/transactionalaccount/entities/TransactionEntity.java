package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.Date;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

public class TransactionEntity {

    private long id;
    private String accountNumber;
    private BigDecimal amount;
    private String currency;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date date;

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

    public Date getDate() {
        return date;
    }

    @JsonIgnore
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(new Amount(currency, amount))
                .setDate(date)
                .setDescription(description)
                .setExternalId(String.valueOf(id))
                .build();
    }
}
