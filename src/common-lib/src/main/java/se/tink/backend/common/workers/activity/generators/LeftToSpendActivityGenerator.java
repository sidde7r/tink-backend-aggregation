package se.tink.backend.common.workers.activity.generators;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import java.text.ParseException;
import java.util.List;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.workers.activity.ActivityGenerator;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.common.workers.activity.generators.models.LeftToSpendActivityData;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.UserProfile;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class LeftToSpendActivityGenerator extends ActivityGenerator {
    private static final LogUtils log = new LogUtils(LeftToSpendActivityGenerator.class);
    private static final int PERCENTAGE_THRESHOLD = 5;
    private static final int REWARN_MULTIPLE_THRESHOLD = 3;

    private static final Ordering<Statistic> STATISTCS_ORDERING_VALUE = new Ordering<Statistic>() {
        @Override
        public int compare(Statistic left, Statistic right) {
            return ComparisonChain.start().compare(left.getValue(), right.getValue()).result();
        }
    };

    public LeftToSpendActivityGenerator(DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(LeftToSpendActivityGenerator.class, 40, 80, deepLinkBuilderFactory);
    }

    @Override
    public void generateActivity(ActivityGeneratorContext context) {
        final UserProfile userProfile = context.getUser().getProfile();

        ImmutableListMultimap<String, Statistic> statisticsByPeriod = Multimaps.index(
                Iterables.filter(context.getStatistics(),
                        s -> (s.getType().equals(Statistic.Types.LEFT_TO_SPEND) && s.getResolution() == userProfile
                                .getPeriodMode())), s -> (s.getPeriod()));

        ImmutableListMultimap<String, Statistic> statisticsAveragesByPeriod = Multimaps.index(
                Iterables.filter(context.getStatistics(),
                        s -> (s.getType().equals(Statistic.Types.LEFT_TO_SPEND_AVERAGE)
                                && s.getResolution() == userProfile
                                .getPeriodMode())), s -> (s.getPeriod()));

        // Go through each period and generate activity when passing the average curve

        for (String period : statisticsByPeriod.keySet()) {
            List<Statistic> statisticsData = Lists.newArrayList();

            boolean warn = false;
            Boolean isLastAboveAverage = null;
            int index = 0;
            double lastWarnLevel = 0;

            List<Statistic> averageStats = statisticsAveragesByPeriod.get(period);

            if (averageStats == null || averageStats.size() == 0) {
                continue;
            }

            Statistic averageMaxStat = STATISTCS_ORDERING_VALUE.max(averageStats);
            double averageMaxStatValue = averageMaxStat.getValue();

            for (final Statistic s : statisticsByPeriod.get(period)) {
                statisticsData.add(s);

                if (averageStats.size() <= index) {
                    continue;
                }

                Double amount = s.getValue();
                boolean rewarning = false;

                Statistic averageStat = averageStats.get(index);
                double averageStatValue = averageStat.getValue();

                boolean isAboveAverage = amount > averageStatValue;
                double difference = amount - averageStatValue;
                double level = (Math.abs(difference) / averageMaxStatValue) * 100;

                if ((isLastAboveAverage == null || isLastAboveAverage != isAboveAverage)
                        && level > PERCENTAGE_THRESHOLD) {
                    warn = true;
                } else if (lastWarnLevel != 0 && isLastAboveAverage == isAboveAverage
                        && level / lastWarnLevel > REWARN_MULTIPLE_THRESHOLD) {
                    warn = true;
                    rewarning = true;
                } else {
                    warn = false;
                }

                // don't warn if less than 3 point in period

                if (statisticsData.size() < 3) {
                    warn = false;
                }

                index++;
                isLastAboveAverage = isAboveAverage;

                if (warn) {
                    lastWarnLevel = level;
                    String key = String.format("%s.%s.%s", Activity.Types.LEFT_TO_SPEND, s.getDescription(),
                            period);

                    String feedActivityIdentifier = StringUtils.hashAsStringSHA1(key);

                    String message = context.getCatalog().getString(
                            "You are above average spending at this time of month");

                    if (isAboveAverage && rewarning) {
                        message = context.getCatalog().getString(
                                "You are still above average spending at this time of month");
                    }

                    if (!isAboveAverage && !rewarning) {
                        message = context.getCatalog()
                                .getString("You are below average spending at this time of month");
                    }

                    if (!isAboveAverage && rewarning) {
                        message = context.getCatalog().getString(
                                "You are still below average spending at this time of month");
                    }

                    LeftToSpendActivityData content = new LeftToSpendActivityData();
                    content.setLeftToSpend(Lists.newArrayList(statisticsData));
                    content.setLeftToSpendAverage(statisticsAveragesByPeriod.get(period));
                    content.setDifference(difference);

                    // generate activity

                    try {
                        context.addActivity(
                                createActivity(
                                        context.getUser().getId(),
                                        ThreadSafeDateFormat.FORMATTER_DAILY.parse(s.getDescription()),
                                        Activity.Types.LEFT_TO_SPEND,
                                        context.getCatalog().getString("Left to Spend"),
                                        message,
                                        content,
                                        key,
                                        feedActivityIdentifier));
                    } catch (ParseException e) {
                        log.error(
                                context.getUser().getId(),
                                "Could not create activity when generating left-to-spend activty for "
                                        + s.getDescription(), e);
                    }
                }
            }
        }
    }

    @Override
    public boolean isNotifiable() {
        return true;
    }
}
