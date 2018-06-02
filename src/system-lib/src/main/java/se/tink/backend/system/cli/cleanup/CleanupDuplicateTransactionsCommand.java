package se.tink.backend.system.cli.cleanup;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.libraries.abnamro.utils.AbnAmroUtils;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.core.User;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.cli.helper.traversal.CommandLineInterfaceUserTraverser;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.guavaimpl.Orderings;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CleanupDuplicateTransactionsCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static final LogUtils log = new LogUtils(CleanupDuplicateTransactionsCommand.class);

    public CleanupDuplicateTransactionsCommand() {
        super("cleanup-duplicate-transactions", "Cleanup duplicate transactions.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, final ServiceContext serviceContext)
            throws Exception {

        final boolean forceDeleteOldest = Boolean.getBoolean("forceDeleteOldest");
        final boolean onlyCompareExternalId = Boolean.getBoolean("onlyCompareExternalId");
        final boolean dryRun = Boolean.getBoolean("dryRun");

        log.info(String.format(
                "Cleanup duplicate transactions (forceDeleteOldest=%b, dryRun=%b, onlyCompareExternalId=%b).",
                forceDeleteOldest, dryRun, onlyCompareExternalId));

        UserRepository userRepository = serviceContext.getRepository(UserRepository.class);

        final AtomicInteger userCount = new AtomicInteger();

        userRepository.streamAll()
                .compose(new CommandLineInterfaceUserTraverser(10))
                .forEach(user -> {
                    try {
                        cleanup(serviceContext, forceDeleteOldest, onlyCompareExternalId, dryRun, user, userCount);
                    } catch (Exception e) {
                        log.error(user.getId(), "Failed to cleanup transactions", e);
                    }
                });
    }

    public void cleanup(ServiceContext serviceContext, boolean forceDeleteOldest, boolean onlyCompareExternalId,
            boolean dryRun, User user, AtomicInteger userCount) {

        final TransactionDao transactionDao = serviceContext.getDao(TransactionDao.class);

        final List<Transaction> transactions = transactionDao
                .findAllByUserId(user.getId()).stream()
                .sorted(Orderings.TRANSACTION_DATE_ORDERING)
                .collect(Collectors.toList());

        Set<Transaction> transactionsToDelete = Sets.newHashSet();

        // Iterate through all transactions.
        for (int i = 0; i < transactions.size(); i++) {
            Transaction t1 = transactions.get(i);

            // Don't process transactions that have already been marked for deletion.
            if (transactionsToDelete.contains(t1)) {
                continue;
            }

            // Iterate trough all following transactions.
            for (int j = i + 1; j < transactions.size(); j++) {
                Transaction t2 = transactions.get(j);

                // Don't process transactions that have already been marked for deletion.
                if (transactionsToDelete.contains(t2)) {
                    continue;
                }

                boolean isDuplicate = onlyCompareExternalId ? AbnAmroUtils.isDuplicate(t1, t2) : isDuplicate(t1, t2);

                if (isDuplicate) {

                    log.info(user.getId(), "Duplicate found!");
                    log.info(user.getId(), String.format("t1=%s", toString(t1)));
                    log.info(user.getId(), String.format("t2=%s", toString(t2)));

                    Transaction transactionToDelete = getTransactionToDelete(t1, t2, forceDeleteOldest);

                    if (transactionToDelete != null) {
                        transactionsToDelete.add(transactionToDelete);
                    }

                    if (Objects.equal(t1, transactionToDelete)) {
                        log.info(user.getId(), "Remove t1.");
                    } else if (Objects.equal(t2, transactionToDelete)) {
                        log.info(user.getId(), "Remove t2.");
                    } else {
                        log.warn(user.getId(), "Unable to decide which transaction to remove.");
                    }
                }
            }
        }

        log.info(user.getId(), String.format("%d duplicate transactions found.", transactionsToDelete.size()));

        if (dryRun) {
            log.info(user.getId(), "No transactions will be deleted, since this is a dry run.");
        } else {
            deleteTransactions(serviceContext, user, transactionsToDelete);
        }

        log.info(String.format("Users processed: %s.", userCount.incrementAndGet()));
    }

    private void deleteTransactions(ServiceContext serviceContext, final User user,
            Iterable<Transaction> transactions) {

        final int size = Iterables.size(transactions);
        log.info(user.getId(), String.format("Delete %d transactions.", size));

        if (size == 0) {
            return;
        }

        final TransactionDao transactionDao = serviceContext.getDao(TransactionDao.class);

        // Delete transactions from index.
        transactionDao.delete(transactions);
        log.info(user.getId(), "Deletion done.");
    }

    /**
     * Figure out which transaction is the best to delete, with a bias towards suggesting `t2`.
     */
    private Transaction getTransactionToDelete(Transaction t1, Transaction t2, boolean forceDeleteOldest) {

        Transaction transactionToDelete = null;

        // = 0: indifferent
        // < 0: t1
        // > 0: t2
        int choice = 0;

        if (!forceDeleteOldest) {

            // Avert from suggesting a transaction with a transfer twin.

            boolean t1HasTwin = !Strings.isNullOrEmpty(t1.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN));
            boolean t2HasTwin = !Strings.isNullOrEmpty(t2.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN));

            choice = (t1HasTwin ? 1 : 0) - (t2HasTwin ? 1 : 0);

            transactionToDelete = chooseTransaction(t1, t2, choice);
            if (transactionToDelete != null) {
                return transactionToDelete;
            }

            // Avert from suggesting a transaction with modified description.

            choice = (t1.isUserModifiedDescription() ? 1 : 0) - (t2.isUserModifiedDescription() ? 1 : 0);

            transactionToDelete = chooseTransaction(t1, t2, choice);
            if (transactionToDelete != null) {
                return transactionToDelete;
            }

            // Avert from suggesting a transaction with modified category.

            choice = (t1.isUserModifiedCategory() ? 1 : 0) - (t2.isUserModifiedCategory() ? 1 : 0);

            transactionToDelete = chooseTransaction(t1, t2, choice);
            if (transactionToDelete != null) {
                return transactionToDelete;
            }
        }

        // Suggest the oldest one.

        choice = Long.compare(t1.getTimestamp(), t2.getTimestamp());

        transactionToDelete = chooseTransaction(t1, t2, choice);
        if (transactionToDelete != null) {
            return transactionToDelete;
        }

        // Comparable properties are the same; default to suggesting `t2`. This should never happen, since transactions
        // with the same timestamp (same batch) won't be identified as duplicates.
        transactionToDelete = t2;

        return transactionToDelete;
    }

    /**
     * choice < 0: t1
     * choice > 0: t2
     * choice = 0: none
     *
     * @param t1
     * @param t2
     * @param choice
     */
    private Transaction chooseTransaction(Transaction t1, Transaction t2, int choice) {
        if (choice < 0) {
            return t1;
        } else if (choice > 0) {
            return t2;
        } else {
            return null;
        }
    }

    private static final TypeReference<List<String>> STRING_LIST_TYPE_REFERENCE = new TypeReference<List<String>>() {
    };

    private static String getRawDescription(Transaction transaction) {

        String serializedDescriptionLines = transaction
                .getInternalPayload(AbnAmroUtils.InternalPayloadKeys.DESCRIPTION_LINES);

        if (Strings.isNullOrEmpty(serializedDescriptionLines)) {
            return transaction.getOriginalDescription();
        }

        List<String> descriptionLines = SerializationUtils.deserializeFromString(serializedDescriptionLines,
                STRING_LIST_TYPE_REFERENCE);

        String rawDescription = Joiner
                .on(" ")
                .join(descriptionLines)
                .replaceAll("\\s+", " ")
                .replaceAll("Â¤", "¤")
                .trim();

        return rawDescription;
    }

    private static final double AMOUNT_EQUALS_DELTA = 0.001;

    // Replication of `Transaction#sameAs(Object obj)` but with custom date comparison (due to timezone f-ups).
    private static boolean isDuplicate(Transaction t1, Transaction t2) {

        // The transactions belong to different accounts.
        if (!Objects.equal(t1.getAccountId(), t2.getAccountId())) {
            return false;
        }

        // The amount is different.
        if (Math.abs(t1.getOriginalAmount() - t2.getOriginalAmount()) > AMOUNT_EQUALS_DELTA) {
            return false;
        }

        // The transactions are not done the same day (not even potentially).
        if (!mightBeSameDay(t1.getOriginalDate(), t2.getOriginalDate())) {
            return false;
        }

        // The descriptions are different.
        if (!Objects.equal(getRawDescription(t1), getRawDescription(t2))) {
            return false;
        }

        // Same!
        return true;
    }

    private static boolean mightBeSameDay(Date d1, Date d2) {

        if (DateUtils.isSameDay(d1, d2)) {
            return true;
        }

        // If the dates are within two hours of each other, they might actually be on the same day.
        long t1 = TimeUnit.MILLISECONDS.toSeconds(d1.getTime());
        long t2 = TimeUnit.MILLISECONDS.toSeconds(d2.getTime());
        long diffSeconds = Math.abs(t1 - t2);

        if (diffSeconds <= 7200) {
            return true;
        }

        return false;
    }

    private static String toString(Transaction t) {
        return MoreObjects.toStringHelper(t.getClass())
                .add("id", t.getId())
                .add("timestamp", t.getTimestamp())
                .add("inserted", t.getInserted())
                .add("payload", t.getPayloadSerialized())
                .add("originaldate", t.getOriginalDate())
                .add("originalamount", t.getOriginalAmount())
                .add("description", t.getDescription())
                .add("originaldescription", t.getOriginalDescription())
                .add("usermodifieddescription", t.isUserModifiedDescription())
                .add("usermodifiedcategory", t.isUserModifiedCategory())
                .add("internalpayload", t.getInternalPayloadSerialized())
                .add("transfertwin", t.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN))
                .toString();
    }
}
