package se.tink.backend.system.workers.cli.processing;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.lang.time.StopWatch;
import se.tink.backend.categorization.lookup.CitiesByMarket;
import se.tink.backend.categorization.rules.LabelIndexCache;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ReprocessTransactionsConfiguration;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.CategoryChangeRecordDao;
import se.tink.backend.common.dao.ProviderDao;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.GiroRepository;
import se.tink.backend.common.repository.mysql.main.MarketRepository;
import se.tink.backend.common.repository.mysql.main.MerchantRepository;
import se.tink.backend.common.repository.mysql.main.PostalCodeAreaRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
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
import se.tink.backend.system.workers.processor.chaining.ReprocessingChainFactory;
import se.tink.backend.system.workers.processor.formatting.MarketDescriptionExtractorFactory;
import se.tink.backend.system.workers.processor.formatting.MarketDescriptionFormatterFactory;
import se.tink.backend.system.workers.processor.transfers.scoring.TransferDetectionScorerFactory;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.metrics.MetricRegistry;

public class ReprocessTransactionsCommand extends ServiceContextCommand<ServiceConfiguration> {

    public static final DummyFirehoseQueueProducer firehoseQueueProducer = new DummyFirehoseQueueProducer();

    private AccountRepository accountRepository;
    private CategoryRepository categoryRepository;
    private CredentialsRepository credentialsRepository;
    private TransactionDao transactionDao;
    private UserRepository userRepository;
    private LoanDataRepository loanDataRepository;

    private ElasticSearchClient elasticSearchClient;
    private Category unknownIncomeCategory;
    private Category unknownExpenseCategory;
    private ImmutableMap<String, Category> categoriesById;
    private ImmutableMap<String, Market> marketsByCode;
    private MarketDescriptionFormatterFactory descriptionFormatterFactory;
    private MarketDescriptionExtractorFactory descriptionExtractorFactory;

    public ReprocessTransactionsCommand() {
        super("reprocess-transactions", "Reprocess all transactions");
    }

