package se.tink.backend.common.workers.activity.generators;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.FollowUtils;
import se.tink.backend.common.workers.activity.ActivityGenerator;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.common.workers.activity.generators.models.FollowActivityData;
import se.tink.backend.core.Activity;
import se.tink.backend.core.UserProfile;
import se.tink.libraries.date.Period;
import se.tink.backend.core.StringDoublePair;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.follow.FollowCriteria;
import se.tink.backend.core.follow.FollowItem;
import se.tink.backend.core.follow.FollowTypes;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class FollowActivityGenerator extends ActivityGenerator {

    // Only generate follow activities for the specified amount of periods.
    private static final int GENERATE_FOR_RECENT_PERIODS = 6;

    public FollowActivityGenerator(DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(FollowActivityGenerator.class, 40, 80, deepLinkBuilderFactory);
    }

    @Override
    public void generateActivity(ActivityGeneratorContext context) {
        User user = context.getUser();

        // Follow items of `EXPENSES` and `SEARCH` type.
        ImmutableList<FollowItem> followItems = ImmutableList.copyOf(Iterables.filter(context.getFollowItems(),
                f -> (Objects.equal(f.getType(), FollowTypes.EXPENSES) || Objects.equal(f.getType(),
                        FollowTypes.SEARCH) && f.getFollowCriteria().getTargetAmount() != null)));

        if (followItems.isEmpty()) {
            return;
        }

        Map<String, List<Transaction>> transactionsBySearchFollowItemId = context.getTransactionsBySearchFollowItemId();

        List<Period> cleanPeriods = DateUtils.getCleanPeriods(context.getUserState().getPeriods());

        Iterable<Period> recentCleanPeriods = cleanPeriods.stream()
                .sorted(Comparator.comparing(Period::getName).reversed())
                .limit(GENERATE_FOR_RECENT_PERIODS).collect(Collectors.toList());

        for (Period period : recentCleanPeriods) {

            List<FollowItem> periodFollowItems = FollowUtils.cloneFollowItems(followItems);

            FollowUtils.populateFollowItems(
                    periodFollowItems,
                    period.getName(),
                    period.getName(),
                    period.getEndDate(),
                    false, // Include historical amounts. Only used by `FollowTypes.SAVINGS`, which is not included.
                    false, // Include transactions
                    false, // Suggest
                    user,
                    context.getTransactions(),
                    transactionsBySearchFollowItemId,
                    context.getAccounts(),
                    null, // Only used by `FollowTypes.SAVINGS`, which is not included.
                    context.getCategories(),
                    context.getCategoryConfiguration());

            for (FollowItem followItem : periodFollowItems) {

                FollowCriteria followCriteria = followItem.getFollowCriteria();
                Double progress = followItem.getProgress();

                if (progress == null || progress < 0.75) {
                    continue;
                }

                Date overdraftDate = null;
                Date warningDate = null;

                for (StringDoublePair periodAmount : followItem.getData().getPeriodAmounts()) {
                    if (periodAmount.getValue() < followCriteria.getTargetAmount()) {
                        try {
                            overdraftDate = ThreadSafeDateFormat.FORMATTER_DAILY.parse(periodAmount.getKey());
                        } catch (ParseException e) {
                        }

                        break;
                    }

                    if (periodAmount.getValue() < (followCriteria.getTargetAmount() * 0.75)) {
                        try {
                            warningDate = ThreadSafeDateFormat.FORMATTER_DAILY.parse(periodAmount.getKey());
                        } catch (ParseException e) {
                        }

                        break;
                    }
                }

                if (overdraftDate != null) {
                    generateFollowActivity(context, followItem, period.getName(), overdraftDate);
                }

                if (warningDate == null) {
                    continue;
                }

                if (overdraftDate == null || DateUtils.getNumberOfDaysBetween(warningDate, overdraftDate) > 2) {
                    generateFollowActivity(context, followItem, period.getName(), warningDate);
                }
            }
        }
    }

    private void generateFollowActivity(ActivityGeneratorContext context, FollowItem followItem, String period,
            Date overdraftDate) {
        overdraftDate = DateUtils.setInclusiveEndTime(overdraftDate);

        FollowItem activityFollowItem = followItem.clone();

        FollowUtils.populateFollowItem(
                activityFollowItem,
                period,
                period,
                overdraftDate,
                true,  // Include historical amounts
                false, // Include transactions
                false, // Suggest
                context.getUser(),
                context.getTransactions(),
                context.getTransactionsBySearchFollowItemId(),
                context.getAccounts(),
                context.getStatistics(),
                context.getCategories(),
                context.getCategoryConfiguration());

        String message = null;

        Double activityProgress = activityFollowItem.getProgress();

        if (activityProgress == null) {
            return;
        }

        double importance = calculateImportance(activityFollowItem.getProgress(), 2);

        String currentPeriod = UserProfile.ProfileDateUtils.getCurrentMonthPeriod(context.getUser().getProfile());

        if (Objects.equal(period, currentPeriod)) {
            if (activityProgress > 1) {
                message = Catalog.format(
                        context.getCatalog().getString("You have exceeded your goal for {0} this month."),
                        activityFollowItem.getName());
            } else {
                message = Catalog.format(
                        context.getCatalog().getString("You are close to exceeding your goal for {0} this month."),
                        activityFollowItem.getName());
            }
        } else {
            String date = new ThreadSafeDateFormat(context.getCatalog().getString("MMMM d"), context.getLocale())
                    .format(overdraftDate);

            if (activityProgress > 1) {
                message = Catalog.format(
                        context.getCatalog().getString("You exceeded your goal for {0} on {1}"),
                        activityFollowItem.getName(), date);
            } else {
                message = Catalog.format(
                        context.getCatalog().getString("You were close to exceeding your goal for {0} on {1}"),
                        activityFollowItem.getName(), date);
            }
        }

        String status;
        String title;

        if (activityProgress > 1) {
            status = "over";
            title = context.getCatalog().getString("Missed goal");
        } else {
            status = "warn";
            title = context.getCatalog().getString("Close to goal");
        }

        String key = String.format("%s.%s.%s.%s", Activity.Types.FOLLOW, period, followItem.getId(), status);

        String feedActivityIdentifier = StringUtils.hashAsStringSHA1(key);

        FollowActivityData activityData = new FollowActivityData();
        activityData.setFollowItem(activityFollowItem);

        context.addActivity(
                createActivity(
                        context.getUser().getId(),
                        overdraftDate,
                        String.format("%s/%s", Activity.Types.FOLLOW,
                                activityFollowItem.getType().name().toLowerCase()),
                        title,
                        message,
                        activityData,
                        key,
                        feedActivityIdentifier,
                        importance));
    }

    @Override
    public boolean isNotifiable() {
        return true;
    }
}
