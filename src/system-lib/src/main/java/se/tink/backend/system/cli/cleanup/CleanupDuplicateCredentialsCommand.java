package se.tink.backend.system.cli.cleanup;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.google.common.util.concurrent.RateLimiter;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.client.ServiceFactory;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.auth.HttpAuthenticationMethod;

public class CleanupDuplicateCredentialsCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static final LogUtils log = new LogUtils(CleanupDuplicateCredentialsCommand.class);

    /**
     * Encapsulates all the changes that would be done. Sort of like a database transaction. Useful to isolate all
     * changes and easily print/log them. Can be executed at will if not running in dry-run.
     */
    public class MergeExecutor {
        private Collection<Account> accountsToSave = Lists.newArrayList();
        private Credentials losingCredentials;
        private List<Transaction> transactionsToSave = Lists.newArrayList();
        private User user;
        private List<Transaction> transactionsToDelete = Lists.newArrayList();

        public void setAccountsToSave(Collection<Account> accountsToSave) {
            this.accountsToSave = accountsToSave;
        }

        public void setLosingCredentials(Credentials losingCredentials) {
            this.losingCredentials = losingCredentials;
            this.user = userRepository.findOne(losingCredentials.getUserId());
        }

        public void setTransactionsToDelete(List<Transaction> transactionsToDelete) {
            this.transactionsToDelete = transactionsToDelete;
        }

        public void setTransactionsToSave(List<Transaction> transactionsToSave) {
            this.transactionsToSave = transactionsToSave;
        }

        private void validate() {
            Preconditions.checkNotNull(user);
            Preconditions.checkNotNull(losingCredentials);

            // Input validation
            for (Account account : accountsToSave) {
                Preconditions.checkArgument(losingCredentials.getUserId().equals(account.getUserId()));
            }
            for (Transaction transaction : transactionsToSave) {
                Preconditions.checkArgument(losingCredentials.getUserId().equals(transaction.getUserId()));
            }
        }

        /**
         * Make the actual changes. Kind of like "COMMIT" in database lingo.
         */
        public void execute() {
            validate();

            log.info(user.getId(), "Applying changes...");
            // Save before delete to avoid deleting stuff mistakenly. Better to duplicate in that case.
            
            if (!transactionsToSave.isEmpty()) {
                transactionDao.saveAndIndex(user, transactionsToSave, false);
            }
            if (!transactionsToDelete.isEmpty()) {
                transactionDao.delete(transactionsToDelete);
            }
            if (!accountsToSave.isEmpty()) {
                accountRepository.save(accountsToSave);
            }

            // Does some async cleanup. Running it the last to avoid deadlock.
            serviceFactory.getCredentialsService().delete(
                    new AuthenticatedUser(HttpAuthenticationMethod.BASIC, user), losingCredentials.getId());
        }

        public void logChanges() {
            validate();

            log.info(
                    losingCredentials.getUserId(),
                    losingCredentials.getId(),
                    String.format("Would have deleted credentials %s (and all that comes with it).", losingCredentials));

            log.info(losingCredentials.getUserId(), losingCredentials.getId(),
                    String.format("%d transactions to save:", transactionsToSave.size()));
            for (Transaction transaction : transactionsToSave) {
                log.info(transaction.getUserId(), transaction.getCredentialsId(),
                        String.format(" * %s", transaction));
            }

            log.info(losingCredentials.getUserId(), losingCredentials.getId(),
                    String.format("%d transactions to delete:", transactionsToDelete.size()));
            for (Transaction transaction : transactionsToDelete) {
                log.info(transaction.getUserId(), transaction.getCredentialsId(),
                        String.format(" * %s", transaction));
            }

            log.info(losingCredentials.getUserId(), losingCredentials.getId(),
                    String.format("%d accounts to save:", accountsToSave.size()));
            for (Account account : accountsToSave) {
                log.info(account.getUserId(), account.getCredentialsId(), String.format(" * %s", account));
            }
        }
    }

    private static final LogUtils LOG = new LogUtils(CleanupDuplicateCredentialsCommand.class);

    private AccountRepository accountRepository;
    private CredentialsRepository credentialsRepository;
    private ServiceFactory serviceFactory;
    private TransactionDao transactionDao;

    private UserRepository userRepository;

    public CleanupDuplicateCredentialsCommand() {
        super("cleanup-duplicate-credentials", "Cleans up duplicate credentials.");
    }

    private MergeExecutor prepareCredentialsMerge(final Credentials losingCredentials,
            final Credentials winnerCredentials)
            throws CloneNotSupportedException, MergeNotSupportedException {

        log.info(String.format("Asked to construct a MergeExecutor merges %s into %s.",
                losingCredentials, winnerCredentials));

        // Input validation.

        Preconditions.checkArgument(!Objects.equal(losingCredentials.getId(), winnerCredentials.getId()));
        Preconditions.checkArgument(
                new NormalizedProviderName(losingCredentials.getProviderName()).equals(
                        new NormalizedProviderName(winnerCredentials.getProviderName())),
                "Merging different providers should probably not be allowed.");
        Preconditions.checkArgument(losingCredentials.getUserId().equals(winnerCredentials.getUserId()));
        final String userId = losingCredentials.getUserId(); // Mostly just an alias.

        // Fetch accounts.

        final ImmutableMap<String, Account> allUsersAccountsById;
        final Iterable<Account> winningAccounts;
        {
            final List<Account> allAccounts = accountRepository.findByUserId(userId);
            allUsersAccountsById = Maps.uniqueIndex(
                    allAccounts, Account::getId);
            winningAccounts = Iterables.filter(allAccounts,
                    input -> Objects.equal(input.getCredentialsId(), winnerCredentials.getId()));
        }

        // Load all transactions for the user. Since they are modified in each iteration it's probably safer to fetch
        // them fresh from database for every call to this method.

        final Iterable<Transaction> allUsersTransactions = transactionDao.findAllByUserId(userId);

        final Predicate<Transaction> transactionHasMatchingAccount = input -> allUsersAccountsById
                .containsKey(input.getAccountId());

        final Iterable<Transaction> orphanTransactions = Iterables.filter(allUsersTransactions,
                Predicates.not(transactionHasMatchingAccount));

        // These are the transactions we will be working with until the end of this method.
        final Iterable<Transaction> transactions = Iterables
                .filter(allUsersTransactions, transactionHasMatchingAccount);

        // Various data structures to look at the world from different angles.

        final Iterable<Transaction> winnerTransactions = Iterables.filter(transactions,
                input -> Objects.equal(input.getCredentialsId(), winnerCredentials.getId()));

        final ImmutableMap<String, Account> winningAccountsByBankId = Maps.uniqueIndex(
                winningAccounts, Account::getBankId);

        final LinkedListMultimap<TransactionGroupKey, Transaction> transactionsForWinner = LinkedListMultimap
                .create(Multimaps.index(winnerTransactions,
                        input -> TransactionGroupKey.from(allUsersAccountsById, input)));

        final Iterable<Transaction> transactionsForThisLoserCredentials = Iterables.filter(transactions,
                input -> Objects.equal(input.getCredentialsId(), losingCredentials.getId()));

        final ImmutableMap<String, Account> losingAccountsById = Maps.uniqueIndex(
                accountRepository.findByCredentialsId(losingCredentials.getId()), Account::getId);

        // Migrate all transactions over from the losing credentials to the winning if they don't exist or have been
        // modified.

        final Map<String, Account> accountsToCreateByBankId = Maps.newHashMap();
        final List<Transaction> transactionsToSave = Lists.newArrayList();
        for (Transaction losingTransaction : transactionsForThisLoserCredentials) {

            transactionDebugLog(losingTransaction, "Handling losing transaction.");

            // Make sure the account exists on winning credentials.

            final Account thisTransactionsAccount = losingAccountsById.get(losingTransaction.getAccountId());

            if (thisTransactionsAccount == null) {
                throw new MergeNotSupportedException(
                        String.format(
                                "Transaction id=%s for user %s did not have a corresponding account. Skipping credentials to be on the safe side.",
                                losingTransaction.getId(), userId));
            }

            if (winningAccountsByBankId.containsKey(thisTransactionsAccount.getBankId())) {
                // We now know that the account exists on the winning credentials.

                transactionDebugLog(losingTransaction,
                        "The losing transaction had a matching account.");

                final Account winningAccount = winningAccountsByBankId.get(thisTransactionsAccount.getBankId());

                // winningAccount can't be more certain than thisTransactionsAccount. This is a safety measure to
                // make sure we sync with the bank fully.
                ArrayList<Date> dateCandidates = Lists.newArrayList(Iterables.filter(
                        Lists.newArrayList(winningAccount.getCertainDate(),
                                thisTransactionsAccount.getCertainDate()), Predicates.notNull()));
                if (!dateCandidates.isEmpty()) {
                    winningAccount.setCertainDate(Collections.min(dateCandidates));
                } else
                    // Better safe than sorry.
                {
                    winningAccount.setCertainDate(null);
                }

                if (losingTransaction.isPending()) {
                    // Not migrating pending transactions to stay on the safe side.
                    transactionDebugLog(losingTransaction, "Transaction is pending. Not migrating.");
                    continue;
                }

                final TransactionGroupKey losingTransactionGroupKey = TransactionGroupKey.from(allUsersAccountsById,
                        losingTransaction);
                if (transactionsForWinner.containsKey(losingTransactionGroupKey)) {
                    // If the transaction was previously recorded in the winning credentials.

                    transactionDebugLog(losingTransaction,
                            "The losing transaction was found in the credentials to be kept.");

                    final Transaction winningTransactionMatch = transactionsForWinner.get(losingTransactionGroupKey)
                            .remove(0);

                    // XXX: If we have multiple winning transactions above we might modify the wrong one here.

                    boolean winningTransactionMatchModified = false;
                    if (losingTransaction.isUserModifiedAmount() && !winningTransactionMatch.isUserModifiedAmount()) {
                        winningTransactionMatch.setAmount(losingTransaction.getAmount());
                        winningTransactionMatch.setUserModifiedAmount(true);
                        winningTransactionMatchModified = true;
                    }
                    if (losingTransaction.isUserModifiedCategory() && !winningTransactionMatch.isUserModifiedCategory()) {
                        winningTransactionMatch
                                .setCategory(losingTransaction.getCategoryId(), losingTransaction.getCategoryType());
                        winningTransactionMatch.setUserModifiedCategory(true);
                        winningTransactionMatchModified = true;
                    }
                    if (losingTransaction.isUserModifiedDate() && !winningTransactionMatch.isUserModifiedDate()) {
                        winningTransactionMatch.setDate(losingTransaction.getDate());
                        winningTransactionMatch.setUserModifiedDate(true);
                        winningTransactionMatchModified = true;
                    }
                    if (losingTransaction.isUserModifiedDescription()
                            && !winningTransactionMatch.isUserModifiedDescription()) {
                        winningTransactionMatch.setDescription(losingTransaction.getDescription());
                        winningTransactionMatch.setUserModifiedDescription(true);
                        winningTransactionMatchModified = true;
                    }
                    if (losingTransaction.isUserModifiedLocation() && !winningTransactionMatch.isUserModifiedLocation()) {
                        winningTransactionMatch.setMerchantId(losingTransaction.getMerchantId());
                        winningTransactionMatch.setUserModifiedLocation(true);
                        winningTransactionMatchModified = true;
                    }
                    
                    final Joiner spaceJoiner = Joiner.on(" ").skipNulls();
                    winningTransactionMatch.setNotes(spaceJoiner.join(
                            org.apache.commons.lang.StringUtils.trimToNull(winningTransactionMatch.getNotes()),
                            org.apache.commons.lang.StringUtils.trimToNull(losingTransaction.getNotes())));

                    if (winningTransactionMatchModified) {
                        transactionDebugLog(losingTransaction, String.format(
                                "The matched winning transaction %s was modified. Resaving it.",
                                winningTransactionMatch));
                        transactionsToSave.add(winningTransactionMatch);
                    }
                } else {
                    // We could not find a transaction match in the winningCredentials. Migrating the transaction.

                    transactionDebugLog(losingTransaction,
                            "We could not find a transaction match in the winningCredentials. Migrating the transaction.");

                    Transaction newTransaction = losingTransaction.clone();
                    newTransaction.setId(StringUtils.generateUUID());
                    newTransaction.setCredentialsId(winnerCredentials.getId());
                    newTransaction.setAccountId(winningAccount.getId());
                    transactionsToSave.add(newTransaction);

                }
                
            } else {
                // (!winningAccountsByBankId.containsKey(thisTransactionsAccount.getBankId()))
                // If the transaction's account doesn't exist, we...

                transactionDebugLog(losingTransaction,
                        "The losing transaction did not have a matching account.");

                // ...1) create the account (once).
                if (!accountsToCreateByBankId.containsKey(thisTransactionsAccount.getBankId())) {
                    Account newAccount = thisTransactionsAccount.clone();
                    newAccount.setId(StringUtils.generateUUID());

                    transactionDebugLog(
                            losingTransaction,
                            String.format("Generating a new account for the transaction. Account id: %s",
                                    newAccount.getId()));

                    accountsToCreateByBankId.put(newAccount.getBankId(), newAccount);
                }

                transactionDebugLog(losingTransaction,
                        "Migrating the transaction since we know for certain it doesn't have a matching account.");

                // ...2) migrate the transaction over to the winning credentials.
                Transaction newTransaction = losingTransaction.clone();
                newTransaction.setId(StringUtils.generateUUID());
                newTransaction.setCredentialsId(winnerCredentials.getId());
                newTransaction.setAccountId(accountsToCreateByBankId.get(thisTransactionsAccount.getBankId()).getId());
                transactionsToSave.add(newTransaction);
                
            }

        }

        MergeExecutor executor = new MergeExecutor();
        executor.setAccountsToSave(accountsToCreateByBankId.values());
        executor.setLosingCredentials(losingCredentials);
        executor.setTransactionsToDelete(Lists.newArrayList(orphanTransactions));
        executor.setTransactionsToSave(transactionsToSave);
        return executor;
    }

    private void transactionDebugLog(Transaction losingTransaction, String message) {
        log.debug(losingTransaction.getUserId(), losingTransaction.getCredentialsId(),
                String.format("[%s] %s", losingTransaction, message));
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        log.info("Cleaning up duplicate credentials.");

        // Command line parameters.

        // !dryRun.
        final boolean actuallyExecute = Boolean.getBoolean("execute");

        final String userIdPrefix = System.getProperty("userIdPrefix", "");

        final String DEFAULT_RATE = "0.033"; // Basically once every 30 seconds.
        final double ratePerSecond = Double.parseDouble(System.getProperty("ratePerSecond", DEFAULT_RATE));
        final RateLimiter rateLimiter = RateLimiter.create(ratePerSecond, 30, TimeUnit.SECONDS);

        serviceFactory = serviceContext.getServiceFactory();

        // Repositories.

        credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        accountRepository = serviceContext.getRepository(AccountRepository.class);
        userRepository = serviceContext.getRepository(UserRepository.class);
        transactionDao = serviceContext.getDao(TransactionDao.class);
        
        // Making sure services that should be reachable are reachable. Better to fail early.

        serviceFactory.getUserService().ping("");
        serviceContext.getSystemServiceFactory().getUpdateService().ping();
        if (serviceContext.isUseAggregationController()) {
            serviceContext.getAggregationControllerCommonClient().ping();
        } else {
            serviceContext.getAggregationServiceFactory().getAggregationService().ping();
        }

        // Group identical credentials.

        Iterable<Credentials> allCredentials = Iterables.filter(credentialsRepository.findAll(),
                input -> input.getUserId().startsWith(userIdPrefix));

        ImmutableListMultimap<CredentialsGroupkey, Credentials> credentialsByDuplicateGroupKey = Multimaps.index(
                allCredentials, CredentialsGroupkey::from);

        // Filter out credentials that have duplicates to be able to log how many we found.
        
        Iterable<Entry<CredentialsGroupkey, Collection<Credentials>>> groups = Iterables.filter(
                credentialsByDuplicateGroupKey.asMap().entrySet(),
                input -> input.getValue().size() > 1);

        log.info(String.format("Found %d unique credentials that had duplicates.", Iterables.size(groups)));

        // Walk through each unique credentials (that is, a "group") and merge the duplicates.

        for (Entry<CredentialsGroupkey, Collection<Credentials>> entry : groups) {

            final CredentialsGroupkey key = entry.getKey();
            final Collection<Credentials> duplicateCredentials = entry.getValue();

            // Should have been filtered out above.
            Preconditions.checkState(entry.getValue().size() > 1);

            log.info(key.userId,
                    String.format("[%s] Processing %d identical credentials.", key, duplicateCredentials.size()));

            // ...identify which credentials we should keep among the duplicates.

            final Credentials winnerCredentials = selectWinner(key.userId, duplicateCredentials);
            log.debug(key.userId, String.format("Selected winner: %s", winnerCredentials));

            // For each of the duplicates in this group...

            for (final Credentials credentials : duplicateCredentials) {
                if (credentials.getId().equals(winnerCredentials.getId())) {
                    // Don't delete the winner.
                    continue;
                }

                rateLimiter.acquire();

                final Credentials losingCredentials = credentials;

                final MergeExecutor changes;
                try {
                    changes = prepareCredentialsMerge(losingCredentials, winnerCredentials);
                } catch (MergeNotSupportedException e) {
                    log.error("Could not constuct merge executor. Trying next credentials.", e);
                    continue;
                }
                Preconditions.checkNotNull(changes);

                // Modify persistence (databases and elastic search et al.) or just log it.

                if (actuallyExecute) {
                    // !Dry-run.
                    log.info(losingCredentials.getUserId(), losingCredentials.getId(), "Applying changes:");
                    changes.logChanges();
                    changes.execute();
                } else {
                    // Dry-run.
                    log.info(losingCredentials.getUserId(), losingCredentials.getId(),
                            "Changes that would have been applied:");
                    changes.logChanges();
                }
            }
        }

    }

    private Credentials selectWinner(String userId, Collection<Credentials> duplicateCredentials) {
        // General input validation.

        Preconditions.checkArgument(duplicateCredentials.size() > 1);
        for (Credentials credentials : duplicateCredentials) {
            Preconditions.checkArgument(credentials.getUserId().equals(userId), "UserId not equals.");
        }

        // Load all transactions for the user. Since they are modified in each iteration it's probably safer to fetch
        // them fresh from database for every call to this method.

        final List<Transaction> transactions = transactionDao.findAllByUserId(userId);

        // Count the credentials with most modified transactions as well as their transaction count.

        final HashMultiset<String> transactionsByCredentials = HashMultiset.create(duplicateCredentials.size());
        final HashMultiset<String> modifiedTransactionsByCredentials = HashMultiset.create(duplicateCredentials.size());
        for (Transaction transaction : transactions) {

            if (transaction.isUserModifiedAmount()) {
                modifiedTransactionsByCredentials.add(transaction.getCredentialsId());
            }
            if (transaction.isUserModifiedCategory()) {
                modifiedTransactionsByCredentials.add(transaction.getCredentialsId());
            }
            if (transaction.isUserModifiedDate()) {
                modifiedTransactionsByCredentials.add(transaction.getCredentialsId());
            }
            if (transaction.isUserModifiedDescription()) {
                modifiedTransactionsByCredentials.add(transaction.getCredentialsId());
            }
            if (transaction.isUserModifiedLocation()) {
                modifiedTransactionsByCredentials.add(transaction.getCredentialsId());
            }
            if (org.apache.commons.lang.StringUtils.trimToNull(transaction.getNotes()) != null) {
                modifiedTransactionsByCredentials.add(transaction.getCredentialsId());
            }
            if (transaction.getPayload().size() > 0) {
                modifiedTransactionsByCredentials.add(transaction.getCredentialsId());
            }

            transactionsByCredentials.add(transaction.getCredentialsId());
        }

        final Ordering<Credentials> candidateOrdering = new Ordering<Credentials>() {

            @Override
            public int compare(Credentials left, Credentials right) {
                return ComparisonChain
                        .start()
                        .compare(modifiedTransactionsByCredentials.count(left.getId()),
                                modifiedTransactionsByCredentials.count(right.getId()))
                        .compare(transactionsByCredentials.count(left.getId()),
                                transactionsByCredentials.count(right.getId())).result();
            }

        };

        return candidateOrdering.max(duplicateCredentials);
    }

}

