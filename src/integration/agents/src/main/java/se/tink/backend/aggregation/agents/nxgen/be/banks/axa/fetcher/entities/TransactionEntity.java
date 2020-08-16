package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.entities;

import java.math.BigDecimal;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class TransactionEntity {
    private BigDecimal amount;
    private String counterpartyName;
    private String description;
    private String processedDateTime;
    private String currency;
    private String orginatorAccountNumber;
    private String counterPartyAccountNumber;
    private Date transactionDate;
    private BigDecimal balanceAmount;

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCounterparty() {
        return counterpartyName;
    }

    public String getCreationTimeStamp() {
        return processedDateTime;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDescription() {
        return description;
    }

    public String getOrginatorAccountNumber() {
        return orginatorAccountNumber;
    }

    public String getCounterPartyAccountNumber() {
        return counterPartyAccountNumber;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public BigDecimal getBalanceAmount() {
        return balanceAmount;
    }
}
