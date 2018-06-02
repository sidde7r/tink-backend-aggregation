package se.tink.backend.common.statistics.functions;

import java.util.function.Function;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.Transaction;

public abstract class TransactionStatisticsTransformationFunction implements Function<Transaction, Statistic> {
    public abstract String description(Transaction t);

    @Override
    public Statistic apply(Transaction t) {
        Statistic s = new Statistic();

        s.setDescription(description(t));

        return s;
    }
}