class CredentialsGroupkey implements Comparable<CredentialsGroupkey> {
    public static CredentialsGroupkey from(Credentials credentials) {
        return new CredentialsGroupkey(credentials.getUserId(), credentials.getProviderName(), credentials.getFields());
    }

    public final Map<String, String> field;
    public final String providerName;
    public final String userId;

    private CredentialsGroupkey(String userId, String providername, Map<String, String> fields) {
        this.userId = userId;
        // Not normalizing provider name here, because users might actually want to have test out with *-bankid and we
        // shouldn't merge those.
        this.providerName = providername;
        this.field = Collections.unmodifiableMap(fields);
    }

    @Override
    public int compareTo(CredentialsGroupkey o) {
        return ComparisonChain.start().compare(this.userId, o.userId).compare(this.providerName, o.providerName)
                .result();
    }

    // Auto-generated by Eclipse.
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CredentialsGroupkey other = (CredentialsGroupkey) obj;
        if (field == null) {
            if (other.field != null) {
                return false;
            }
        } else if (!field.equals(other.field)) {
            return false;
        }
        if (providerName == null) {
            if (other.providerName != null) {
                return false;
            }
        } else if (!providerName.equals(other.providerName)) {
            return false;
        }
        if (userId == null) {
            if (other.userId != null) {
                return false;
            }
        } else if (!userId.equals(other.userId)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userId, providerName, field);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass()).add("userId", userId).add("providerName", providerName)
                .add("fields", field).toString();
    }
}

