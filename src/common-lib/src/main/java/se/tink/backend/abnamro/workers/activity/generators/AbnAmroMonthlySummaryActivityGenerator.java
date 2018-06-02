package se.tink.backend.abnamro.workers.activity.generators;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import org.joda.time.DateTime;
import se.tink.backend.abnamro.workers.activity.generators.models.AbnAmroMonthlySummaryActivityData;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.FollowUtils;
import se.tink.backend.common.workers.activity.ActivityGenerator;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.common.workers.activity.generators.models.FollowActivityFeedbackData;
import se.tink.backend.core.Activity;
import se.tink.backend.core.CategoryTypes;
import se.tink.libraries.date.Period;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.follow.FollowItem;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.backend.utils.guavaimpl.Predicates;

public class AbnAmroMonthlySummaryActivityGenerator extends ActivityGenerator {

    private static final Joiner FOLLOW_ITEM_KEY_JOINER = Joiner.on(';');

    public AbnAmroMonthlySummaryActivityGenerator(DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(AbnAmroMonthlySummaryActivityGenerator.class, 90, 90, deepLinkBuilderFactory);
    }

    @Override
    public void generateActivity(ActivityGeneratorContext context) {

        final Date today = DateUtils.getToday();
        final ThreadSafeDateFormat monthNameFormat = ThreadSafeDateFormat.FORMATTER_MONTH_NAME.toBuilder()
                .setLocale(context.getLocale()).build();

        final ResolutionTypes periodMode = context.getUser().getProfile().getPeriodMode();

        final Iterable<Statistic> incomeAndExpensesStatistics = Iterables.filter(context.getStatistics(),
                Predicates.statisticForTypeAndResolution(Statistic.Types.INCOME_AND_EXPENSES, periodMode));

        final ImmutableMap<String, Statistic> expensesByPeriod = Maps.uniqueIndex(
                Iterables.filter(incomeAndExpensesStatistics,
                        Predicates.statisticForDescription(CategoryTypes.EXPENSES.toString())),
                Statistic::getPeriod);

        final ImmutableMap<String, Statistic> incomeByPeriod = Maps.uniqueIndex(
                Iterables.filter(incomeAndExpensesStatistics,
                        Predicates.statisticForDescription(CategoryTypes.INCOME.toString())),
                Statistic::getPeriod);

        final List<Statistic> transactionCountStatistics = Lists.newArrayList(Iterables.filter(context.getStatistics(),
                Predicates.statisticForTypeAndResolution(Statistic.Types.INCOME_AND_EXPENSES_COUNT, periodMode)));

        final ImmutableMap<String, Statistic> expenseCountByPeriod = Maps.uniqueIndex(
                Iterables.filter(transactionCountStatistics,
                        Predicates.statisticForDescription(CategoryTypes.EXPENSES.toString())),
                Statistic::getPeriod);

        final ImmutableMap<String, Statistic> incomeCountByPeriod = Maps.uniqueIndex(
                Iterables.filter(transactionCountStatistics,
                        Predicates.statisticForDescription(CategoryTypes.INCOME.toString())),
                Statistic::getPeriod);

        final ImmutableListMultimap<String, Statistic> leftToSpendStatisticsByPeriod = Multimaps.index(
                Iterables.filter(context.getStatistics(),
                        Predicates.statisticForTypeAndResolution(Statistic.Types.LEFT_TO_SPEND, periodMode)),
                Statistic::getPeriod);

        for (Period period : context.getUserState().getPeriods()) {

            final Date periodEndDate = period.getEndDate();

            // If period end date is in the future, the month isn't over yet.
            if (periodEndDate == null || periodEndDate.after(today)) {
                continue;
            }

            final String periodName = period.getName();
            final String previousPeriodName = DateUtils.getPreviousMonthPeriod(periodName);

            String monthName = monthNameFormat.format(DateTime.parse(periodName).toDate());

            Statistic expenses = expensesByPeriod.get(periodName);
            Statistic expenseCount = expenseCountByPeriod.get(periodName);
            Statistic expensesPreviousPeriod = expensesByPeriod.get(previousPeriodName);

            Statistic income = incomeByPeriod.get(periodName);
            Statistic incomeCount = incomeCountByPeriod.get(periodName);
            Statistic incomePreviousPeriod = incomeByPeriod.get(previousPeriodName);

            List<Statistic> leftToSpend = leftToSpendStatisticsByPeriod.get(periodName);
            List<Statistic> leftToSpendPreviousPeriod = leftToSpendStatisticsByPeriod.get(previousPeriodName);

            Statistic netResult = null;
            if (leftToSpend != null && !leftToSpend.isEmpty()) {
                netResult = leftToSpend.stream().max(Comparator
                        .comparing(s -> DateUtils.parseDate(s.getDescription()))).get();
            }

            Statistic netResultPreviousPeriod = null;
            if (leftToSpendPreviousPeriod != null && !leftToSpendPreviousPeriod.isEmpty()) {
                netResultPreviousPeriod = leftToSpendPreviousPeriod.stream()
                        .max(Comparator
                                .comparing(s -> DateUtils.parseDate(s.getDescription()))).get();
            }

            AbnAmroMonthlySummaryActivityData data = new AbnAmroMonthlySummaryActivityData();

            data.setExpenseAmount(getAmount(expenses));
            data.setExpenseChange(-getChange(expensesPreviousPeriod, expenses));
            data.setExpenseCount(getCount(expenseCount));

            data.setIncomeAmount(getAmount(income));
            data.setIncomeChange(getChange(incomePreviousPeriod, income));
            data.setIncomeCount(getCount(incomeCount));

            data.setLeftToSpend(leftToSpend);
            data.setLeftToSpendAmount(getAmount(netResult));
            data.setLeftToSpendChange(getChange(netResultPreviousPeriod, netResult));

            data.setFollowFeedback(getFollowFeedbackData(context, period));

            data.setPeriod(period);

            String title = Catalog.format(context.getCatalog().getString("{0} overview"),
                    StringUtils.firstLetterUppercaseFormatting(monthName));

            String message = context.getCatalog().getString("Check it out!");

            // Construct key. This needs to be done _after_ `data` has been populated.
            String notificationKey = createKey(data, period);
            String feedActivityIdentifier = StringUtils.hashAsStringSHA1(notificationKey);

            context.addActivity(
                    createActivity(
                            context.getUser().getId(),
                            periodEndDate,
                            Activity.Types.MONTHLY_SUMMARY_ABNAMRO,
                            title,
                            message,
                            data,
                            notificationKey,
                            feedActivityIdentifier));
        }
    }

