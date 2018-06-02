package se.tink.backend.common.search;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Doubles;
import com.google.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.lang.ObjectUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.cache.CacheScope;
import se.tink.backend.common.dao.CategoryDao;
import se.tink.backend.common.exceptions.LockException;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.search.cluster.LundgrenHedbergClusterer;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.core.Account;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Currency;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionCluster;
import se.tink.backend.core.User;
import se.tink.backend.rpc.SuggestTransactionsResponse;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.guavaimpl.predicates.AccountPredicate;

/**
 * Suggest significant transactions for the user to categorize.
 */
public class SuggestTransactionsSearcher {
    /**
     * The minimum cluster score to suggest to the user.
     */
    private static final double MIN_CLUSTER_SCORE = 1500;

    /**
     * The score factor for categorized transactions within a cluster
     * (categorized transactions are only included if the absolute amount is
     * significant).
     */
    private static final double AMOUNT_SCORE_FACTOR_CATEGORIZED = 0.25;

    /**
     * The score factor for uncategorized transactions within a cluster.
     */
    private static final double AMOUNT_SCORE_FACTOR_UNCATEGORIZED = 0.50;

    private static final LogUtils log = new LogUtils(SuggestTransactionsSearcher.class);

    /**
     * The minimum JaroWinkler value of transactions within a considered
     * cluster.
     */
    private static final double MIN_JARO_WINKLER_THREASHOLD = 0.7;

    /**
     * The minimum number of transactions within a cluster (we're fine with
     * choosing a single transaction "cluster").
     */
    private static final int MIN_NUMBER_OF_ITEMS_IN_CLUSTER = 1;

    /**
     * The minimum number of characters in a cluster pattern. If the pattern is
     * smaller, the cluster if probably not very likely to make sense.
     */
    private static final int MIN_NUMBER_OF_RULES_CHARS_IN_CLUSTER = 4;

    /**
     * The absolute amount of a transaction of type expenses that should be
     * considered significant despite being already categorized.
     */
    private static final double MIN_SIGNIFICANT_CATEGORIZED_AMOUNT_EXPENSES = 5000;

    private static final MetricId SUGGEST_TRANSACTIONS = MetricId.newId("suggest_transactions");
    private final Timer timer;

    private static Ordering<Transaction> SUGGEST_ORDERING = new Ordering<Transaction>() {
        @Override
        public int compare(Transaction left, Transaction right) {
            return Doubles.compare(left.getAmount(), right.getAmount());
        }
    };

    private static Ordering<TransactionCluster> TRANSACTION_CLUSTER_SCORE_ORDERING = Ordering.natural().reverse()
            .onResultOf(
                    TransactionCluster::getScore);

    private static final String LOCK_PREFIX_USER = "/locks/suggestTransactions/user/";

    private final CacheClient cacheClient;
    private final LundgrenHedbergClusterer clusterer;
    private final Category uncategorizedCategory;
    private final CuratorFramework coordinationClient;
    private final AccountRepository accountRepository;
    private final ImmutableMap<String, Currency> currenciesByCode;
    private final Timer acquireLockTimer;

    private SuggestTransactionsSearcher(LundgrenHedbergClusterer clusterer, CategoryDao categoryDao,
            ImmutableMap<String, Currency> currenciesByCode, AccountRepository accountRepository,
            CacheClient cacheClient, CuratorFramework coordinationClient, MetricRegistry metricRegistry) {
        this.clusterer = clusterer;
        this.uncategorizedCategory = categoryDao.getUnknownExpense();
        this.currenciesByCode = currenciesByCode;
        this.accountRepository = accountRepository;
        this.cacheClient = cacheClient;
        this.coordinationClient = coordinationClient;
        this.acquireLockTimer = metricRegistry.timer(MetricId.newId("suggest_transaction_acquire_lock"));
        this.timer = metricRegistry.timer(SUGGEST_TRANSACTIONS);
    }

    @Inject
    public SuggestTransactionsSearcher(CategoryDao categoryDao, ImmutableMap<String, Currency> currenciesByCode,
            AccountRepository accountRepository, CacheClient cacheClient, CuratorFramework coordinationClient,
            MetricRegistry metricRegistry) {
        this(new LundgrenHedbergClusterer(MIN_NUMBER_OF_ITEMS_IN_CLUSTER,
                        MIN_NUMBER_OF_RULES_CHARS_IN_CLUSTER, MIN_JARO_WINKLER_THREASHOLD), categoryDao,
                currenciesByCode, accountRepository, cacheClient, coordinationClient, metricRegistry);
    }

