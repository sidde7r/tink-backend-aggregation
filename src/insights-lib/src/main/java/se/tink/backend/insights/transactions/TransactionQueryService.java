package se.tink.backend.insights.transactions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Doubles;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import se.tink.backend.core.User;
import se.tink.backend.insights.core.domain.model.InsightTransaction;
import se.tink.backend.insights.core.valueobjects.MonthlyTransactions;
import se.tink.backend.insights.core.valueobjects.WeeklyTransactions;
import se.tink.backend.insights.core.valueobjects.UserId;
import se.tink.backend.insights.utils.ListFilterUtils;
import se.tink.libraries.date.Period;

public interface TransactionQueryService {

    Ordering<InsightTransaction> TRANSACTIONS_ORDERING = new Ordering<InsightTransaction>() {
        @Override
        public int compare(InsightTransaction left, InsightTransaction right) {
            return Doubles.compare(Math.abs(right.getAmount()),
                    Math.abs(left.getAmount()));
        }
    };

    List<InsightTransaction> findAllByUserId(UserId userId);

    Integer getTransactionsCount(UserId userId);

    List<InsightTransaction> findForCurrentAndPreviousPeriodByUserId(UserId userId);

    default <T> ImmutableList<InsightTransaction> filterTransactions(List<InsightTransaction> transactions,
            Function<InsightTransaction, T> transactionFunction, T requirement,
            BiFunction<T, T, Boolean> filterFunction) {
        return ListFilterUtils.filterObjectList(transactions, transactionFunction, requirement, filterFunction);
    }

    default Optional<InsightTransaction> largestExpenseInPeriod(Iterable<InsightTransaction> transactions,
            Period period) {
        Iterable<InsightTransaction> periodTransactions = StreamSupport.stream(transactions.spliterator(), false)
                .filter(t -> (t.getDate().getTime() >= period.getStartDate().getTime()
                        && t.getDate().getTime() <= period.getEndDate().getTime())).collect(Collectors.toList());

        // Expenses are of negative values. min returns largest expense
        if (Iterables.size(periodTransactions) == 0) {
            return Optional.empty();
        }
        return Optional.of(TRANSACTIONS_ORDERING.min(periodTransactions));
    }

    WeeklyTransactions findLastWeeksExpenseTransactions(UserId userId);

    MonthlyTransactions findPreviousMonthsExpenseTransactions(UserId userId);

    void findUpcomingExpenses(UserId userId); // TODO

    List<InsightTransaction> getInsightTransactionForPeriodByUserId(UserId userId);
}