    private static String createKey(AbnAmroMonthlySummaryActivityData data, Period period) {
        String notificationKey = String.format("%s.%s", Activity.Types.MONTHLY_SUMMARY_ABNAMRO, period.getName());

        // Add start date and end date to the key if the period is not `MONTHLY` ("calendar month").
        if (!Objects.equal(ResolutionTypes.MONTHLY, period.getResolution())) {
            ThreadSafeDateFormat dateFormatter = ThreadSafeDateFormat.FORMATTER_INTEGER_DATE;
            notificationKey += String.format(".%s-%s", dateFormatter.format(period.getStartDate()),
                    dateFormatter.format(period.getEndDate()));
        }

        // Add follow item state to the key.
        if (data.getFollowFeedback() != null) {
            List<String> followItemKeys = Lists.newArrayList();

            // Get id:s and last modified date for all follow items.
            for (FollowItem item : data.getFollowFeedback().getFollowItems()) {
                followItemKeys.add(String.format("%s,%d", item.getId(), item.getLastModified().getTime()));
            }

            Collections.sort(followItemKeys);

            String followItemsKey = StringUtils.hashAsStringSHA1(FOLLOW_ITEM_KEY_JOINER.join(followItemKeys));

            notificationKey += String.format(".FOLLOW_ITEMS-%s", followItemsKey);
        }

        return notificationKey;
    }