    /**
     * Use constructor which receives all needed object instead.
     */
    @Deprecated
    public SuggestTransactionsSearcher(ServiceContext context, Map<String, Currency> currenciesByCode,
            MetricRegistry metricRegistry) {
        this(new CategoryDao(context.getRepository(CategoryRepository.class), context.getCategoryConfiguration()),
                ImmutableMap.copyOf(currenciesByCode), context.getRepository(AccountRepository.class),
                context.getCacheClient(), context.getCoordinationClient(), metricRegistry);
    }

    /**
     * Find transaction clusters using the Lundgren-Hedberg algorithm and
     * calculate the cluster score for each of the clusters.
     *
     * @param transactions        The transaction list.
     * @param transactionClusters The transaction cluster list to add the clusters to.
     */
    private void clusterTransactions(List<Transaction> transactions, List<TransactionCluster> transactionClusters) {
        Collection<TransactionCluster> newTransactionClusters = clusterer.findClustersQuickly(transactions);

        for (TransactionCluster cluster : newTransactionClusters) {
            double scoreOfTransactions = 0;

            for (Transaction t : cluster.getTransactions()) {
                switch (t.getCategoryType()) {
                    case INCOME:
                        scoreOfTransactions += (Math.abs(t.getAmount()) * AMOUNT_SCORE_FACTOR_CATEGORIZED);
                        break;
                    case EXPENSES:
                        if (ObjectUtils.equals(t.getCategoryId(), uncategorizedCategory.getId())) {
                            scoreOfTransactions += (Math.abs(t.getAmount()) * AMOUNT_SCORE_FACTOR_UNCATEGORIZED);
                        } else {
                            scoreOfTransactions += (Math.abs(t.getAmount()) * AMOUNT_SCORE_FACTOR_CATEGORIZED);
                        }

                        break;
                    default:
                        break;
                }
            }

            cluster.setScore(scoreOfTransactions);

            transactionClusters.add(cluster);
        }
    }

    /**
     * Suggest transaction clusters to the user to categorize.
     *
     * @param enabledAccountsTransactions
     * @param user                        The user.
     * @return The transaction clusters.
     */
    private List<TransactionCluster> findSuggestedTransactionClusters(Iterable<Transaction> enabledAccountsTransactions,
            User user) {
        List<TransactionCluster> clusters = Lists.newArrayList();

        // Fetch transactions to suggest changes to.

        final double currencyFactor = currenciesByCode.get(user.getProfile().getCurrency()).getFactor();

        Iterable<Transaction> suggestTransactions = StreamSupport
                .stream(enabledAccountsTransactions.spliterator(), false)
                .filter(transaction -> (!Objects.equal(transaction.getCategoryType(), CategoryTypes.TRANSFERS) &&
                        !Objects.equal(transaction.getCategoryType(), CategoryTypes.INCOME) &&
                        !transaction.isPending() &&
                        !transaction.isUserModifiedCategory() &&
                        (Objects.equal(transaction.getCategoryId(), uncategorizedCategory.getId()) ||
                                (Objects.equal(transaction.getCategoryType(), CategoryTypes.EXPENSES) &&
                                        Math.abs(transaction.getAmount())
                                                > MIN_SIGNIFICANT_CATEGORIZED_AMOUNT_EXPENSES * currencyFactor))))
                .collect(Collectors.toList());

        List<Transaction> transactions = SUGGEST_ORDERING.sortedCopy(suggestTransactions);

        if (transactions.isEmpty()) {
            return clusters;
        }

        // Find transaction clusters based on their similarity in description.

        clusterTransactions(transactions, clusters);

        // Sort the clusters.

        Collections.sort(clusters, TRANSACTION_CLUSTER_SCORE_ORDERING);

        return clusters;
    }

    public SuggestTransactionsResponse suggest(User user, int numberOfClusters, final boolean evaluateEverything,
            List<Transaction> transactions) throws LockException {

        List<Account> accounts = accountRepository.findByUserId(user.getId());

        return suggest(user, numberOfClusters, evaluateEverything, transactions, accounts);
    }

