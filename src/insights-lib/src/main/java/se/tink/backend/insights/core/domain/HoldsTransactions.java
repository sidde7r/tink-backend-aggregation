package se.tink.backend.insights.core.domain;

import java.util.List;
import java.util.Optional;
import se.tink.backend.insights.core.domain.model.InsightTransaction;

public interface HoldsTransactions {

    List<InsightTransaction> getTransactions();

    int getTransactionsCount();

    double getTotalAmountInTransactions();

    default Optional<InsightTransaction> getLargestTransaction() {
        return getTransactions().stream().min(InsightTransaction.TRANSACTION_BY_AMOUNT);
    }
}
