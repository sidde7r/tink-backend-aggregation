package se.tink.backend.aggregation.nxgen.framework.validation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.assertj.core.util.Preconditions;
import se.tink.backend.aggregation.agents.models.Transaction;

/** Used for finding duplicates in a collection of transactions efficiently. */
public final class DuplicateTransactionFinder {
    private Collection<Transaction> transactions;
    private Collection<Transaction> duplicates; // Any subset containing considered duplicates

    public DuplicateTransactionFinder() {
        transactions = null;
        duplicates = Collections.emptySet();
    }

    /**
     * @return true iff the collection contains a pair of transactions that are considered
     *     duplicates
     */
    public boolean containsDuplicates(@Nonnull final Collection<Transaction> transactions) {
        return !getAnyDuplicates(transactions).isEmpty();
    }

    /**
     * @return Any subset of transactions in the collection such that two transactions are
     *     duplicates of one another, or an empty collection if no such pair exists.
     */
    public Collection<Transaction> getAnyDuplicates(
            @Nonnull final Collection<Transaction> transactions) {
        Preconditions.checkNotNull(transactions);

        // Comparing by reference is sufficient for now
        if (transactions != this.transactions) {
            computeDuplicates(transactions);
        }
        return duplicates;
    }

    /**
     * Postcondition: this.transactions will be assigned to the argument transactions.
     * this.duplicates will be set to a pair of transactions within this collection that are
     * duplicates of each other.
     */
    private void computeDuplicates(final Collection<Transaction> transactions) {
        // TODO Optimize to O(n*log(n))
        for (final Transaction transaction : transactions) {
            final Optional<Transaction> duplicate =
                    transactions.stream()
                            .filter(t -> t != transaction)
                            .filter(t -> Objects.equals(t.getDate(), transaction.getDate()))
                            .filter(
                                    t ->
                                            Objects.equals(
                                                    t.getDescription(),
                                                    transaction.getDescription()))
                            .filter(t -> t.getAmount() == transaction.getAmount())
                            .filter(
                                    t ->
                                            Objects.equals(
                                                    t.getAccountId(), transaction.getAccountId()))
                            .findFirst();
            if (duplicate.isPresent()) {
                duplicates = Arrays.asList(transaction, duplicate.get());
                break;
            }
        }
        this.transactions = transactions;
    }

    public static boolean containsDupes(@Nonnull final Collection<Transaction> transactions) {
        return new DuplicateTransactionFinder().containsDuplicates(transactions);
    }
}
