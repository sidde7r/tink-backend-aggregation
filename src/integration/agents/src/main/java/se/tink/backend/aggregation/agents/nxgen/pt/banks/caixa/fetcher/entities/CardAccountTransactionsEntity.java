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

    public BigDecimal getCreditTotal() {
        return creditTotal;
    }

    public List<CardTransactionEntity> getTransactions() {
        return transactions;
    }
}
