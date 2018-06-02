package se.tink.libraries.abnamro.utils.creditcards;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import org.joda.time.Duration;
import se.tink.backend.core.Account;
import se.tink.backend.core.Transaction;
import se.tink.libraries.date.DateUtils;

public class DuplicateAccountMatcher {
    private ImmutableSet<Transaction> mutualTransactions;
    private int mutualTransactionThreshold;
    private Duration creationDateDifference;
    private AccountResult oldAccount;
    private AccountResult newAccount;

    /**
     * Consider accounts to be duplicates if they have more mutual transactions than the threshold.
     */
    public boolean isDuplicate() {
        return mutualTransactions.size() >= mutualTransactionThreshold;
    }

    public AccountResult getNewAccount() {
        return newAccount;
    }

    public AccountResult getOldAccount() {
        return oldAccount;
    }

    public Duration getCreationDateDifference() {
        return creationDateDifference;
    }

    public ImmutableSet<Transaction> getMutualTransactions() {
        return mutualTransactions;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class AccountResult {
        private ImmutableSet<Transaction> uniqueTransactions;
        private List<Transaction> allTransactions;
        private double balance;
        private String bankId;

        AccountResult(String bankId, double balance, List<Transaction> all, ImmutableSet<Transaction> unique) {
            this.bankId = bankId;
            this.balance = balance;
            this.allTransactions = all;
            this.uniqueTransactions = unique;
        }

        public double getBalance() {
            return balance;
        }

        public List<Transaction> getAllTransactions() {
            return allTransactions;
        }

        public ImmutableSet<Transaction> getUniqueTransactions() {
            return uniqueTransactions;
        }

        public String getBankId() {
            return bankId;
        }
    }

    public final static class Builder {
        // We consider ICS transactions the same if the have the same amount, description and date.
        private final static Comparator<Transaction> TRANSACTION_COMPARATOR = Comparator
                .comparing(Transaction::getOriginalAmount)
                .thenComparing(t -> t.getOriginalDescription().toLowerCase())
                .thenComparing(t -> DateUtils.flattenTime(t.getOriginalDate()));

        private Account account1;
        private Account account2;
        private List<Transaction> transactions1;
        private List<Transaction> transactions2;
        private Date account1CreatedDate;
        private Date account2CreatedDate;
        private int mutualTransactionThreshold;

        public Builder withMutualTransactionThreshold(int mutualTransactionThreshold) {
            this.mutualTransactionThreshold = mutualTransactionThreshold;
            return this;
        }

        public Builder withAccount1(Account account) {
            this.account1 = account;
            return this;
        }

        public Builder withAccount1Transactions(List<Transaction> transactions) {
            this.transactions1 = transactions;
            return this;
        }

        public Builder withAccount2(Account account) {
            this.account2 = account;
            return this;
        }

        public Builder withAccount1CreatedAt(Date date) {
            this.account1CreatedDate = date;
            return this;
        }

        public Builder withAccount2Transactions(List<Transaction> transactions) {
            this.transactions2 = transactions;
            return this;
        }

        public Builder withAccount2CreatedAt(Date date) {
            this.account2CreatedDate = date;
            return this;
        }

        public DuplicateAccountMatcher build() {
            Preconditions.checkNotNull(account1);
            Preconditions.checkNotNull(account2);
            Preconditions.checkNotNull(account1CreatedDate);
            Preconditions.checkNotNull(account2CreatedDate);
            Preconditions.checkNotNull(transactions1);
            Preconditions.checkNotNull(transactions2);

            TreeSet<Transaction> tree1 = Sets.newTreeSet(TRANSACTION_COMPARATOR);
            tree1.addAll(transactions1);

            TreeSet<Transaction> tree2 = Sets.newTreeSet(TRANSACTION_COMPARATOR);
            tree2.addAll(transactions2);

            DuplicateAccountMatcher duplicateMatcher = new DuplicateAccountMatcher();

            AccountResult accountResult1 = new AccountResult(account1.getBankId(), account1.getBalance(), transactions1,
                    Sets.difference(tree1, tree2).immutableCopy());

            AccountResult accountResult2 = new AccountResult(account2.getBankId(), account2.getBalance(), transactions2,
                    Sets.difference(tree2, tree1).immutableCopy());

            if (account1CreatedDate.before(account2CreatedDate)) {
                duplicateMatcher.oldAccount = accountResult1;
                duplicateMatcher.newAccount = accountResult2;
                duplicateMatcher.creationDateDifference = new Duration(account1CreatedDate.getTime(),
                        account2CreatedDate.getTime());
            } else {
                duplicateMatcher.oldAccount = accountResult2;
                duplicateMatcher.newAccount = accountResult1;
                duplicateMatcher.creationDateDifference = new Duration(account2CreatedDate.getTime(),
                        account1CreatedDate.getTime());
            }

            duplicateMatcher.mutualTransactions = Sets.intersection(tree1, tree2).immutableCopy();
            duplicateMatcher.mutualTransactionThreshold = mutualTransactionThreshold;

            return duplicateMatcher;
        }
    }
}