class MergeNotSupportedException extends Exception {

    private static final long serialVersionUID = 776723736462057161L;

    public MergeNotSupportedException(String message) {
        super(message);
    }

}

/**
 * A normalized providername independent of bankid or not.
 */
class NormalizedProviderName {

    private static String normalizeProvidername(String originalProvidername) {
        Preconditions.checkNotNull(originalProvidername);

        if (!originalProvidername.endsWith("-bankid")) {
            return String.format("%s-bankid", originalProvidername);
        } else {
            return originalProvidername;
        }
    }

    private String normalizedName;

    private String originalName;

    NormalizedProviderName(String providerName) {
        this.originalName = providerName;
        this.normalizedName = normalizeProvidername(providerName);
    }

    // Auto-generated by Eclipse.
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NormalizedProviderName other = (NormalizedProviderName) obj;
        if (normalizedName == null) {
            if (other.normalizedName != null) {
                return false;
            }
        } else if (!normalizedName.equals(other.normalizedName)) {
            return false;
        }
        return true;
    }

    // Auto-generated by Eclipse.
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((normalizedName == null) ? 0 : normalizedName.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass()).add("normalizedName", normalizedName)
                .add("originalName", originalName).toString();
    }

}

class TransactionGroupKey {
    public static TransactionGroupKey from(Map<String, Account> accountsById, Transaction transaction) {
        return new TransactionGroupKey(transaction.getUserId(), accountsById.get(transaction.getAccountId())
                .getBankId(), transaction.getOriginalAmount(),
                transaction.getOriginalDate(), transaction.getOriginalDescription());
    }

