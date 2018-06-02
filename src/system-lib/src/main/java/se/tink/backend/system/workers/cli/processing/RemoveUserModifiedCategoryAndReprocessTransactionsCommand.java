package se.tink.backend.system.workers.cli.processing;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.inject.Injector;
import com.google.inject.util.Providers;
import io.dropwizard.setup.Bootstrap;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.lang.time.StopWatch;
import org.elasticsearch.client.Client;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.categorization.factory.FastTextInProcessCategorizerFactory;
import se.tink.backend.categorization.interfaces.CategorizerFactory;
import se.tink.backend.categorization.lookup.CitiesByMarket;
import se.tink.backend.categorization.rules.LabelIndexCache;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.CategorizationConfiguration;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.CategoryChangeRecordDao;
import se.tink.backend.common.dao.ProviderDao;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.product.targeting.TargetProductsRunnableFactory;
import se.tink.backend.common.repository.cassandra.CategoryChangeRecordRepository;
import se.tink.backend.common.repository.cassandra.DAO.LoanDAO;
import se.tink.backend.common.repository.cassandra.ExternallyDeletedTransactionRepository;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.cassandra.TransactionExternalIdRepository;
import se.tink.backend.common.dao.transactions.TransactionRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.GiroRepository;
import se.tink.backend.common.repository.mysql.main.MarketRepository;
import se.tink.backend.common.repository.mysql.main.MerchantRepository;
import se.tink.backend.common.repository.mysql.main.PostalCodeAreaRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.search.SimilarTransactionsSearcher;
import se.tink.backend.common.search.client.ElasticSearchClient;
import se.tink.backend.core.Category;
import se.tink.backend.core.ClusterCategories;
import se.tink.backend.core.Market;
import se.tink.backend.core.StatisticMode;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.UserData;
import se.tink.backend.firehose.v1.queue.DummyFirehoseQueueProducer;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.cli.helper.traversal.CommandLineInterfaceUserTraverser;
import se.tink.backend.system.rpc.GenerateStatisticsAndActivitiesRequest;
import se.tink.backend.system.workers.processor.TransactionProcessor;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;
import se.tink.backend.system.workers.processor.chaining.OxfordReprocessingChainFactory;
import se.tink.backend.system.workers.processor.formatting.MarketDescriptionExtractorFactory;
import se.tink.backend.system.workers.processor.formatting.MarketDescriptionFormatterFactory;
import se.tink.backend.system.workers.processor.transfers.scoring.TransferDetectionScorerFactory;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.metrics.MetricRegistry;