    /**
     * Suggest transaction clusters to the user to categorize.
     *
     * @param user               The user ID.
     * @param evaluateEverything
     * @param numberOfClusters
     * @param evaluateEverything
     * @return The transaction clusters.
     * @throws LockException
     */
    public SuggestTransactionsResponse suggest(User user, int numberOfClusters, final boolean evaluateEverything,
            List<Transaction> transactions, List<Account> accounts) throws LockException {

        InterProcessSemaphoreMutex lock = new InterProcessSemaphoreMutex(coordinationClient,
                LOCK_PREFIX_USER + user.getId());

        SuggestTransactionsResponse response;

        try {

            Timer.Context acquireLockTimerContext = acquireLockTimer.time();
            try {
                if (!lock.acquire(15, TimeUnit.SECONDS)) {
                    log.warn(user.getId(), "Timeout while waiting for suggested transactions, continuing anyways");
                }
            } catch (Exception e) {
                throw new LockException("Cannot acquire lock for suggesting transactions", e);
            }

            acquireLockTimerContext.stop();
            response = SerializationUtils.deserializeFromBinary(
                    (byte[]) cacheClient.get(CacheScope.SUGGEST_TRANSACTIONS_RESPONSE_BY_USERID, user.getId()),
                    SuggestTransactionsResponse.class);

            if (response == null) {
                // Cache  miss.
                Timer.Context timerContext = timer.time();

                List<Transaction> filteredTransactions = filterTransactionsOnIncludedAccounts(accounts, transactions);

                long numberOfTransactions = filteredTransactions.size();
                long numberOfUncategorizedTransactions = 0;

                response = new SuggestTransactionsResponse();

                if (numberOfTransactions > 0) {
                    double amountUncategorized = 0;
                    double amount = 0;

                    for (Transaction t : filteredTransactions) {
                        amount += Math.abs(t.getAmount());

                        if (Objects.equal(t.getCategoryId(), uncategorizedCategory.getId())) {
                            amountUncategorized += Math.abs(t.getAmount());
                            numberOfUncategorizedTransactions++;
                        }
                    }

                    List<TransactionCluster> clusters = findSuggestedTransactionClusters(filteredTransactions, user);

                    double categorizationImprovement = 0;

                    for (TransactionCluster cluster : clusters) {
                        double amountUncategorizedCluster = 0;

                        for (Transaction transaction : cluster.getTransactions()) {
                            if (Objects.equal(transaction.getCategoryId(), uncategorizedCategory.getId())) {
                                amountUncategorizedCluster += Math.abs(transaction.getAmount());
                            }
                        }

                        double categorizationImprovementCluster = amountUncategorizedCluster / amount;

                        categorizationImprovement += categorizationImprovementCluster;

                        cluster.setCategorizationImprovement(categorizationImprovementCluster);
                    }

                    response.setClusters(clusters);
                    response.setCategorizationLevel((amount - amountUncategorized) / amount);
                    response.setCategorizationImprovement(categorizationImprovement);
                }

                if (response.getClusters() == null) {
                    response.setClusters(Lists.<TransactionCluster>newArrayList());
                }

                response.setNumberOfTransactions(numberOfTransactions);
                response.setNumberOfUncategorizedTransactions(numberOfUncategorizedTransactions);

                timerContext.stop();

                if (response.getClusters() != null) {
                    response.setNumberOfClusters(response.getClusters().size());
                }

                cacheClient.set(CacheScope.SUGGEST_TRANSACTIONS_RESPONSE_BY_USERID, user.getId(),
                        SuggestTransactionsResponse.CACHE_EXPIRY, SerializationUtils.serializeToBinary(response));
            }

            if (response.getClusters() != null) {
                List<TransactionCluster> clusters = (response.getClusters().subList(0,
                        Math.min(response.getClusters().size(), numberOfClusters)));

                response.setClusters(clusters.stream()
                        .filter(cluster -> (evaluateEverything || cluster.getScore() > MIN_CLUSTER_SCORE))
                        .collect(Collectors.toList()));

                double categorizationImprovement = 0;

                for (TransactionCluster tc : response.getClusters()) {
                    categorizationImprovement += tc.getCategorizationImprovement();
                }

                response.setCategorizationImprovement(categorizationImprovement);
            }
        } finally {
            if (lock.isAcquiredInThisProcess()) {
                try {
                    lock.release();
                } catch (Exception e) {
                    log.error(user.getId(), "Cannot release lock for suggesting transactions", e);
                }
            } else {
                log.warn(user.getId(), "Write lock is not acquired in this process");
            }
        }

        return response;
    }

    /**
     * Filter away transactions that belongs to accounts that are excluded. Transactions belonging to accounts that
     * aren't in the input will also be excluded.
     * <p/>
     * Public to be able to test it without doing large
     *
     * @param accounts
     * @param transactions
     * @return List of transactions for included accounts.
     */
    public static List<Transaction> filterTransactionsOnIncludedAccounts(List<Account> accounts,
            final List<Transaction> transactions) {

        Preconditions.checkNotNull(accounts, "Accounts cannot be null");
        Preconditions.checkNotNull(transactions, "Transactions cannot be null");

        final Set<String> includedAccounts = accounts.stream().filter(AccountPredicate.IS_NOT_EXCLUDED::apply)
                .map(Account::getId).collect(Collectors.toSet());

        return transactions.stream().filter(t -> includedAccounts.contains(t.getAccountId()))
                .collect(Collectors.toList());
    }
}
