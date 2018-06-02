package se.tink.backend.system.workers.processor.deduplication.detector;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import se.tink.backend.core.Account;
import se.tink.backend.core.Provider;
import se.tink.backend.core.Transaction;
import se.tink.backend.system.workers.processor.TransactionProcessor;
import se.tink.backend.system.workers.processor.deduplication.DeduplicationResult;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.guavaimpl.Orderings;
import se.tink.backend.utils.guavaimpl.Predicates;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.Histogram;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

public class FuzzyTransactionDeduplicator {
    private static final LogUtils log = new LogUtils(FuzzyTransactionDeduplicator.class);
    private static final float MIN_ALLOWED_SCORE_FOR_MATCH = 0.65f;
    private static final MetricId DEDUPLICATOR_METRIC_ID = MetricId.newId("transaction_deduplicator");

    private final Histogram unchangedTransactionsHistogram;
    private final Histogram newTransactionsHistogram;
    private final Histogram modifiedPendingTransactionsHistogram;
    private final Histogram settledTransactionsHistogram;
    private final Histogram deletedTransactionsHistogram;
    private final Counter successCounter;
    private final Counter failedCounter;

    private int unchangedTransactions;
    private int settledTransactions;

    private Provider provider;
    private final List<Account> accounts;

    private Map<String, List<Transaction>> existingTransactionsByAccountId;
    private Map<String, List<Transaction>> incomingTransactionsByAccountId;
    private final Map<Transaction, Transaction> transactionsToUpdateByExistingTransaction = Maps.newHashMap();

    /**
     * Cut existing transactions before oldest incoming and add pending transactions before oldest incoming
     * transaction (non-pending transactions before incoming cannot change date = don't deduplicate)
     */
    private static Predicate<Transaction> filterOutNonPendingTransactionsBefore(final Date cutOffDate) {
        return t -> t.isPending() || DateUtils.daysBetween(cutOffDate, t.getOriginalDate()) >= 0;
    }

    public FuzzyTransactionDeduplicator(MetricRegistry metricRegistry, Provider provider, List<Account> accounts) {
        this.provider = provider;
        this.accounts = accounts;

        MetricId.MetricLabels providerLabel = new MetricId.MetricLabels()
                .add(TransactionProcessor.MetricKey.PROVIDER, provider.getName());

        this.unchangedTransactionsHistogram = metricRegistry.histogram(
                TransactionProcessor.UNCHANGED_TRANSACTIONS_METRIC_ID.label(providerLabel),
                TransactionProcessor.MetricBuckets.X_LARGE);
        this.modifiedPendingTransactionsHistogram = metricRegistry.histogram(
                TransactionProcessor.UPDATED_TRANSACTIONS_METRIC_ID.label("settled", "false").label(providerLabel),
                TransactionProcessor.MetricBuckets.MEDIUM);
        this.settledTransactionsHistogram = metricRegistry.histogram(
                TransactionProcessor.UPDATED_TRANSACTIONS_METRIC_ID.label("settled", "true").label(providerLabel),
                TransactionProcessor.MetricBuckets.MEDIUM);
        this.newTransactionsHistogram = metricRegistry.histogram(
                TransactionProcessor.NEW_TRANSACTIONS_METRIC_ID.label(providerLabel),
                TransactionProcessor.MetricBuckets.LARGE);
        this.deletedTransactionsHistogram = metricRegistry.histogram(
                TransactionProcessor.DELETED_TRANSACTIONS_METRIC_ID.label(providerLabel),
                TransactionProcessor.MetricBuckets.SMALL);

        this.successCounter = metricRegistry.meter(DEDUPLICATOR_METRIC_ID.label("outcome", "success"));
        this.failedCounter = metricRegistry.meter(DEDUPLICATOR_METRIC_ID.label("outcome", "failed"));
    }

    private List<Transaction> getTransactionsToDelete() {
        List<Transaction> transactionsToDelete = Lists.newArrayList();

        for (List<Transaction> transactions : existingTransactionsByAccountId.values()) {
            transactionsToDelete.addAll(transactions);
        }

        return transactionsToDelete;
    }

    private List<Transaction> getNewTransactions() {
        List<Transaction> newTransactions = Lists.newArrayList();

        for (List<Transaction> transactions : incomingTransactionsByAccountId.values()) {
            newTransactions.addAll(transactions);
        }

        return newTransactions;
    }

