package se.tink.backend.insights.transactions.mapper;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.core.Transaction;
import se.tink.backend.insights.core.domain.model.InsightTransaction;
import se.tink.backend.insights.core.valueobjects.TransactionId;

public class TransactionMapper {
    public static InsightTransaction translate(Transaction transaction) {
        return new InsightTransaction(
                TransactionId.of(transaction.getId()),
                transaction.getAmount(),
                transaction.getDate(),
                transaction.getCategoryType(),
                transaction.getCategoryId());
    }

    public static List<InsightTransaction> translate(List<Transaction> transactions) {
        return transactions.stream().map(TransactionMapper::translate).collect(Collectors.toList());
    }
}
