package se.tink.backend.common.workers.activity.generators;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.backend.common.workers.activity.ActivityGenerator;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.common.workers.activity.generators.models.NetIncomeActivityData;
import se.tink.backend.core.Activity;
import se.tink.backend.core.KVPair;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.Statistic;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.utils.StringUtils;

public class NetIncomeActivityGenerator extends ActivityGenerator {
    private static final int MAX_NBR_PERIODS = 8;
    private static final int MIN_NBR_PERIODS = 3;
    private static final Ordering<Statistic> STATISTICS_ORDERING = new Ordering<Statistic>() {
        @Override
        public int compare(Statistic left, Statistic right) {
            return ComparisonChain.start().compare(left.getPeriod(), right.getPeriod()).result();
        }
    };

    private static final ImmutableList<Integer> THRESHOLDS = ImmutableList.of(500, 1000, 5000, 10000, 25000, 50000,
            10000);

    private int periodAdjustedDay;
    private ResolutionTypes periodMode;

    public NetIncomeActivityGenerator(DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(NetIncomeActivityGenerator.class, 80, 90, deepLinkBuilderFactory);
    }

    private void createActivity(ActivityGeneratorContext context, String period, Collection<Statistic> netPeriods) {
        Date date = DateUtils.getCurrentOrPreviousBusinessDay(DateUtils.getLastDateFromPeriod(period, periodMode,
                periodAdjustedDay));

        double totalNetIncome = 0;

        NetIncomeActivityData content = new NetIncomeActivityData();

        List<KVPair<String, Double>> dataPoints = Lists.newArrayList();

        for (Statistic statistic : netPeriods) {
            totalNetIncome += statistic.getValue();
            dataPoints.add(new KVPair<String, Double>(statistic.getPeriod(), statistic.getValue()));
        }

        content.setData(dataPoints);

        String key = String.format("%s.%s.%.2f", Activity.Types.NET_INCOME, period, totalNetIncome);
        
        String feedActivityIdentifier = StringUtils.hashAsStringSHA1(key);

        context.addActivity(
                createActivity(
                        context.getUser().getId(),
                        date,
                        Activity.Types.NET_INCOME,
                        context.getCatalog().getString("Left to Spend"),
                        Catalog.format(context.getCatalog().getString("You have saved {0} in {1} months!"),
                                I18NUtils.formatCurrency(totalNetIncome, context.getUserCurrency(), context.getLocale()),
                                netPeriods.size()),
                        content,
                        key,
                        feedActivityIdentifier));
    }

    @Override
    public void generateActivity(final ActivityGeneratorContext context) {
        Iterable<Integer> userThresholds = Iterables.transform(THRESHOLDS,
                i -> new Integer((int) (i * context.getUserCurrency().getFactor())));

        periodMode = context.getUser().getProfile().getPeriodMode();
        periodAdjustedDay = context.getUser().getProfile().getPeriodAdjustedDay();

        final String currentPeriod = DateUtils.getCurrentMonthPeriod(periodMode, periodAdjustedDay);

        // Fetch the net-income statistics for all months except the current one.

        List<Statistic> netStatistics = STATISTICS_ORDERING.sortedCopy(Iterables.filter(context.getStatistics(),
                s -> (s.getType().equals(Statistic.Types.INCOME_NET)
                        && s.getResolution() == context.getUser().getProfile().getPeriodMode() && !Objects
                        .equal(s.getPeriod(), currentPeriod))));

        // Trailing lists for the threshold calculations.

        DescriptiveStatistics trailingNetValues = new DescriptiveStatistics(MAX_NBR_PERIODS);
        CircularFifoQueue<Statistic> trailingNetStatistics = new CircularFifoQueue<Statistic>(MAX_NBR_PERIODS);

        boolean isFirstWarning = true;

        statisticsLoop: for (Statistic statistic : netStatistics) {
            double previousSum = trailingNetValues.getSum();

            double value = statistic.getValue();

            trailingNetStatistics.add(statistic);
            trailingNetValues.addValue(value);

            double currentSum = trailingNetValues.getSum();

            // If we're on a cumulative negative, just reset the lists and begin again.

            if (currentSum < 0) {
                trailingNetStatistics.clear();
                trailingNetValues.clear();
                continue;
            }

            // Require a certain number of periods.

            if (trailingNetStatistics.size() < MIN_NBR_PERIODS) {
                isFirstWarning = true;
                continue;
            }

            // check rolling accumulating values > 0

            Iterator<Statistic> it = trailingNetStatistics.iterator();
            double sum = 0;

            while (it.hasNext()) {
                Statistic s = it.next();
                sum += s.getValue();
                if (sum < 0) {
                    continue statisticsLoop;
                }
            }

            boolean shouldCreateActivity = false;

            // Loop through the thresholds to see if we've passed any of them and should generate the activity

            for (Integer userThreshold : userThresholds) {
                if ((isFirstWarning && currentSum > userThreshold && currentSum > previousSum)
                        || (previousSum < userThreshold && currentSum > userThreshold)) {
                    isFirstWarning = false;
                    shouldCreateActivity = true;
                    break;
                }
            }

            if (shouldCreateActivity) {
                createActivity(context, statistic.getPeriod(), trailingNetStatistics);
            }
        }
    }

    @Override
    public boolean isNotifiable() {
        return false;
    }
}
