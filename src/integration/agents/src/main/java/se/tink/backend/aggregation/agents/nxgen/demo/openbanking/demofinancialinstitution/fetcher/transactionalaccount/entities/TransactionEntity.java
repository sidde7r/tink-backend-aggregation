package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demofinancialinstitution.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.Date;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

public class TransactionEntity {

    private long id;
    private String receiverBank;
    private String receiverAccount;
    private String transactionId;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date date;

    private BigDecimal amount;
    private String currency;

    public long getId() {
        return id;
    }

    public String getReceiverBank() {
        return receiverBank;
    }

    public String getReceiverAccount() {
        return receiverAccount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getDescription() {
        return description;
    }

    public Date getDate() {
        return date;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    @JsonIgnore
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(new Amount(currency, amount))
                .setDate(date)
                .setDescription(description)
                .build();
    }
}
