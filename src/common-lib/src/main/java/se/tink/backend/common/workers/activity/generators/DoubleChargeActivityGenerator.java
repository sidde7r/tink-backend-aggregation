package se.tink.backend.common.workers.activity.generators;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;
import se.tink.backend.common.utils.I18NUtils;
import se.tink.backend.common.workers.activity.ActivityGenerator;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.core.Account;
import se.tink.backend.core.Activity;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Notification;
import se.tink.backend.core.Transaction;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.backend.utils.guavaimpl.Predicates;

public class DoubleChargeActivityGenerator extends ActivityGenerator {
    protected static final double AMOUNT_NORMALIZATION_FACTOR = 2000;
    private static final double MIN_CHARGE_THRESHOLD = 15;
    private DeepLinkBuilderFactory deepLinkBuilderFactory;

    public DoubleChargeActivityGenerator(DeepLinkBuilderFactory deepLinkBuilderFactory) {
        super(DoubleChargeActivityGenerator.class, 60, 90, deepLinkBuilderFactory);
        this.deepLinkBuilderFactory = deepLinkBuilderFactory;
    }

    @Override
    public void generateActivity(final ActivityGeneratorContext context) {

        final double minimumChargeThreshold = context.getUserCurrency().getFactor() * MIN_CHARGE_THRESHOLD;

        List<Transaction> transactions = FluentIterable
                .from(context.getTransactions())
                .filter(Predicates.transactionsWithCategoryType(CategoryTypes.EXPENSES))
                .filter(t -> Math.abs(t.getAmount()) >= minimumChargeThreshold)
                .filter(t -> !context.getCategoryConfiguration().getDoubleChargeActivityExcludedCodes()
                        .contains(context.getCategoriesById().get(t.getCategoryId()).getCode()))
                .toList();

        Map<Key, List<Transaction>> transactionToKey = Maps.newHashMap();

        for (Transaction transaction : transactions) {

            Key key = getKey(transaction);
            if (!transactionToKey.containsKey(key)) {
                transactionToKey.put(key, Lists.<Transaction>newArrayList());
            }

            transactionToKey.get(key).add(transaction);
        }

        for (List<Transaction> transactionList : transactionToKey.values()) {
            if (transactionList.size() >= 2) {
                doubleChargeTransactions(context, transactionList);
            }
        }
    }

    private Key getKey(Transaction transaction) {
        return new Key(transaction.getOriginalAmount(), transaction.getAccountId(),
                transaction.getOriginalDescription(), transaction.isPending(), transaction.getOriginalDate());
    }

    private void doubleChargeTransactions(ActivityGeneratorContext context,
            List<Transaction> doubleChargeTransactions) {
        Transaction t1 = doubleChargeTransactions.get(0);

        String transactionHash = StringUtils.hashAsStringMD5(t1.getOriginalDescription()
                + t1.getOriginalAmount() + t1.getAccountId());

        String key = String.format("%s.%s.%s", Activity.Types.DOUBLE_CHARGE,
                ThreadSafeDateFormat.FORMATTER_DAILY.format(t1.getDate()), transactionHash);

        String feedActivityIdentifier = StringUtils.hashAsStringSHA1(key);

        double amount = calculateAmount(doubleChargeTransactions);

        context.addActivity(
                createActivity(
                        context.getUser().getId(),
                        t1.getDate(),
                        Activity.Types.DOUBLE_CHARGE,
                        context.getCatalog().getString("Double charge"),
                        generateMessage(context, t1),
                        doubleChargeTransactions,
                        key,
                        feedActivityIdentifier,
                        calculateImportance(amount,
                                AMOUNT_NORMALIZATION_FACTOR)),
                doubleChargeTransactions);
    }

    private double calculateAmount(List<Transaction> doubleChargeTransactions) {
        double amount = 0;
        for (Transaction t : doubleChargeTransactions) {
            amount += Math.abs(t.getAmount());
        }

        return amount;
    }

    private static String generateMessage(ActivityGeneratorContext context, Transaction transaction) {
        final Catalog catalog = context.getCatalog();
        boolean shouldGenerateSensitiveMessage = context.getActivitiesConfiguration().shouldGenerateSensitiveMessage();

        if (shouldGenerateSensitiveMessage) {
            String formattedAmount = I18NUtils.formatCurrency(Math.abs(transaction.getAmount()),
                    context.getUserCurrency(), context.getLocale());
            return Catalog.format(catalog.getString("You were double-charged by {0} for {1}."),
                    transaction.getDescription(), formattedAmount);
        } else {
            return catalog.getString("You were double-charged.");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<Notification> createNotifications(Activity activity, ActivityGeneratorContext context) {
        List<Transaction> transactions = (List<Transaction>) activity.getContent();

        if (transactions.size() == 0) {
            return Lists.newArrayList();
        }

        final Transaction transaction = transactions.get(0);

        Notification.Builder notification = new Notification.Builder()
                .fromActivity(activity)
                .groupable(true);

        if (context.getServiceContext().getConfiguration().getCluster() == Cluster.CORNWALL) {
            Optional<Account> account = context.getAccounts().stream().filter(
                    account1 -> Objects.equals(account1.getId(), transaction.getAccountId())).findFirst();

            if (account.isPresent() && transactions.size() > 1) {

                Transaction transaction2 = transactions.get(1);

                notification.url(String.format("%saccount/%s?transaction1=%s&transaction2=%s",
                        deepLinkBuilderFactory.getPrefix(), account.get().getId(), transaction.getId(),
                        transaction2.getId()));
            } else {
                notification.url(deepLinkBuilderFactory.getPrefix());
            }
        } else {
            notification.url(deepLinkBuilderFactory.transaction(transaction.getId()).build());
        }

        return buildNotificationsSilentlyFailing(activity.getUserId(), notification);
    }

    private class Key {
        private double originalAmount;
        private String accountId;
        private String originDescription;
        private boolean pending;
        private long time;

        public Key(double originalAmount, String accountId, String originDescription, boolean pending,
                Date originalDate) {
            this.originalAmount = originalAmount;
            this.accountId = accountId;
            this.originDescription = originDescription;
            this.pending = pending;
            this.time = DateUtils.flattenTime(originalDate).getTime();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Key key = (Key) o;

            if (!originDescription.equals(key.originDescription)) {
                return false;
            }
            if (Double.compare(key.originalAmount, originalAmount) != 0) {
                return false;
            }
            if (time != key.time) {
                return false;
            }
            if (!accountId.equals(key.accountId)) {
                return false;
            }
            return pending == key.pending;
        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            temp = Double.doubleToLongBits(originalAmount);
            result = (int) (temp ^ (temp >>> 32));
            result = 31 * result + (int) (time ^ (time >>> 32));
            result = 31 * result + accountId.hashCode();
            result = 31 * result + originDescription.hashCode();
            result = 31 * result + (pending ? 1 : 0);
            return result;
        }
    }

    @Override
    public boolean isNotifiable() {
        return true;
    }
}
