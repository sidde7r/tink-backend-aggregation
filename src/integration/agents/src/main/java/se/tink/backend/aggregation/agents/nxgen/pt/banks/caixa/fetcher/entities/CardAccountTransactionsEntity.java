package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities;

import java.math.BigDecimal;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardAccountTransactionsEntity {
    private BigDecimal automaticDebit;
    private BigDecimal balanceAfterStatementIssuance;
    private BigDecimal creditTotal;
    private BigDecimal debitTotal;
    private BigDecimal minimumPaymentAmount;
    private BigDecimal previousBalance;
    private List<CardTransactionEntity> transactions;

    public BigDecimal getAutomaticDebit() {
        return automaticDebit;
    }

    public BigDecimal getBalanceAfterStatementIssuance() {
        return balanceAfterStatementIssuance;
    }

    public BigDecimal getCreditTotal() {
        return creditTotal;
    }

    public BigDecimal getDebitTotal() {
        return debitTotal;
    }

    public BigDecimal getMinimumPaymentAmount() {
        return minimumPaymentAmount;
    }

    public BigDecimal getPreviousBalance() {
        return previousBalance;
    }

    public List<CardTransactionEntity> getTransactions() {
        return transactions;
    }
}
