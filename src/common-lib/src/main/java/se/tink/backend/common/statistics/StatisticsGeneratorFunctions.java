package se.tink.backend.common.statistics;

import com.google.common.base.Objects;
import java.util.Collection;
import java.util.Date;
import java.util.function.Function;
import se.tink.backend.common.statistics.functions.StatisticsCounting;
import se.tink.backend.common.statistics.functions.StatisticsSumming;
import se.tink.backend.common.statistics.functions.TransactionStatisticsTransformationFunction;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.Transaction;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class StatisticsGeneratorFunctions {
    static final Function<Collection<Statistic>, Collection<Statistic>> STATISTICS_COUNT_FUNCTION = StatisticsCounting::count;

    static final Function<Statistic, Integer> STATISTICS_DAY_OF_MONTH_GROUP_FUNCTION = s -> 0;

    public static final Function<Statistic, Integer> STATISTICS_GROUP_FUNCTION = Statistic::hashCode;

    static final Function<Statistic, String> STATISTICS_DESCRIPTION_FUNCTION = Statistic::getDescription;

    public static final Function<Statistic, Integer> STATISTICS_PERIOD_GROUP_FUNCTION = s -> Objects
            .hashCode(s.getPeriod());

    public static final Function<Collection<Statistic>, Collection<Statistic>> STATISTICS_SUM_FUNCTION =
            StatisticsSumming::sum;

    public static final Function<Transaction, Statistic> TRANSACTION_CATEGORY_FUNCTION = new TransactionStatisticsTransformationFunction() {
        @Override
        public String description(Transaction t) {
            return (t.getCategoryId());
        }
    };

    public static final Function<Transaction, Statistic> TRANSACTION_CATEGORY_TYPE_FUNCTION = new TransactionStatisticsTransformationFunction() {
        @Override
        public String description(Transaction t) {
            return t.getCategoryType().name();
        }
    };

    static final Function<Transaction, Statistic> TRANSACTION_DATE_FUNCTION = new TransactionStatisticsTransformationFunction() {
        @Override
        public String description(Transaction t) {
            return ThreadSafeDateFormat.FORMATTER_DAILY.format(t.getDate());
        }
    };

    static final Function<Transaction, Statistic> TRANSACTION_NET_INCOME_FUNCTION = new TransactionStatisticsTransformationFunction() {
        @Override
        public String description(Transaction t) {
            return Statistic.Types.INCOME_NET;
        }
    };

    public static final Function<Date, String> DAILY_PERIODIZATION_FUNCTION =
            ThreadSafeDateFormat.FORMATTER_DAILY::format;

    public static final Function<Date, String> MONTHLY_PERIODIZATION_FUNCTION =
            ThreadSafeDateFormat.FORMATTER_MONTHLY::format;

    public static final Function<Date, String> YEARLY_PERIODIZATION_FUNCTION =
            ThreadSafeDateFormat.FORMATTER_YEARLY::format;
}