    private static double getAmount(Statistic statistic) {
        if (statistic == null) {
            return 0;
        } else {
            return statistic.getValue();
        }
    }

    private static double getChange(Statistic previous, Statistic current) {
        return getAmount(current) - getAmount(previous);
    }

    private static int getCount(Statistic statistic) {
        if (statistic == null) {
            return 0;
        } else {
            return (int) statistic.getValue();
        }
    }

    private static String getFollowFeedback(ActivityGeneratorContext context, Period period, int numberOfFollowItems,
            int numberOfPositiveFollowItems) {

        ThreadSafeDateFormat monthNameFormat = ThreadSafeDateFormat.FORMATTER_MONTH_NAME.toBuilder()
                .setLocale(context.getLocale()).build();
        String monthName = monthNameFormat.format(DateTime.parse(period.getName()).toDate());

        if (numberOfPositiveFollowItems == numberOfFollowItems) {
            return Catalog.format(
                    context.getCatalog().getPluralString(
                            "You kept your goal in {0}. Great job!",
                            "You kept all your goals in {0}. Great job!",
                            numberOfFollowItems),
                    monthName);
        } else if (numberOfPositiveFollowItems > 0) {
            double ratioOfPassedBudgets = (double) numberOfPositiveFollowItems / (double) numberOfFollowItems;

            String establishment = Catalog.format(context.getCatalog().getString("You kept {0} of your goals in {1}."),
                    numberOfPositiveFollowItems, monthName);

            String assessment;
            if (ratioOfPassedBudgets > 0.80) {
                assessment = context.getCatalog().getString("Great job!");
            } else if (ratioOfPassedBudgets > 0.5) {
                assessment = context.getCatalog().getString("Good job!");
            } else {
                assessment = context.getCatalog().getString("Come on!");
            }

            return Catalog.format("{0} {1}", establishment, assessment);
        } else {
            return Catalog.format(
                    context.getCatalog().getPluralString(
                            "You did not keep your goal in {0}. Better luck next month.",
                            "You did not keep a single goal in {0}. Better luck next month.",
                            numberOfFollowItems),
                    monthName);
        }
    }

    private static FollowActivityFeedbackData getFollowFeedbackData(ActivityGeneratorContext context, Period period) {

        if (context.getFollowItems() == null || context.getFollowItems().isEmpty()) {
            return null;
        }

        final List<FollowItem> followItems = Lists.newArrayList(Iterables.filter(context.getFollowItems(),
                f -> {

                    boolean validType;

                    switch (f.getType()) {
                    case EXPENSES:
                    case SEARCH:
                        validType = true;
                        break;
                    default:
                        validType = false;
                    }

                    boolean hasTargetAmount = (f.getFollowCriteria().getTargetAmount() != null);

                    return validType && hasTargetAmount;
                }));

        if (followItems == null || followItems.isEmpty()) {
            return null;
        }

        List<FollowItem> periodFollowItems = FollowUtils.cloneFollowItems(followItems);

        FollowUtils.populateFollowItems(
                periodFollowItems,
                period.getName(),
                period.getName(),
                period.getEndDate(),
                false, // Include historical amounts
                false, // Include transactions
                false, // Suggest
                context.getUser(),
                context.getTransactions(),
                context.getTransactionsBySearchFollowItemId(),
                context.getAccounts(),
                context.getStatistics(),
                context.getCategories(),
                context.getCategoryConfiguration());

        Iterable<FollowItem> positiveFollowItems = Iterables.filter(periodFollowItems, FollowItem::isProgressPositive);

        int numberOfFollowItems = Iterables.size(periodFollowItems);
        int numberOfPositiveFollowItems = Iterables.size(positiveFollowItems);
        String feedback = getFollowFeedback(context, period, numberOfFollowItems, numberOfPositiveFollowItems);

        FollowActivityFeedbackData followFeedback = new FollowActivityFeedbackData();
        followFeedback.setFeedbackTitle(feedback);
        followFeedback.setFollowItems(periodFollowItems);

        return followFeedback;
    }

    @Override
    public boolean isNotifiable() {
        return true;
    }
}