    private static final LogUtils log = new LogUtils(TransactionProcessor.class);

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, final ServiceContext serviceContext)
            throws Exception {

        accountRepository = serviceContext.getRepository(AccountRepository.class);
        categoryRepository = serviceContext.getRepository(CategoryRepository.class);
        credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        transactionDao = serviceContext.getDao(TransactionDao.class);
        userRepository = serviceContext.getRepository(UserRepository.class);
        loanDataRepository = serviceContext.getRepository(LoanDataRepository.class);

        Cluster cluster = serviceContext.getConfiguration().getCluster();
        descriptionFormatterFactory = MarketDescriptionFormatterFactory.byCluster(cluster);
        descriptionExtractorFactory = MarketDescriptionExtractorFactory.byCluster(cluster);

        MetricRegistry metricRegistry = injector.getInstance(MetricRegistry.class);
        elasticSearchClient = injector.getInstance(ElasticSearchClient.class);

        unknownIncomeCategory = categoryRepository
                .findByCode(serviceContext.getCategoryConfiguration().getIncomeUnknownCode());
        unknownExpenseCategory = categoryRepository
                .findByCode(serviceContext.getCategoryConfiguration().getExpenseUnknownCode());

        categoriesById = Maps.uniqueIndex(categoryRepository.findAll(), Category::getId);

        marketsByCode = Maps.uniqueIndex(serviceContext.getRepository(MarketRepository.class).findAll(),
                m -> (m.getCodeAsString()));

        ReprocessTransactionsConfiguration config = configuration.getReprocessTransactionsOptions();

        final TransactionProcessor transactionProcessor = new TransactionProcessor(
                metricRegistry
        );

        // get for which category

        List<String> categoryIds = Lists.newArrayList();
        List<String> categoryCodes = config.getCategoryCode();

        if (categoryCodes != null && categoryCodes.size() > 0) {
            for (String code : categoryCodes) {
                log.info(String.format("Including category '%s' in reprocessing", code));
                Category category = categoryRepository.findByCode(code);
                if (category != null) {
                    categoryIds.add(category.getId());
                } else { // terminate
                    log.error("Could not find categoryId to category code: " + config.getCategoryCode());
                    return;
                }
            }
        } else {
            // Reprocess for all categories
            log.info("Including all categories in reprocessing.");
        }

        // get for which description

        String description = null;
        if (config.getDescription() != null && config.getDescription().length() > 2) {
            description = config.getDescription();
        }

        final List<String> categoryIdsFinal = categoryIds;
        final String descriptionFinal = description;

        StopWatch watch = new StopWatch();
        watch.start();

        final AtomicInteger userCount = new AtomicInteger();

        userRepository.streamAll()
                .compose(new CommandLineInterfaceUserTraverser(20))
                .forEach(user -> {
                    userCount.incrementAndGet();
                    process(serviceContext, transactionProcessor, user, categoryIdsFinal, descriptionFinal,
                            metricRegistry);
                });

        log.info(String.format("Processed %d users", userCount.get()));
        log.info("Processed transactions in " + watch.toString());
    }

    private void process(ServiceContext serviceContext, TransactionProcessor transactionProcessor, User user,
            final List<String> categoryIdsFinal, final String descriptionFinal, MetricRegistry metricRegistry) {
        try {
            List<Transaction> allTransactions = transactionDao.findAllByUserId(user.getId());

            // Reset invalid categorization before filtering.
            for (Transaction transaction : allTransactions) {
                if (!hasValidCategory(transaction)) {
                    resetCategory(transaction);
                }
            }

            List<Transaction> filteredTransactions = Lists.newArrayList(Iterables.filter(allTransactions,
                    t -> {
                        if (categoryIdsFinal.size() == 0 && descriptionFinal == null) {
                            return true;
                        }
                        if (t.getCategoryId() != null && categoryIdsFinal.contains(t.getCategoryId())) {
                            return true;
                        }
                        if (t.getDescription() != null && t.getDescription().equals(descriptionFinal)) {
                            return true;
                        }
                        return false;
                    }));

            if (filteredTransactions.size() == 0) {
                return;
            }

            ImmutableListMultimap<String, Transaction> transactionsByCredentialsId = Multimaps.index(
                    filteredTransactions, Transaction::getCredentialsId);

            // Construct the command chain.

            UserData userData = loadUserData(user);
            Market market = marketsByCode.get(user.getProfile().getMarket());

            final CitiesByMarket citiesByMarket;
            if (serviceContext.isProvidersOnAggregation()) {
                citiesByMarket = CitiesByMarket.build(
                        serviceContext.getAggregationControllerCommonClient().listProviders());
            } else {
                citiesByMarket = CitiesByMarket.build(serviceContext.getRepository(ProviderRepository.class).findAll());
            }

            final LabelIndexCache labelIndexCache = LabelIndexCache
                    .build(serviceContext.getConfiguration().getCluster());

            // Process the transactions for each credentials.

            for (String credentialsId : transactionsByCredentialsId.keySet()) {

                List<Transaction> transactions = transactionsByCredentialsId.get(credentialsId);

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
                        new ReprocessingChainFactory(
                                citiesByMarket, new ClusterCategories(categoriesById.values()),
                                serviceContext.getCategoryConfiguration(), firehoseQueueProducer,
                                metricRegistry,
                                serviceContext.getRepository(MerchantRepository.class),
                                serviceContext.getRepository(GiroRepository.class),
                                labelIndexCache,
                                accountRepository, credentialsRepository, loanDataRepository,
                                serviceContext.getConfiguration().getReprocessTransactions(),
                                transactionDao,
                                TransferDetectionScorerFactory
                                        .byCluster(serviceContext.getConfiguration().getCluster()),
                                elasticSearchClient,
                                serviceContext.getRepository(PostalCodeAreaRepository.class),
                                serviceContext.getRepository(CategoryRepository.class),
                                descriptionFormatterFactory, descriptionExtractorFactory,
                                serviceContext.getDao(CategoryChangeRecordDao.class),
                                serviceContext.getConfiguration().getCategorization())
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

    private boolean hasValidCategory(Transaction transaction) {

        Category category = categoriesById.get(transaction.getCategoryId());

        // No category found.
        if (category == null) {
            return false;
        }

        // The category type of the transaction doesn't match the type of the actual category.
        if (!Objects.equal(transaction.getCategoryType(), category.getType())) {
            return false;
        }

        // The type of the actual category is not valid for the transaction.
        if (!transaction.isValidCategoryType(category.getType())) {
            return false;
        }

        return true;
    }

    private void resetCategory(Transaction transaction) {

        Category category = getDefaultCategory(transaction);

        transaction.setCategory(category);
        transaction.setUserModifiedCategory(false);
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
        userData.setTransactions(transactionDao.findAllByUserId(user.getId()));
        userData.setAccounts(accountRepository.findByUserId(user.getId()));
        userData.setCredentials(credentialsRepository.findAllByUserId(user.getId()));

        return userData;
    }
}