    private Map<Transaction, Transaction>  getTransactionsToUpdate() {
        return transactionsToUpdateByExistingTransaction;
    }

    @VisibleForTesting
    Transaction getTransactionToReplace(Transaction transaction) {
        return transactionsToUpdateByExistingTransaction.get(transaction);
    }

    public DeduplicationResult deduplicate(Collection<Transaction> allExistingTransactions,
            List<Transaction> allIncomingTransactions) {
        try {
            this.existingTransactionsByAccountId = toMapByAccountId(allExistingTransactions);
            this.incomingTransactionsByAccountId = toMapByAccountId(allIncomingTransactions);

            truncateTransactionsByAccount(existingTransactionsByAccountId, incomingTransactionsByAccountId, accounts);

            ruleOutExactMatches();
            deduplicatePendingTransactions();

            DeduplicationResult result = result();
            successCounter.inc();

            return result;
        } catch (RuntimeException e) {
            log.error("FuzzyTransactionDeduplicator failed due to exception", e);
            failedCounter.inc();
            throw e;
        }
    }

    private void ruleOutExactMatches() {
        for (String accountId : this.existingTransactionsByAccountId.keySet()) {
            Iterator<Transaction> existingTransactions = this.existingTransactionsByAccountId.get(accountId).iterator();

            while (existingTransactions.hasNext()) {
                Transaction existingTransaction = existingTransactions.next();
                final Iterator<Transaction> incomingTransactions = this.incomingTransactionsByAccountId.get(accountId).iterator();

                while (incomingTransactions.hasNext()) {
                    Transaction incomingTransaction = incomingTransactions.next();
                    FuzzyTransactionMatcher.Result result = FuzzyTransactionMatcher
                            .compare(existingTransaction, incomingTransaction, provider, true);

                    if (Objects.equals(result.getScore(), 1d)) {

                        if (existingTransaction.isPending() && !incomingTransaction.isPending()) {
                            transactionsToUpdateByExistingTransaction.put(existingTransaction, incomingTransaction);
                            settledTransactions++;
                        } else {
                            unchangedTransactions++;
                        }

                        existingTransactions.remove();
                        incomingTransactions.remove();
                        break;
                    }
                }
            }
        }
    }

    /**
     * When we have found the best match for an existing transaction within
     * the list of incoming transactions, we reverse the process and try to
     * find the best match for the incoming transaction (that we just found)
     * in the list of existing transactions.
     *
     * If the best match we can find for the incoming transaction is the
     * same transaction as we started with, we can be certain that there are
     * no other transactions that is a better match.
     *
     */
    private void deduplicatePendingTransactions() {
        for (String accountId : existingTransactionsByAccountId.keySet()) {
            int plausibleMatches;

            do {
                plausibleMatches = 0;
                Iterator<Transaction> existingTransactions = existingTransactionsByAccountId.get(accountId).iterator();

                while (existingTransactions.hasNext()) {
                    Transaction existingTransaction = existingTransactions.next();

                    if (!existingTransaction.isPending()) {
                        // Only deduplicate pending transactions
                        continue;
                    }

                    // Find the most similar incoming transaction
                    FuzzyTransactionMatcher.Result result = findBestMatch(existingTransaction,
                            incomingTransactionsByAccountId.get(existingTransaction.getAccountId()), true);

                    Optional<Transaction> bestMatch = result.getPossibleMatch();

                    if (bestMatch.isPresent() && result.getScore() > MIN_ALLOWED_SCORE_FOR_MATCH) {
                        // Found a potential matching incoming transaction
                        plausibleMatches++;

                        // Find the most similar existing transaction for the found incoming transaction
                        FuzzyTransactionMatcher.Result reverseResult = findBestMatch(bestMatch.get(),
                                this.existingTransactionsByAccountId.get(bestMatch.get().getAccountId()), false);

                        Optional<Transaction> reverseMatch = reverseResult.getPossibleMatch();

                        if (reverseMatch.isPresent() && Objects.equals(existingTransaction, reverseMatch.get())) {
                            // The existing transaction {reverseMatch} most similar to the found
                            // incoming transaction {bestMatch} is the same as the same existing transaction
                            // as we started with {existingTransaction}
                            Transaction certainMatch = bestMatch.get();

                            if (existingTransaction.isPending() && !certainMatch.isPending()) {
                                settledTransactions++;
                            }

                            transactionsToUpdateByExistingTransaction.put(existingTransaction, certainMatch);
                            incomingTransactionsByAccountId.get(certainMatch.getAccountId()).remove(certainMatch);
                            existingTransactions.remove();

                            plausibleMatches--;
                        }
                    }
                }
            } while (!this.existingTransactionsByAccountId.get(accountId).isEmpty() &&
                    !incomingTransactionsByAccountId.get(accountId).isEmpty() && plausibleMatches > 0);
        }
    }

