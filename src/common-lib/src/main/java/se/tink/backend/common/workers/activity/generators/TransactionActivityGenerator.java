package se.tink.backend.common.workers.activity.generators;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.workers.activity.ActivityGenerator;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.core.Activity;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Notification;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.core.User;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.i18n.Catalog;

public class TransactionActivityGenerator extends ActivityGenerator {
    protected static final double AMOUNT_NORMALIZATION_FACTOR = 200;
    protected static final long RECENT_TRANSACTION_CUTOFF = 7 * 24 * 60 * 60 * 1000;
    protected static final double RECENT_TRANSACTION_FACTOR = 4;
    
    private static final boolean GENERATE_FOR_RECENT_DAYS_ONLY = true; // Only generate for recent transactions.
    private static final int RECENT_DAYS_CUTOFF = 42; // 6 weeks required for ABN AMRO (Tink and Cornwall need less).
    private DeepLinkBuilderFactory deepLinkBuilderFactory;

    protected static double calculateTransactionImportance(Transaction transaction, Date now) {
        if (now.getTime() - transaction.getDate().getTime() <= RECENT_TRANSACTION_CUTOFF) {
            return Math.abs(transaction.getAmount()) * RECENT_TRANSACTION_FACTOR;
        } else {
            return Math.abs(transaction.getAmount());
        }
    }

    public TransactionActivityGenerator(DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(TransactionActivityGenerator.class, 10, 50, deepLinkBuilderFactory);
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;

        // Don't show these in "Tink 2.0".
        maxIosVersion = "2.4.9999";
        maxAndroidVersion = "2.4.9999";
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<Notification> createNotifications(Activity activity, ActivityGeneratorContext context) {
        List<Transaction> transactions;

        if (activity.getType().equals(Activity.Types.TRANSACTION)) {
            transactions = Lists.newArrayList((Transaction) activity.getContent());
        } else {
            transactions = (List<Transaction>) activity.getContent();
        }

        List<Notification> notifications = Lists.newArrayList();

        for (Transaction transaction : transactions) {

            String key = String.format("%s.%s.%s", Activity.Types.TRANSACTION,
                    ThreadSafeDateFormat.FORMATTER_DAILY.format(transaction.getDate()), transaction.getId());

            Notification.Builder notification = new Notification.Builder()
                    .userId(transaction.getUserId())
                    .title(context.getCatalog().getString("Expense"))
                    .date(transaction.getDate())
                    .message(generateMessage(
                            context.getActivitiesConfiguration().shouldGenerateSensitiveMessage(),
                            context.getUser(), context.getCatalog(), transaction, context.getCluster()))
                    .sensitiveMessage(generateSensitiveMessage(context.getUser(), context.getCatalog(), transaction))
                    .key(key)
                    .type(Activity.Types.TRANSACTION)
                    .groupable(true);

            if (context.getCluster() == Cluster.CORNWALL) {
                // Add the verification number for Cornwall.
                notification.url(String.format("%saccount/%s/transaction/%s?externalId=%s",
                        deepLinkBuilderFactory.getPrefix(),
                        transaction.getAccountId(), transaction.getId(),
                        transaction.getPayloadValue(TransactionPayloadTypes.EXTERNAL_ID)));
            } else {
                notification.url(deepLinkBuilderFactory.transaction(transaction.getId()).build());
            }

            notifications.addAll(buildNotificationsSilentlyFailing(activity.getUserId(), notification));
        }

        return notifications;
    }

    @Override
    public void generateActivity(ActivityGeneratorContext context) {
        double amountNormalizationFactor = context.getUserCurrency().getFactor() * AMOUNT_NORMALIZATION_FACTOR;

        Date today = DateUtils.getToday();
        
        Date cutoffDate;
        
        if (GENERATE_FOR_RECENT_DAYS_ONLY) {
            cutoffDate = DateUtils.addDays(today, -RECENT_DAYS_CUTOFF);
        }

        for (final Transaction transaction : context.getUnusedTransactions()) {
            
            if (Objects.equals(CategoryTypes.TRANSFERS, transaction.getCategoryType())) {
                continue;
            }
            
            // If a cutoff date is defined, don't generate activities for transactions before that.
            if (cutoffDate != null && cutoffDate.after(transaction.getDate())) {
                continue;
            }

            StringBuilder feedActivityIdentifierBuilder = new StringBuilder();

            feedActivityIdentifierBuilder.append(transaction.getId());
            feedActivityIdentifierBuilder.append(transaction.getDate());

            String feedActivityIdentifier = StringUtils.hashAsStringSHA1(feedActivityIdentifierBuilder.toString());

            context.addActivity(
                    createActivity(
                            context.getUser().getId(),
                            transaction.getDate(),
                            Activity.Types.TRANSACTION,
                            context.getCatalog().getString("Expense"),
                            transaction.getDescription(),
                            transaction,
                            transaction.getId() + transaction.getCategoryId(),
                            feedActivityIdentifier,
                            calculateImportance(calculateTransactionImportance(transaction, today),
                                    amountNormalizationFactor)), transaction);
        }
    }

    @Override
    public List<Activity> groupActivities(ActivityGeneratorContext context, List<Activity> activities) {
        if (activities.size() == 1) {
            return activities;
        }

        Date today = DateUtils.getToday();

        List<Transaction> transactions = Lists.newArrayList(Iterables.transform(activities,
                a -> (Transaction) a.getContent()));

        Iterable<String> descriptions = Iterables.transform(transactions, Transaction::getDescription);

        double amount = 0;

        StringBuilder keyBuilder = new StringBuilder(Activity.Types.TRANSACTION_MULTIPLE);
        StringBuilder feedActivityIdentifierBuilder = new StringBuilder();

        for (Transaction transaction : transactions) {
            amount += calculateTransactionImportance(transaction, today);
            keyBuilder.append(".");
            keyBuilder.append(transaction.getId());
            keyBuilder.append(transaction.getCategoryId());

            feedActivityIdentifierBuilder.append(transaction.getId());
            feedActivityIdentifierBuilder.append(transaction.getDate());
        }

        String feedActivityIdentifier = StringUtils.hashAsStringSHA1(feedActivityIdentifierBuilder.toString());

        return Lists.newArrayList(
                createActivity(
                        context.getUser().getId(),
                        activities.get(0).getDate(),
                        Activity.Types.TRANSACTION_MULTIPLE,
                        context.getCatalog().getString("Expenses"),
                        formatMultipleTitles(descriptions, context.getCatalog()),
                        transactions,
                        keyBuilder.toString(),
                        feedActivityIdentifier,
                        calculateImportance(Math.abs(amount), AMOUNT_NORMALIZATION_FACTOR)));
    }

    private static String generateMessage(boolean shouldGenerateSensitiveMessage, User user, Catalog catalog,
            Transaction transaction, Cluster cluster) {
        if (shouldGenerateSensitiveMessage) {
            return getDetailedMessage(catalog, transaction);
        } else {
            return catalog.getString("You've made a purchase.");
        }
    }

    private static String generateSensitiveMessage(User user, Catalog catalog, Transaction transaction) {
        if (user.getFlags().contains(FeatureFlags.ABN_AMRO_DETAILED_PUSH_NOTIFICATIONS)) {
            return getDetailedMessage(catalog, transaction);
        } else {
            return null;
        }
    }

    private static String getDetailedMessage(Catalog catalog, Transaction transaction) {
        return Catalog.format(catalog.getString("You had an expense charged by {0}"), transaction.getDescription());
    }

    @Override
    public boolean isNotifiable() {
        return true;
    }
}
