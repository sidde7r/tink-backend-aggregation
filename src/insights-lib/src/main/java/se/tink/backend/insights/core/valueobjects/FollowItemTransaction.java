package se.tink.backend.insights.core.valueobjects;

import java.util.List;
import se.tink.backend.insights.core.domain.model.InsightTransaction;

public class FollowItemTransaction {
    private List<InsightTransaction> transactions;
    private double transactionTotalAmount = 0;
    private double targetAmount;
    private double leftOfBudget;
    private double threshold = 0.90; // Consider 90 % of spending on budget close
    private double budgetProgress;
    private String name;

    FollowItemTransaction(List<InsightTransaction> transactions, Double targetAmount, String name) {
        this.transactions = transactions;
        this.name = name;
        transactions.forEach(t -> transactionTotalAmount += t.getAmount());
        this.targetAmount = targetAmount;

        this.leftOfBudget = targetAmount - transactionTotalAmount;
        this.budgetProgress = transactionTotalAmount / targetAmount; // budgetProgress in percent
    }

    public static FollowItemTransaction of(List<InsightTransaction> transactions, Double targetAmount, String name) {
        return new FollowItemTransaction(transactions, targetAmount, name);
    }

    public double getLeftOfBudget() {
        return Math.abs(leftOfBudget);
    }

    public double getTransactionTotalAmount() {
        return Math.abs(transactionTotalAmount);
    }

    public double getTargetAmount() {
        return Math.abs(targetAmount);
    }

    public boolean hasOverSpent(){
        return leftOfBudget > 0;
    }

    public boolean isCloseToOverSpending() {
        return (leftOfBudget < 0 && budgetProgress > threshold);
    }

    public String getName() {
        return name;
    }
}