    private FuzzyTransactionMatcher.Result findBestMatch(Transaction transaction, List<Transaction> possibleMatches,
            boolean possibleMatchesAreIncoming) {
        FuzzyTransactionMatcher.Result bestMatch = new FuzzyTransactionMatcher.Result(transaction);

        for (Transaction possibleMatch : possibleMatches) {
            FuzzyTransactionMatcher.Result result = FuzzyTransactionMatcher
                    .compare(transaction, possibleMatch, provider, possibleMatchesAreIncoming);

            if (!bestMatch.getPossibleMatch().isPresent() || result.getScore() > bestMatch.getScore()) {
                bestMatch = result;

                if (Objects.equals(result.getScore(), 1)) {
                    break;
                }
            }
        }

        return bestMatch;
    }

    static Map<String, List<Transaction>> toMapByAccountId(Collection<Transaction> transactions) {
        if (transactions == null) {
            return Maps.newHashMap();
        }

        return transactions.stream().collect(Collectors.groupingBy(Transaction::getAccountId));
    }

    static void truncateTransactionsByAccount(Map<String, List<Transaction>> existingTransactionsByAccount,
            Map<String, List<Transaction>> incomingTransactionsByAccount, List<Account> accounts) {

        Preconditions.checkNotNull(accounts);
        Preconditions.checkArgument(!accounts.isEmpty());

        Set<String> accountIds = accounts.stream()
                .map(Account::getId)
                .collect(Collectors.toSet());

        filterOutTransactionsWithoutAccount(accountIds, existingTransactionsByAccount);
        filterOutTransactionsWithoutAccount(accountIds, incomingTransactionsByAccount);

        for (Account account : accounts) {
            if (isNewAccount(account, existingTransactionsByAccount)) {
                // Couldn't find any transactions in the database related to the account
                // Don't truncate any of the lists
                continue;
            }
            if (isClosedAccount(account, incomingTransactionsByAccount)) {
                // Couldn't find any incoming transactions related to the account
                // Existing transactions for the account will always be present
                // Since isNewAccount() would continue the loop otherwise
                existingTransactionsByAccount.remove(account.getId());
                continue;
            }

            List<Transaction> existingTransactions = existingTransactionsByAccount.get(account.getId());
            List<Transaction> incomingTransactions = incomingTransactionsByAccount.get(account.getId());

            if (existingTransactions.stream().filter(Predicates.filterTransactionsOnIsPending(false)::apply).count() == 0) {
                // The only existing transactions are pending, let's not truncate.
                continue;
            }

            final Date oldestIncomingTransactionDate = new DateTime(incomingTransactions.stream()
                    .min(Orderings.TRANSACTION_ORIGINAL_DATE_ORDERING).get().getOriginalDate())
                    .withTimeAtStartOfDay()
                    .toDate();

            if (account.getCertainDate() != null) {
                // Remove all existing transactions before the related accounts certainDate
                existingTransactions = truncateTransactions(existingTransactions, account.getCertainDate());

                if (existingTransactions.isEmpty()) {
                    // Most likely a really rare case, something has gone wrong
                    // with the calculation of the accounts certainDate
                    log.warn(account.getUserId(), account.getCredentialsId(),
                            "The accounts certainDate was newer than the most recent existing transaction, "
                                    + "have the certainDate been altered?");

                    // Remove all existingTransactions that before the oldest incoming transaction
                    existingTransactions = truncateTransactions(existingTransactionsByAccount.get(account.getId()),
                            oldestIncomingTransactionDate);

                    if (existingTransactions.isEmpty()) {
                        // If existingTransactions is still empty, the most recent existing transaction is older
                        // than the oldest incoming transaction
                        // This case should be handled as a new account
                        existingTransactionsByAccount.remove(account.getId());
                        continue;
                    }
                }
            }

            Date mostRecentExistingTransactionDate = new DateTime(existingTransactions.stream()
                    .max(Orderings.TRANSACTION_ORIGINAL_DATE_ORDERING).get().getOriginalDate())
                    .withTimeAtStartOfDay()
                    .toDate();

            Date oldestExistingTransactionDate = new DateTime(existingTransactions.stream()
                    .min(Orderings.TRANSACTION_ORIGINAL_DATE_ORDERING).get().getOriginalDate())
                    .withTimeAtStartOfDay()
                    .toDate();

            if (DateUtils.beforeOrEqual(oldestIncomingTransactionDate, oldestExistingTransactionDate)) {
                // The oldest incoming transaction is older than the accounts certain date (Most common case)
                incomingTransactions = truncateTransactions(incomingTransactions, oldestExistingTransactionDate);
            } else if (DateUtils.after(oldestIncomingTransactionDate, mostRecentExistingTransactionDate)) {
                // The user haven't refreshed in a while (longer than the banks transaction history) resulting
                // in a gap between the oldest incoming transaction and the most recent existing transaction
                existingTransactions = Lists.newArrayList();
            } else {
                // The user haven't refreshed in a while (just enough in order to make it impossible for us to know
                // where it would be safe to cut the lists).
                existingTransactions = existingTransactions.stream()
                        .filter(filterOutNonPendingTransactionsBefore(
                                oldestIncomingTransactionDate))
                        .collect(Collectors.toList());
            }

            existingTransactionsByAccount.put(account.getId(), existingTransactions);
            incomingTransactionsByAccount.put(account.getId(), incomingTransactions);
        }
    }

