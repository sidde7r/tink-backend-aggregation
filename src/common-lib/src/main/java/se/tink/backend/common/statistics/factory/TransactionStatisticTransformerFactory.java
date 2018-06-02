package se.tink.backend.common.statistics.factory;

import com.google.common.collect.Maps;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import se.tink.backend.common.statistics.functions.TransactionStatisticTransformer;
import se.tink.backend.core.Account;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.Transaction;

public class TransactionStatisticTransformerFactory {

    private final Map<String, Account> accountsById;
    private final PeriodFunctionFactory periodFunctionFactory;
    private final String userId;

    public TransactionStatisticTransformerFactory(List<Account> accounts, PeriodFunctionFactory periodFunctionFactory,
            String userId) {
        this.periodFunctionFactory = periodFunctionFactory;
        this.userId = userId;
        this.accountsById = Maps.uniqueIndex(accounts, Account::getId);
    }

    public TransactionStatisticTransformer createTransformer(String type, ResolutionTypes resolution,
            Function<Transaction, Statistic> transformationFunction, boolean disregardAccountOwnership) {

        Function<Date, String> periodFunction = periodFunctionFactory.getPeriodFunction(resolution);
        TransactionStatisticTransformer transformer = new TransactionStatisticTransformer(accountsById, type,
                resolution, periodFunction, transformationFunction, disregardAccountOwnership, userId);

        return transformer;
    }
}