    public final String accountBankID;
    public final double originalAmount;
    public final Date originalDate;
    public final String originalDescription;
    public final String userId;

    public TransactionGroupKey(String userId, String accountBankID, double originalAmount, Date originalDate,
            String originalDescription) {
        this.userId = userId;
        this.originalAmount = originalAmount;
        this.originalDate = originalDate;
        this.originalDescription = originalDescription;
        this.accountBankID = accountBankID;
    }

    // Auto-generated by Eclipse.
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TransactionGroupKey other = (TransactionGroupKey) obj;
        if (accountBankID == null) {
            if (other.accountBankID != null) {
                return false;
            }
        } else if (!accountBankID.equals(other.accountBankID)) {
            return false;
        }
        if (Double.doubleToLongBits(originalAmount) != Double.doubleToLongBits(other.originalAmount)) {
            return false;
        }
        if (originalDate == null) {
            if (other.originalDate != null) {
                return false;
            }
        } else if (!originalDate.equals(other.originalDate)) {
            return false;
        }
        if (originalDescription == null) {
            if (other.originalDescription != null) {
                return false;
            }
        } else if (!originalDescription.equals(other.originalDescription)) {
            return false;
        }
        if (userId == null) {
            if (other.userId != null) {
                return false;
            }
        } else if (!userId.equals(other.userId)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(originalAmount, originalDate, originalDescription, userId, accountBankID);
    }
}