    private static void filterOutTransactionsWithoutAccount(Set<String> accountIds,
            Map<String, List<Transaction>> transactionsByAccountId) {
        Set<String> accountIdsToRemove = transactionsByAccountId.keySet().stream()
                .filter(accountId -> !accountIds.contains(accountId))
                .collect(Collectors.toSet());

        accountIdsToRemove.forEach(transactionsByAccountId::remove);
    }

    private static List<Transaction> truncateTransactions(List<Transaction> transactions, Date cutOfDate) {
        return transactions.stream()
                .filter(Predicates.filterOutTransactionsWithOriginalDateBefore(cutOfDate)::apply)
                .collect(Collectors.toList());
    }

    private static boolean isClosedAccount(Account account, Map<String, List<Transaction>> incomingTransactionsByAccount) {
        return !containsAnyTransactionsForAccount(account, incomingTransactionsByAccount);
    }

    private static boolean isNewAccount(Account account, Map<String, List<Transaction>> existingTransactionsByAccount) {
        return !containsAnyTransactionsForAccount(account, existingTransactionsByAccount);
    }

    private static boolean containsAnyTransactionsForAccount(Account account, Map<String, List<Transaction>> transactionsByAccount) {
        return transactionsByAccount.containsKey(account.getId())
                && transactionsByAccount.get(account.getId()) != null
                && !transactionsByAccount.get(account.getId()).isEmpty();
    }

    private DeduplicationResult result() {
        List<Transaction> newTransactions = getNewTransactions();
        List<Transaction> deletedTransactions = getTransactionsToDelete();
        List<Transaction> updatedTransactions = Lists.newArrayList();

        getTransactionsToUpdate().forEach((existingTransaction, incomingTransaction) -> {
            LocalDate existingTransactionDate = existingTransaction.getOriginalDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate incomingTransactionDate = incomingTransaction.getOriginalDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            int existingMonth = existingTransactionDate.getMonth().getValue();
            int incomingMonth = incomingTransactionDate.getMonth().getValue();

            if(incomingMonth != existingMonth) {
                deletedTransactions.add(existingTransaction);
                incomingTransaction.copyToIncoming(existingTransaction);
                updatedTransactions.add(incomingTransaction);
            } else {
                existingTransaction.copyToExisting(incomingTransaction);
                updatedTransactions.add(existingTransaction);
            }


        });

        this.unchangedTransactionsHistogram.update(this.unchangedTransactions);
        this.newTransactionsHistogram.update(newTransactions.size());
        // updatedTransactions contain both modified pending transactions and settled transactions
        this.modifiedPendingTransactionsHistogram.update(updatedTransactions.size() - this.settledTransactions);
        this.settledTransactionsHistogram.update(this.settledTransactions);
        this.deletedTransactionsHistogram.update(deletedTransactions.size());

        return new DeduplicationResult(newTransactions, updatedTransactions, deletedTransactions);
    }
}
