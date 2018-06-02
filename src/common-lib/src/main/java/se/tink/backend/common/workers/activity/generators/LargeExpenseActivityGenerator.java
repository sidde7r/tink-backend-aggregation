package se.tink.backend.common.workers.activity.generators;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.workers.activity.ActivityGenerator;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.core.Account;
import se.tink.backend.core.Activity;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Notification;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionTypes;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class LargeExpenseActivityGenerator extends ActivityGenerator {

    private static final LogUtils log = new LogUtils(LargeExpenseActivityGenerator.class);

    private static final int TOTAL_PERCENTILE_THRESHOLD = 99;
    private static final double CATEGORY_STANDARD_DEVIATION_THRESHOLD = 2;
    private static final double CATEGORY_MINIMUM_DEVIATION_THRESHOLD = 100;

    private final Ordering<Transaction> transactionDateOrdering = new Ordering<Transaction>() {
        @Override
        public int compare(Transaction left, Transaction right) {
            return ComparisonChain.start().compare(left.getDate(), right.getDate())
                    .compare(left.getId(), right.getId()).result();
        }
    };
    private DeepLinkBuilderFactory deepLinkBuilderFactory;

    public LargeExpenseActivityGenerator(DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(LargeExpenseActivityGenerator.class, 50, 70, deepLinkBuilderFactory);
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;
    }

    private boolean shouldShowTransactionDetails(Cluster cluster) {
        switch (cluster) {
        case CORNWALL:
        case ABNAMRO:
            return false;
        default:
            return true;
        }
    }

    @Override
    public void generateActivity(ActivityGeneratorContext context) {
        double categoryMinimumDeviationThreshold = context.getUserCurrency().getFactor()
                * CATEGORY_MINIMUM_DEVIATION_THRESHOLD;

        Map<String, DescriptiveStatistics> categoryStatistics = Maps.newHashMap();
        Map<String, DescriptiveStatistics> descriptionStatistics = Maps.newHashMap();
        Map<String, DescriptiveStatistics> merchantStatistics = Maps.newHashMap();
        DescriptiveStatistics totalStatistics = new DescriptiveStatistics(500);

        for (Transaction transaction : transactionDateOrdering.sortedCopy(context.getUnusedTransactions())) {
            if (transaction.getCategoryType() != CategoryTypes.EXPENSES || isTransfer(transaction)) {
                continue;
            }

            // Check there are enough statistics to proceed.

            if (totalStatistics.getN() < 100) {
                addTransactionToStatistics(categoryStatistics, descriptionStatistics, merchantStatistics,
                        totalStatistics, transaction);
                continue;
            }

            double threshold = totalStatistics.getPercentile(TOTAL_PERCENTILE_THRESHOLD);
            DescriptiveStatistics categoryStatistic = categoryStatistics.get(transaction.getCategoryId());

            DescriptiveStatistics descriptionStatistic = descriptionStatistics.get(getDescription(transaction
                    .getDescription()));
            DescriptiveStatistics merchantStatistic = merchantStatistics.get(transaction.getMerchantId());

            // Category Threshold is mean + maximum value of stdev and minimum threshold
            // If there is no category statistics, user total statistics.

            double categoryThreshold = totalStatistics.getMean() + totalStatistics.getStandardDeviation()
                    * CATEGORY_STANDARD_DEVIATION_THRESHOLD;

            // Set threshold as 2 stds from mean.

            if (categoryStatistic != null) {
                categoryThreshold = (categoryStatistic.getMean() + Math.max(
                        categoryStatistic.getStandardDeviation() * CATEGORY_STANDARD_DEVIATION_THRESHOLD,
                        categoryMinimumDeviationThreshold));
            }

            double descriptionThreshold = categoryThreshold;

            // Set threshold as max plus on std.

            if (descriptionStatistic != null) {
                descriptionThreshold = (descriptionStatistic.getMax() + Math.max(
                        descriptionStatistic.getStandardDeviation(),
                        categoryMinimumDeviationThreshold));
            }

            double merchantThreshold = categoryThreshold;

            // Set threshold as max plus on std.

            if (merchantStatistic != null) {
                merchantThreshold = (merchantStatistic.getMax() + Math.max(
                        merchantStatistic.getStandardDeviation(),
                        categoryMinimumDeviationThreshold));
            }

            double amount = Math.abs(transaction.getAmount());

            // Add transaction to statistics for next comparison.

            addTransactionToStatistics(categoryStatistics, descriptionStatistics, merchantStatistics, totalStatistics,
                    transaction);

            // Check if this transaction is unusual large.

            if (amount <= threshold || amount <= categoryThreshold || amount <= descriptionThreshold
                    || amount <= merchantThreshold) {
                continue;
            }

            String key = String.format("%s.%s.%s", Activity.Types.LARGE_EXPENSE,
                    ThreadSafeDateFormat.FORMATTER_DAILY.format(transaction.getDate()), transaction.getId());

            String feedActivityIdentifier = StringUtils.hashAsStringSHA1(key);

            context.addActivity(
                    createActivity(
                            context.getUser().getId(),
                            transaction.getDate(),
                            Activity.Types.LARGE_EXPENSE,
                            context.getCatalog().getString("Large expense"),
                            generateMessage(context, transaction),
                            transaction,
                            key,
                            feedActivityIdentifier,
                            calculateImportance(Math.abs(transaction.getAmount()), DEFAULT_AMOUNT_NORMALIZATION_FACTOR
                                    * context.getUserCurrency().getFactor())), transaction);
        }
    }

    private boolean isTransfer(Transaction transaction) {
        return transaction.getType() == TransactionTypes.TRANSFER;
    }

    private void addTransactionToStatistics(Map<String, DescriptiveStatistics> categoryStatistics,
            Map<String, DescriptiveStatistics> descriptionStatistics,
            Map<String, DescriptiveStatistics> merchantStatistics, DescriptiveStatistics totalStatistics,
            Transaction transaction) {

        totalStatistics.addValue(Math.abs(transaction.getAmount()));

        if (!categoryStatistics.containsKey(transaction.getCategoryId())) {
            categoryStatistics.put(transaction.getCategoryId(), new DescriptiveStatistics());
        }

        if (!descriptionStatistics.containsKey(getDescription(transaction.getDescription()))) {
            descriptionStatistics.put(getDescription(transaction.getDescription()), new DescriptiveStatistics());
        }

        categoryStatistics.get(transaction.getCategoryId()).addValue(Math.abs(transaction.getAmount()));
        descriptionStatistics.get(getDescription(transaction.getDescription())).addValue(
                Math.abs(transaction.getAmount()));

        if (transaction.getMerchantId() != null) {
            if (!merchantStatistics.containsKey(transaction.getMerchantId())) {
                merchantStatistics.put(transaction.getMerchantId(), new DescriptiveStatistics());
            }

            merchantStatistics.get(transaction.getMerchantId()).addValue(Math.abs(transaction.getAmount()));
        }
    }

    @Override
    public List<Activity> groupActivities(ActivityGeneratorContext context, List<Activity> activities) {
        if (activities.size() == 1) {
            return activities;
        }

        ListMultimap<String, Activity> activitiesByDate = Multimaps.index(activities,
                a -> ThreadSafeDateFormat.FORMATTER_DAILY.format(a.getDate()));

        List<Activity> grouped = Lists.newArrayList();

        for (String date : activitiesByDate.keySet()) {
            grouped.add(createGroupedActivity(context, activitiesByDate.get(date)));
        }

        return grouped;
    }

    private Activity createGroupedActivity(ActivityGeneratorContext context, List<Activity> activities) {
        List<Transaction> transactions = Lists.newArrayList(
                activities.stream().map(a -> (Transaction) a.getContent()).collect(Collectors.toList()));

        double amount = 0;

        for (Transaction transaction : transactions) {
            amount += Math.abs(transaction.getAmount());
        }

        StringBuilder feedActivityIdentifierBuilder = new StringBuilder(Activity.Types.LARGE_EXPENSE_MULTIPLE);

        for (Transaction transaction : transactions) {
            feedActivityIdentifierBuilder.append(transaction.getId());
            feedActivityIdentifierBuilder.append(transaction.getCategoryId());
            feedActivityIdentifierBuilder.append(transaction.getDate());
        }

        String feedActivityIdentifier = StringUtils.hashAsStringSHA1(feedActivityIdentifierBuilder.toString());
        String key = String.format("%s.%s.%s", Activity.Types.LARGE_EXPENSE_MULTIPLE,
                ThreadSafeDateFormat.FORMATTER_DAILY.format(transactions.get(0).getDate()), feedActivityIdentifier);

        return createActivity(
                context.getUser().getId(),
                activities.get(0).getDate(),
                Activity.Types.LARGE_EXPENSE_MULTIPLE,
                context.getCatalog().getString("Large expenses"),
                generateMessage(context, transactions),
                transactions,
                key,
                feedActivityIdentifier,
                calculateImportance(amount,
                        DEFAULT_AMOUNT_NORMALIZATION_FACTOR * context.getUserCurrency().getFactor()));
    }

    private String getDescription(String description) {
        String trimmedString = StringUtils.trimTrailingDigits(description);
        if (trimmedString.length() == 0) {
            return description.toLowerCase();
        } else {
            return trimmedString.toLowerCase();
        }
    }

    private String generateMessage(ActivityGeneratorContext context, Transaction transaction) {
        Cluster cluster = context.getServiceContext().getConfiguration().getCluster();
        final Catalog catalog = context.getCatalog();

        if (shouldShowTransactionDetails(cluster)) {
            return Catalog.format(catalog.getString("You were charged an unusually large expense by {0}"),
                    transaction.getDescription());
        } else {
            return catalog.getString("You were charged an unusually large expense.");
        }
    }

    private String generateMessage(ActivityGeneratorContext context, List<Transaction> transactions) {
        if (transactions.size() == 1) {
            return generateMessage(context, transactions.get(0));
        }

        Cluster cluster = context.getServiceContext().getConfiguration().getCluster();
        final Catalog catalog = context.getCatalog();

        if (shouldShowTransactionDetails(cluster)) {
            Iterable<String> descriptions = Iterables.transform(transactions, Transaction::getDescription);

            return Catalog.format(catalog.getString("You were charged some unusually large expenses by {0}"),
                    formatMultipleTitles(descriptions, catalog));
        } else {
            return Catalog.format(catalog.getString("You were charged {0} unusually large expenses."),
                    transactions.size());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<Notification> createNotifications(Activity activity, ActivityGeneratorContext context) {
        List<Transaction> transactions;

        if (activity.getType().equals(Activity.Types.LARGE_EXPENSE)) {
            transactions = Lists.newArrayList((Transaction) activity.getContent());
        } else {
            transactions = (List<Transaction>) activity.getContent();
        }

        if (transactions.size() == 0) {
            return Lists.newArrayList();
        }

        final Transaction transaction = transactions.get(0);

        Notification.Builder notification = new Notification.Builder()
                .fromActivity(activity)
                .groupable(true);

        if (context.getServiceContext().getConfiguration().getCluster() == Cluster.CORNWALL) {
            Optional<Account> account = context.getAccounts().stream()
                    .filter(a -> Objects.equals(a.getId(), transaction.getAccountId())).findFirst();

            if (account.isPresent()) {
                notification.url(String.format("%saccount/%s/transaction/%s", deepLinkBuilderFactory.getPrefix(),
                        account.get().getId(), transaction.getId()));
            } else {
                log.error(context.getUser().getId(),
                        String.format("Could not find Cornwall account with id %s when generating notification",
                                transaction.getAccountId()));
                notification.url(deepLinkBuilderFactory.getPrefix());
            }
        } else {
            notification.url(deepLinkBuilderFactory.transaction(transaction.getId()).build());
        }

        return buildNotificationsSilentlyFailing(activity.getUserId(), notification);
    }

    @Override
    public boolean isNotifiable() {
        return true;
    }
}