public class RemoveUserModifiedCategoryAndReprocessTransactionsCommand
        extends ServiceContextCommand<ServiceConfiguration> {

    public static final DummyFirehoseQueueProducer firehoseQueueProducer = new DummyFirehoseQueueProducer();

    private AccountRepository accountRepository;
    private CategoryRepository categoryRepository;
    private CredentialsRepository credentialsRepository;
    private TransactionRepository transactionRepository;
    private UserRepository userRepository;
    private LoanDataRepository loanDataRepository;
    private ElasticSearchClient elasticSearchClient;

    private Category unknownIncomeCategory;
    private Category unknownExpenseCategory;
    private ImmutableMap<String, Category> categoriesById;
    private ImmutableMap<String, Market> marketsByCode;
    private MarketDescriptionFormatterFactory descriptionFormatterFactory;
    private MarketDescriptionExtractorFactory descriptionExtractorFactory;

    public RemoveUserModifiedCategoryAndReprocessTransactionsCommand() {
        super("remove-user-modified-reprocess-transactions",
                "Remove user modified category and reprocess all transactions");
    }

    private static final LogUtils log = new LogUtils(TransactionProcessor.class);

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, final ServiceContext serviceContext)
            throws Exception {

        String userIdsFile = System.getProperty("userIdsFile");
        Preconditions.checkArgument(userIdsFile != null, "'Must specify file with user IDs.");
        File userIdFilterFile = new File(userIdsFile);
        Set<String> userIds = Sets.newHashSet(Files.readLines(userIdFilterFile, Charsets.UTF_8));

        String userTransactionCategoriesFileName = System.getProperty("userTransactionCategoriesFile");
        Preconditions.checkArgument(userTransactionCategoriesFileName != null,
                "'Must specify file with user transactions and their categories.");
        File userTransactionCategoriesFile = new File(userTransactionCategoriesFileName);

        Map<String, List<String>> transactionsWithCategories = Files
                .readLines(userTransactionCategoriesFile, Charsets.UTF_8)
                .stream()
                .map(StringUtils::parseCSV).collect(
                        Collectors.toMap(lst -> (String) lst.get(3), lst -> lst));
        elasticSearchClient = injector.getInstance(ElasticSearchClient.class);
        accountRepository = serviceContext.getRepository(AccountRepository.class);
        categoryRepository = serviceContext.getRepository(CategoryRepository.class);
        credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        transactionRepository = serviceContext.getRepository(TransactionRepository.class);
        userRepository = serviceContext.getRepository(UserRepository.class);
        loanDataRepository = serviceContext.getRepository(LoanDataRepository.class);

        Cluster cluster = serviceContext.getConfiguration().getCluster();
        descriptionFormatterFactory = MarketDescriptionFormatterFactory.byCluster(cluster);
        descriptionExtractorFactory = MarketDescriptionExtractorFactory.byCluster(cluster);

        MetricRegistry metricRegistry = injector.getInstance(MetricRegistry.class);

        unknownIncomeCategory = categoryRepository
                .findByCode(serviceContext.getCategoryConfiguration().getIncomeUnknownCode());
        unknownExpenseCategory = categoryRepository
                .findByCode(serviceContext.getCategoryConfiguration().getExpenseUnknownCode());

        categoriesById = Maps.uniqueIndex(categoryRepository.findAll(), Category::getId);

        marketsByCode = Maps.uniqueIndex(serviceContext.getRepository(MarketRepository.class).findAll(),
                m -> (m.getCodeAsString()));

        final TransactionProcessor transactionProcessor = new TransactionProcessor(
                metricRegistry
        );

        StopWatch watch = new StopWatch();
        watch.start();

        final AtomicInteger userCount = new AtomicInteger();

        userRepository.streamAll()
                .compose(new CommandLineInterfaceUserTraverser(20))
                .filter(u -> {
                    if (userIds.contains(u.getId())) {
                        return true;
                    }
                    return false;
                })
                .forEach(user -> {
                    userCount.incrementAndGet();
                    process(serviceContext, transactionProcessor, user, metricRegistry, transactionsWithCategories);
                });

        log.info(String.format("Processed %d users", userCount.get()));
        log.info("Processed transactions in " + watch.toString());
    }

    private void process(ServiceContext serviceContext, TransactionProcessor transactionProcessor, User user,
            MetricRegistry metricRegistry, Map<String, List<String>> transactionsWithCategories) {
        try {
            List<Transaction> allTransactions = transactionRepository.findAllByUserId(user.getId());

            ImmutableListMultimap<String, Transaction> transactionsByCredentialsId = Multimaps.index(
                    allTransactions, Transaction::getCredentialsId);

            // Construct the command chain.

            UserData userData = loadUserData(user);

            final CitiesByMarket citiesByMarket;
            if (serviceContext.isProvidersOnAggregation()) {
                citiesByMarket = CitiesByMarket.build(
                        serviceContext.getAggregationControllerCommonClient().listProviders());
            } else {
                citiesByMarket = CitiesByMarket.build(serviceContext.getRepository(ProviderRepository.class).findAll());
            }

            final LabelIndexCache labelIndexCache = LabelIndexCache
                    .build(serviceContext.getConfiguration().getCluster());

            TargetProductsRunnableFactory targetProductsRunnableFactory = new TargetProductsRunnableFactory(
                    serviceContext);
            LoanDAO loanDAO = serviceContext.getDao(LoanDAO.class);
            Client searchClient = serviceContext.getSearchClient();
            PostalCodeAreaRepository postalCodeAreaRepository = serviceContext
                    .getRepository(PostalCodeAreaRepository.class);
            CategoryConfiguration categoryConfiguration = serviceContext.getCategoryConfiguration();
            SimilarTransactionsSearcher similarTransactionsSearcher =
                    elasticSearchClient.getSimilarTransactionsSearcher();
            ClusterCategories categories = new ClusterCategories(
                    serviceContext.getRepository(CategoryRepository.class).findAll());
            TransactionExternalIdRepository transactionExternalIdRepository = serviceContext
                    .getRepository(TransactionExternalIdRepository.class);
            ExternallyDeletedTransactionRepository externallyDeletedTransactionRepository = serviceContext
                    .getRepository(ExternallyDeletedTransactionRepository.class);
            CategorizationConfiguration categorizationConfiguration = serviceContext.getConfiguration()
                    .getCategorization();

            CategorizerFactory oxfordFasttextCategorizerFactory = new FastTextInProcessCategorizerFactory(
                    serviceContext.getConfiguration().getCluster(),
                    categoryConfiguration, categorizationConfiguration,
                    metricRegistry, categories, similarTransactionsSearcher,
                    new File(categorizationConfiguration.getExecutable()),
                    categorizationConfiguration.getFastTextCategorizers(),
                    categorizationConfiguration.getFastTextIncomeCategorizers(), "oxford-fasttext");

            OxfordReprocessingChainFactory oxfordReprocessingChainFactory = new OxfordReprocessingChainFactory(
                    Cluster.TINK,
                    citiesByMarket,
                    new ClusterCategories(categoriesById.values()),
                    categoryConfiguration,
                    targetProductsRunnableFactory,
                    firehoseQueueProducer,
                    metricRegistry,
                    serviceContext.getRepository(MerchantRepository.class),
                    serviceContext.getRepository(GiroRepository.class),
                    labelIndexCache,
                    loanDAO,
                    serviceContext.getExecutorService(),
                    serviceContext.getDao(TransactionDao.class),
                    TransferDetectionScorerFactory
                            .byCluster(serviceContext.getConfiguration().getCluster()),
                    serviceContext.getSystemServiceFactory(),
                    accountRepository,
                    similarTransactionsSearcher,
                    Providers.of(MarketDescriptionFormatterFactory.byCluster(Cluster.TINK)),
                    Providers.of(MarketDescriptionExtractorFactory.byCluster(Cluster.TINK)),
                    oxfordFasttextCategorizerFactory,
                    new CategoryChangeRecordDao(
                            serviceContext.getRepository(CategoryChangeRecordRepository.class), metricRegistry),
                    externallyDeletedTransactionRepository,
                    transactionExternalIdRepository,
                    categorizationConfiguration);

            // Process the transactions for each credentials.
            for (String credentialsId : transactionsByCredentialsId.keySet()) {

                List<Transaction> transactions = transactionsByCredentialsId.get(credentialsId);
                for (Transaction transaction : transactions) {
                    if (transactionsWithCategories.containsKey(transaction.getId())) {
                        setCategory(transaction, transactionsWithCategories.get(transaction.getId()).get(16));
                    } else {
                        resetCategory(transaction);
                    }
                }
                userData.getAccounts().forEach(a -> a.setCertainDate(null));
                TransactionProcessorContext context = new TransactionProcessorContext(
                        user,
                        serviceContext.getDao(ProviderDao.class).getProvidersByName(),
                        transactions,
                        userData,
                        credentialsId
                );

                log.info(user.getId(), credentialsId, String.format(
                        "Reprocessing %d transactions", transactions.size()));

                transactionProcessor.processTransactions(
                        context,
                        oxfordReprocessingChainFactory
                        , userData, false
                );
            }

            // Regenerate user statistics

            log.info(user.getId(), "Generating statistics");

            GenerateStatisticsAndActivitiesRequest statisticsRequest = new GenerateStatisticsAndActivitiesRequest();
            statisticsRequest.setUserId(user.getId());
            statisticsRequest.setMode(StatisticMode.FULL);

            serviceContext.getSystemServiceFactory().getProcessService()
                    .generateStatisticsAndActivitySynchronous(statisticsRequest);

        } catch (Exception e) {
            log.error(user.getId(), "Failed to reprocess transactions.", e);
        }

    }

    private void resetCategory(Transaction transaction) {

        Category category = getDefaultCategory(transaction);

        transaction.setCategory(category);
        transaction.setUserModifiedCategory(false);
    }

    private void setCategory(Transaction transaction, String categoryId) {
        Category category = categoryRepository.findById(categoryId);

        transaction.setCategory(category);
        transaction.setUserModifiedCategory(true);

    }

    private Category getDefaultCategory(Transaction transaction) {
        if (transaction.getAmount() > 0) {
            return unknownIncomeCategory;
        } else {
            return unknownExpenseCategory;
        }
    }

    private UserData loadUserData(User user) {
        UserData userData = new UserData();

        userData.setUser(user);
        userData.setTransactions(transactionRepository.findAllByUserId(user.getId()));
        userData.setAccounts(accountRepository.findByUserId(user.getId()));
        userData.setCredentials(credentialsRepository.findAllByUserId(user.getId()));

        return userData;
    }
}
