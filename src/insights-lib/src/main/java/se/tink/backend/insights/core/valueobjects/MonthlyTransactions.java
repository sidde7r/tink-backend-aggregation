package se.tink.backend.insights.core.valueobjects;

import java.util.List;
import se.tink.backend.insights.core.domain.HoldsTransactions;
import se.tink.backend.insights.core.domain.model.InsightTransaction;

public class MonthlyTransactions implements HoldsTransactions {
    private List<InsightTransaction> transactions;
    private int transactionsCount = 0;
    private double transactionsTotal = 0;
    private Month month;

    MonthlyTransactions(
            List<InsightTransaction> transactions,
            Month month) {
        this.transactions = transactions;
        transactions.forEach(t -> {
                    transactionsCount += 1;
                    transactionsTotal += t.getAmount();
                }
        );
        this.month = month;
    }

    public static MonthlyTransactions of(List<InsightTransaction> transactions, Month month) {
        return new MonthlyTransactions(transactions, month);
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

    public Month getMonth() {
        return month;
    }
}

