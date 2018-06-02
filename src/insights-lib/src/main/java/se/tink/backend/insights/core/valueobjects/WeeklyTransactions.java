package se.tink.backend.insights.core.valueobjects;

import java.util.List;
import se.tink.backend.insights.core.domain.HoldsTransactions;
import se.tink.backend.insights.core.domain.model.InsightTransaction;

public class WeeklyTransactions implements HoldsTransactions {
    private List<InsightTransaction> transactions;
    private int transactionsCount = 0;
    private double transactionsTotal = 0;
    private Week week;

    WeeklyTransactions(
            List<InsightTransaction> transactions,
            Week week) {
        this.transactions = transactions;
        transactions.forEach(t -> {
                    transactionsCount += 1;
                    transactionsTotal += t.getAmount();
                }
        );
        this.week = week;
    }

    public static WeeklyTransactions of(List<InsightTransaction> transactions, Week week) {
        return new WeeklyTransactions(transactions, week);
    }

    @Override
    public List<InsightTransaction> getTransactions() {
        return transactions;
    }

    @Override
    public int getTransactionsCount() {
        return transactionsCount;
    }

    @Override
    public double getTotalAmountInTransactions() {
        return transactionsTotal;
    }

    public Week getWeek() {
        return week;
    }
}

