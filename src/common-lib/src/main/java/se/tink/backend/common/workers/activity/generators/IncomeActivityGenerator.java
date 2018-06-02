package se.tink.backend.common.workers.activity.generators;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.List;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.workers.activity.ActivityGenerator;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.core.Activity;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.i18n.Catalog;

public class IncomeActivityGenerator extends ActivityGenerator {
    protected static final double MININUM_THRESHOLD = 0;

    public IncomeActivityGenerator(DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(IncomeActivityGenerator.class, 50, 70, deepLinkBuilderFactory);

        // Don't show these in "Tink 2.0".
        maxIosVersion = "2.4.9999";
        maxAndroidVersion = "2.4.9999";
    }

    @Override
    public void generateActivity(ActivityGeneratorContext context) {
        double minimumThreshold = context.getUserCurrency().getFactor() * MININUM_THRESHOLD;

        for (final Transaction transaction : context.getUnusedTransactions()) {
            if (transaction.getCategoryType() != CategoryTypes.INCOME) {
                continue;
            }

            if (transaction.getAmount() < minimumThreshold) {
                continue;
            }

            String key = String.format("%s.%s.%s", Activity.Types.INCOME,
                    ThreadSafeDateFormat.FORMATTER_DAILY.format(transaction.getDate()), transaction.getId());

            StringBuilder feedActivityIdentifierBuilder = new StringBuilder();

            feedActivityIdentifierBuilder.append(transaction.getId());
            feedActivityIdentifierBuilder.append(transaction.getDate());

            String feedActivityIdentifier = StringUtils.hashAsStringSHA1(feedActivityIdentifierBuilder.toString());

            context.addActivity(
                    createActivity(
                            context.getUser().getId(),
                            transaction.getDate(),
                            Activity.Types.INCOME,
                            context.getCatalog().getString("Income"),
                            generateMessage(context.getCatalog(), transaction,
                                    context.getActivitiesConfiguration().shouldGenerateSensitiveMessage()),
                            generateSensitiveMessage(context.getUser(), context.getCatalog(), transaction),
                            transaction,
                            key,
                            feedActivityIdentifier,
                            calculateImportance(Math.abs(transaction.getAmount()), DEFAULT_AMOUNT_NORMALIZATION_FACTOR
                                    * context.getUserCurrency().getFactor())), transaction);

        }
    }

    @Override
    public List<Activity> groupActivities(ActivityGeneratorContext context, List<Activity> activities) {
        if (activities.size() == 1) {
            return activities;
        }

        List<Transaction> transactions = Lists.newArrayList(Iterables.transform(activities,
                a -> (Transaction) a.getContent()));

        double amount = 0;

        for (Transaction transaction : transactions) {
            amount += transaction.getAmount();
        }

        String key = String.format("%s.%s.%s", Activity.Types.INCOME_MULTIPLE,
                ThreadSafeDateFormat.FORMATTER_DAILY.format(activities.get(0).getDate()),
                StringUtils.hashAsStringMD5("" + amount + transactions.size()));

        StringBuilder feedActivityIdentifierBuilder = new StringBuilder(Activity.Types.INCOME_MULTIPLE);
        for (Transaction transaction : transactions) {
            feedActivityIdentifierBuilder.append(transaction.getId());
            feedActivityIdentifierBuilder.append(transaction.getDate());
        }

        String feedActivityIdentifier = StringUtils.hashAsStringSHA1(feedActivityIdentifierBuilder.toString());

        return Lists.newArrayList(createActivity(
                context.getUser().getId(),
                activities.get(0).getDate(),
                Activity.Types.INCOME_MULTIPLE,
                context.getCatalog().getString("Income"),
                generateMessage(context.getCatalog(), transactions,
                        context.getActivitiesConfiguration().shouldGenerateSensitiveMessage()),
                generateSensitiveMessage(context.getUser(), context.getCatalog(), transactions),
                transactions,
                key,
                feedActivityIdentifier,
                calculateImportance(Math.abs(amount), DEFAULT_AMOUNT_NORMALIZATION_FACTOR
                        * context.getUserCurrency().getFactor())));
    }

    private static String generateMessage(Catalog catalog, Transaction transaction,
            boolean shouldGenerateSensitiveMessage) {
        if (shouldGenerateSensitiveMessage) {
            return getDetailedMessage(catalog, transaction);
        } else {
            return catalog.getString("You received an income.");
        }
    }

    private static String generateMessage(Catalog catalog, List<Transaction> transactions,
            boolean shouldGenerateSensitiveMessage) {
        if (transactions.size() == 1) {
            return generateMessage(catalog, transactions.get(0), shouldGenerateSensitiveMessage);
        }

        if (shouldGenerateSensitiveMessage) {
            return getDetailedMessage(catalog, transactions);
        } else {
            return Catalog.format(catalog.getString("You received {0} incomes."), transactions.size());
        }
    }

    private static String generateSensitiveMessage(User user, Catalog catalog, Transaction transaction) {
        if (user.getFlags().contains(FeatureFlags.ABN_AMRO_DETAILED_PUSH_NOTIFICATIONS)) {
            return Catalog.format(catalog.getString("You received income from {0}"), transaction.getDescription());
        } else {
            return null;
        }
    }

    private static String generateSensitiveMessage(User user, Catalog catalog, List<Transaction> transactions) {
        if (transactions.size() == 1) {
            return generateSensitiveMessage(user, catalog, transactions.get(0));
        }

        if (user.getFlags().contains(FeatureFlags.ABN_AMRO_DETAILED_PUSH_NOTIFICATIONS)) {
            return getDetailedMessage(catalog, transactions);
        } else {
            return null;
        }
    }

    private static String getDetailedMessage(Catalog catalog, Transaction transaction) {
        return Catalog.format(catalog.getString("You received income from {0}"), transaction.getDescription());
    }

    private static String getDetailedMessage(Catalog catalog, List<Transaction> transactions) {
        Iterable<String> descriptions = Iterables.transform(transactions, Transaction::getDescription);

        return Catalog
                .format(catalog.getString("You received income from {0}"), formatMultipleTitles(descriptions, catalog));
    }

    @Override
    public boolean isNotifiable() {
        return true;
    }
}
