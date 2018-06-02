package se.tink.backend.insights.core.domain.model;

import java.util.Comparator;
import java.util.Date;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.insights.core.valueobjects.TransactionId;

public class InsightTransaction {
    private TransactionId transactionId;
    private double amount;
    private Date date;
    private CategoryTypes categoryTypes;
    private String categoryId;
    public static Comparator<InsightTransaction> TRANSACTION_BY_AMOUNT = Comparator
            .comparingDouble(transaction -> transaction.getAmount());

    public InsightTransaction(TransactionId transactionId, double amount, Date date,
            CategoryTypes categoryTypes, String categoryId) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.date = date;
        this.categoryTypes = categoryTypes;
        this.categoryId = categoryId;
    }

    public TransactionId getTransactionId() {
        return transactionId;
    }

    public double getAmount() {
        return amount;
    }

    public Date getDate() {
        return date;
    }

    public CategoryTypes getCategoryTypes() {
        return categoryTypes;
    }

    public String getCategoryId() {
        return categoryId;
    }
}
