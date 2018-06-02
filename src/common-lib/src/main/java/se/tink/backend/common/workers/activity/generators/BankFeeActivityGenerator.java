package se.tink.backend.common.workers.activity.generators;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.List;
import se.tink.backend.common.bankfees.BankFeeRules;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.workers.activity.ActivityGenerator;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.core.Activity;
import se.tink.backend.core.Activity.Types;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class BankFeeActivityGenerator extends ActivityGenerator {

    private static final String KEY_FORMAT = Activity.Types.BANK_FEE + ".%s.%s";

    public BankFeeActivityGenerator(DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(BankFeeActivityGenerator.class, 60, 80, deepLinkBuilderFactory);
    }

    @Override
    public void generateActivity(ActivityGeneratorContext context) {
        if (!context.getUser().getFlags().contains(FeatureFlags.TINK_EMPLOYEE)) {
            return;
        }

        // Filter the transaction that matches bank fee descriptions
        ImmutableList<Transaction> matchedTransactions = FluentIterable.from(context.getUnusedTransactions())
                .filter(transaction -> transaction.getCategoryType() == CategoryTypes.EXPENSES &&
                        BankFeeRules.getInstance().matches(transaction))
                .toList();

        for (final Transaction transaction : matchedTransactions) {

            context.addActivity(
                    createActivity(
                            context.getUser().getId(),
                            transaction.getDate(),
                            Activity.Types.BANK_FEE,
                            context.getCatalog().getString("Bank Fee"),
                            Catalog.format(context.getCatalog().getString("You were charged by {0}"),
                                    transaction.getDescription()),
                            transaction,
                            createKey(transaction),
                            createFeedActivityIdentifier(transaction),
                            calculateImportance(Math.abs(transaction.getAmount()),
                                    DEFAULT_AMOUNT_NORMALIZATION_FACTOR * context.getUserCurrency().getFactor())),
                    transaction);
        }
    }

    private String createFeedActivityIdentifier(Transaction transaction) {
        StringBuilder feedActivityIdentifierBuilder = new StringBuilder(Types.BANK_FEE);

        feedActivityIdentifierBuilder.append(transaction.getId());
        feedActivityIdentifierBuilder.append(transaction.getDate());

        return StringUtils.hashAsStringSHA1(feedActivityIdentifierBuilder.toString());
    }

    private String createKey(Transaction t) {
        return String.format(KEY_FORMAT, ThreadSafeDateFormat.FORMATTER_DAILY.format(t.getDate()), t.getId());
    }

    @Override
    public List<Activity> groupActivities(ActivityGeneratorContext context, List<Activity> activities) {

        if (activities.size() == 1) {
            return activities;
        }

        List<Transaction> transactions = Lists.newArrayList(Iterables.transform(activities,
                a -> (Transaction) a.getContent()));

        Iterable<String> descriptions = Iterables.transform(transactions, Transaction::getDescription);

        double amount = 0;

        for (Transaction transaction : transactions) {
            amount += transaction.getAmount();
        }

        String key = String.format(KEY_FORMAT, ThreadSafeDateFormat.FORMATTER_DAILY.format(activities.get(0).getDate()),
                StringUtils.hashAsStringMD5("" + amount + transactions.size()));

        String feedActivityIdentifier = StringUtils.hashAsStringSHA1(key);

        return Lists.newArrayList(
                createActivity(
                        context.getUser().getId(),
                        activities.get(0).getDate(),
                        Activity.Types.BANK_FEE_MULTIPLE,
                        context.getCatalog().getString("Bank Fee"),
                        Catalog.format(context.getCatalog().getString("You were charged by {0}"),
                                formatMultipleTitles(descriptions, context.getCatalog())),
                        transactions,
                        key,
                        feedActivityIdentifier,
                        calculateImportance(Math.abs(amount),
                                DEFAULT_AMOUNT_NORMALIZATION_FACTOR * context.getUserCurrency().getFactor())));
    }

    @Override
    public boolean isNotifiable() {
        return false;
    }
}
