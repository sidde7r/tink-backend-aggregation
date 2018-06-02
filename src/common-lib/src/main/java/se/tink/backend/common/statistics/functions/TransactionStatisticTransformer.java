package se.tink.backend.common.statistics.functions;

import com.google.common.collect.ImmutableList;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import se.tink.backend.core.Account;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.Transaction;

/**
 * Mapper that transforms the transaction into statistic objects.
 */
public class TransactionStatisticTransformer implements Function<Transaction, List<Statistic>> {
    private Function<Transaction, Statistic> transformationFunction;
    private Function<Date, String> periodFunction;
    private ResolutionTypes resolution;
    private String type;
    private Map<String, Account> accountsById;
    private boolean disregardAccountOwnership;
    private String userId;

    public TransactionStatisticTransformer(Map<String, Account> accountsById, String type,
            ResolutionTypes resolution, Function<Date, String> periodFunction,
            Function<Transaction, Statistic> transformationFunction, boolean disregardAccountOwnership, String userId) {

        this.type = type;
        this.resolution = resolution;
        this.periodFunction = periodFunction;
        this.transformationFunction = transformationFunction;
        this.disregardAccountOwnership = disregardAccountOwnership;
        this.accountsById = accountsById;
        this.userId = userId;
    }

    /**
     * This will transform one transaction into several statistics objects. The current implementation only returns one
     * statistic object for each transaction but this can be changed later when transaction parts can have their own
     * category.
     */
    @Override
    public List<Statistic> apply(Transaction transaction) {

        // Dispensable amount will fallback to amount if dispensable amount isn't set
        BigDecimal amount = transaction.getDispensableAmount();

        // No need to generate statistics if amount is zero
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            return Collections.emptyList();
        }

        Account account = accountsById.get(transaction.getAccountId());

        final double ownership = (!disregardAccountOwnership && account != null) ? account.getOwnership() : 1;

        Statistic statistic = transformationFunction.apply(transaction);
        statistic.setType(type);
        statistic.setUserId(userId);
        statistic.setResolution(resolution);
        statistic.setPeriod(periodFunction.apply(transaction.getDate()));
        statistic.setValue(amount.doubleValue() * ownership);

        return ImmutableList.of(statistic);
    }
}
