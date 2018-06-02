package se.tink.backend.system.workers.processor.deduplication.detector;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import java.util.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.libraries.date.DateUtils;

/**
 * Detector to find duplicates in a set of transactions.
 *
 * Please not that this class is NOT thread-safe due to the optimization of using a shared calendar instance for hashing
 * transactions based on the julian date of a transaction.
 *
 */
public class DuplicateTransactionDetector {
    private static final double AMOUNT_EQUALS_DELTA = 0.001;

    private enum DuplicateDetectionMode {
        IGNORE_BATCHES,
        NO_DUPLICATES_IN_BATCH
    }

    // Overrides equals and hashCode to easily find duplicate transactions in a HashSet.
    private class TransactionByDuplicateFields {
        private final ImmutableList<Object> properties;
        private final int hashCode;

        public TransactionByDuplicateFields(Transaction transaction) {
            this.properties = buildProperties(transaction);
            this.hashCode = properties.hashCode(); // Cache for speedup.
        }

        private ImmutableList<Object> buildProperties(Transaction transaction) {
            calendar.setTime(transaction.getOriginalDate());
            
            return ImmutableList.<Object>of(
                    transaction.getAccountId(),
                    transaction.getOriginalDescription(),
                    transaction.isPending(),
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.DAY_OF_YEAR),
                    (int) (transaction.getOriginalAmount() / AMOUNT_EQUALS_DELTA));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TransactionByDuplicateFields) {
                TransactionByDuplicateFields other_wrapper = (TransactionByDuplicateFields) obj;
                return other_wrapper.properties.equals(this.properties);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("hashCode", hashCode).add("properties", properties).toString();
        }
    }

    // Overrides equals and hashCode to easily find duplicate transactions in a HashSet.
    private static class TransactionByExternalId {
        private final ImmutableList<Object> properties;
        private final int hashCode;

        public TransactionByExternalId(Transaction transaction) {
            this.properties = buildProperties(transaction);
            this.hashCode = properties.hashCode(); // Cache for speedup.
        }

        private ImmutableList<Object> buildProperties(Transaction transaction) {
            if (transaction.hasPayload()) {

                String externalId = transaction.getPayloadValue(TransactionPayloadTypes.EXTERNAL_ID);
                
                if (!Strings.isNullOrEmpty(externalId)) {
                    return ImmutableList.<Object> of(transaction.getAccountId(), externalId, transaction.isPending());
                }
            }
            return ImmutableList.of();
        }

        public boolean hasExternalId() {
            return !properties.isEmpty();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TransactionByExternalId) {
                TransactionByExternalId other_wrapper = (TransactionByExternalId) obj;
                return other_wrapper.properties.equals(this.properties);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("hashCode", hashCode).add("properties", properties).toString();
        }
    }

    // Note that `Transaction` doesn't override equals/hashCode so we are working with Object instance equality here.
    private final Map<TransactionByDuplicateFields, Set<Transaction>> transactionsByDuplicateFields;
    private final Map<TransactionByExternalId, Set<Transaction>> transactionsByExternalId;

    private final Calendar calendar = DateUtils.getCalendar();

    /**
     * Constructs a detector with a set of transactions to look for duplicates in.
     *
     * @param transactions Transactions to look for duplicates in.
     */
    public DuplicateTransactionDetector(Iterable<Transaction> transactions) {
        transactionsByDuplicateFields = Maps.newHashMapWithExpectedSize(Iterables.size(transactions));
        transactionsByExternalId = Maps.newHashMapWithExpectedSize(Iterables.size(transactions));
        
        for (Transaction transaction : transactions) {
            addTransaction(transaction);
        }
    }

    private static <T> void putToMap(Map<T, Set<Transaction>> map, T key, Transaction value) {
        Set<Transaction> bucket = map.get(key);
        if (bucket == null) {
            bucket = Sets.newHashSet(value);
            map.put(key, bucket);
        } else {
            bucket.add(value);
        }
    }

    private static <T> void removeFromMap(Map<T, Set<Transaction>> map, T key, Transaction transaction) {
        Set<Transaction> bucket = map.get(key);
        if (bucket != null) {
            bucket.remove(transaction);
            if (bucket.isEmpty()) {
                map.remove(key);
            }
        }
    }

    private void addTransaction(Transaction transaction) {
        putToMap(transactionsByDuplicateFields, new TransactionByDuplicateFields(transaction), transaction);

        TransactionByExternalId transactionByExternalId = new TransactionByExternalId(transaction);
        if (transactionByExternalId.hasExternalId()) {
            putToMap(transactionsByExternalId, transactionByExternalId, transaction);
        }
    }

    private void removeTransaction(Transaction transaction) {
        removeFromMap(transactionsByDuplicateFields, new TransactionByDuplicateFields(transaction), transaction);
        TransactionByExternalId transactionByExternalId = new TransactionByExternalId(transaction);
        if (transactionByExternalId.hasExternalId()) {
            removeFromMap(transactionsByExternalId, transactionByExternalId, transaction);
        }
    }

    /**
     * Find (and remove) a likely duplicate of a transaction in the set of transactions that backs the detector.
     *
     * @param transaction The transaction to find a duplicate for.
     * @return The matched duplicate transaction.
     */
    public Optional<Transaction> findAndRemoveDuplicate(Transaction transaction) {
        Optional<Transaction> matchedTransaction = Optional.empty();
        
        TransactionByExternalId transactionByExternalId = new TransactionByExternalId(transaction);
        if (transactionByExternalId.hasExternalId()) {
            // Prioritize external id lookup if we can. It's more correct. Transactions with the same external id in the
            // same batch are considered duplicates.
            matchedTransaction = searchForDuplicate(transactionsByExternalId, transactionByExternalId, transaction,
                    DuplicateDetectionMode.IGNORE_BATCHES);
        }

        if (!matchedTransaction.isPresent()) {
            // Fallback to check on duplicate fields if it wasn't matched on external id. Transactions within the same
            // batch are not considered duplicates.
            TransactionByDuplicateFields testKey = new TransactionByDuplicateFields(transaction);
            matchedTransaction = searchForDuplicate(transactionsByDuplicateFields, testKey, transaction,
                    DuplicateDetectionMode.NO_DUPLICATES_IN_BATCH);
        }

        if (matchedTransaction.isPresent()) {

            // Transactions with different external ids are always considered as different transactions
            if (differentExternalIds(transaction, matchedTransaction.get())) {
                return Optional.empty();
            }

            removeTransaction(matchedTransaction.get());
        }

        // Remove the transaction as this match shouldn't be available to be matched against again.

        return matchedTransaction;
    }

    /**
     * Returns true if both transactions have an external id and they differ
     */
    private static boolean differentExternalIds(Transaction left, Transaction right) {

        String leftExternalId = left.getPayloadValue(TransactionPayloadTypes.EXTERNAL_ID);
        String rightExternalId = right.getPayloadValue(TransactionPayloadTypes.EXTERNAL_ID);

        return !(Strings.isNullOrEmpty(leftExternalId) || Strings.isNullOrEmpty(rightExternalId) || Objects
                .equal(leftExternalId, rightExternalId));
    }

    private static <T> Optional<Transaction> searchForDuplicate(Map<T, Set<Transaction>> map, T key,
            Transaction transaction, DuplicateDetectionMode mode) {
        Set<Transaction> matchedBucket = map.get(key);
        if (matchedBucket != null) {
            for (Transaction potentialMatch : matchedBucket) {

                // If the transactions has the same inserted, ie. was ingested in the same batch,
                // it's likely not a duplicate.
                if (Objects.equal(mode, DuplicateDetectionMode.NO_DUPLICATES_IN_BATCH) && Objects
                        .equal(transaction.getInserted(), potentialMatch.getInserted())) {
                    continue;
                }

                return Optional.of(potentialMatch);
            }
        }
        return Optional.empty();
    }

    // Package-visible for testability.
    boolean isEmpty() {
        return transactionsByDuplicateFields.isEmpty() && transactionsByExternalId.isEmpty();
    }
}
