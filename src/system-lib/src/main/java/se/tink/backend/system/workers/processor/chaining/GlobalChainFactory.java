package se.tink.backend.system.workers.processor.chaining;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import org.elasticsearch.client.Client;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.categorization.interfaces.CategorizerFactory;
import se.tink.backend.categorization.interfaces.Classifier;
import se.tink.backend.categorization.learning.UserLearningCommand;
import se.tink.backend.categorization.lookup.CitiesByMarket;
import se.tink.backend.categorization.lookup.LookupGiroCommand;
import se.tink.backend.categorization.rules.LabelIndexCache;
import se.tink.backend.categorization.rules.NaiveBayesCategorizationCommand;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.concurrency.ListenableExecutor;
import se.tink.backend.common.config.CategorizationConfiguration;
import se.tink.backend.common.dao.CategoryChangeRecordDao;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.product.targeting.TargetProductsRunnableFactory;
import se.tink.backend.common.repository.cassandra.CategoryChangeRecordRepository;
import se.tink.backend.common.repository.cassandra.DAO.LoanDAO;
import se.tink.backend.common.repository.cassandra.ExternallyDeletedTransactionRepository;
import se.tink.backend.common.repository.cassandra.TransactionExternalIdRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.GiroRepository;
import se.tink.backend.common.repository.mysql.main.MerchantRepository;
import se.tink.backend.common.repository.mysql.main.PostalCodeAreaRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.common.search.SimilarTransactionsSearcher;
import se.tink.backend.core.ClusterCategories;
import se.tink.backend.core.Provider;
import se.tink.backend.firehose.v1.queue.FirehoseQueueProducer;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.system.workers.processor.TransactionProcessorCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;
import se.tink.backend.system.workers.processor.categorization.CategorizerCommand;
import se.tink.backend.system.workers.processor.deduplication.GlobalDeduplicationCommand;
import se.tink.backend.system.workers.processor.formatting.FormatDescriptionCommand;
import se.tink.backend.system.workers.processor.formatting.MarketDescriptionExtractorFactory;
import se.tink.backend.system.workers.processor.formatting.MarketDescriptionFormatterFactory;
import se.tink.backend.system.workers.processor.loan.CalculateLoanInterestRateCommand;
import se.tink.backend.system.workers.processor.other.EnrichTransferObjectOnPayloadCommand;
import se.tink.backend.system.workers.processor.other.FilterOrphansCommand;
import se.tink.backend.system.workers.processor.other.ProviderDetectionCommand;
import se.tink.backend.system.workers.processor.other.RandomCategoryResetCommand;
import se.tink.backend.system.workers.processor.other.SendTransactionsToFirehoseCommand;
import se.tink.backend.system.workers.processor.other.payment.PaymentDetectionCommand;
import se.tink.backend.system.workers.processor.storage.HandleAlreadyExternallyDeletedTransactionCommand;
import se.tink.backend.system.workers.processor.storage.SaveTransactionCommand;
import se.tink.backend.system.workers.processor.storage.UpdateTransactionsOnContextCommand;
import se.tink.backend.system.workers.processor.system.UpdateCredentialsStatusAndCertainDateCommand;
import se.tink.backend.system.workers.processor.transfers.TransferDetectionCommand;
import se.tink.backend.system.workers.processor.transfers.scoring.TransferDetectionScorerFactory;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.metrics.MetricRegistry;

// TODO: Move this to a Guice module.
public class GlobalChainFactory implements ChainFactory {
    private final CategorizerFactory categorizerFactory;
    private CitiesByMarket citiesByMarket;
    private ClusterCategories categories;
    private CategoryConfiguration categoryConfiguration;
    private TargetProductsRunnableFactory targetProductsRunnableFactory;
    private FirehoseQueueProducer firehoseQueueProducer;
    private MetricRegistry metricRegistry;
    private MerchantRepository merchantRepository;
    private GiroRepository giroRepository;
    private LabelIndexCache labelIndexCache;
    private LoanDAO loanDAO;
    private ListenableExecutor asyncExecutor;
    private TransactionDao transactionDao;
    private TransferDetectionScorerFactory transferDetectionScorerFactory;
    private SystemServiceFactory systemServiceFactory;
    private AccountRepository accountRepository;
    private SimilarTransactionsSearcher similarTransactionsSearcher;
    private com.google.inject.Provider<MarketDescriptionFormatterFactory> descriptionFormatterFactory;
    private com.google.inject.Provider<MarketDescriptionExtractorFactory> descriptionExtractorFactory;
    private CategoryChangeRecordDao categoryChangeRecordDao;
    private ExternallyDeletedTransactionRepository externallyDeletedTransactionRepository;
    private TransactionExternalIdRepository transactionExternalIdRepository;
    private Cluster cluster;
    private CategorizationConfiguration categorizationConfiguration;

    /**
     * Package protected; You probably want to use {@link DefaultUserChainFactoryCreator}.
     */
    @Inject
    public GlobalChainFactory(
            Cluster cluster,
            CitiesByMarket citiesByMarket,
            ClusterCategories categories,
            CategoryConfiguration categoryConfiguration,
            TargetProductsRunnableFactory targetProductsRunnableFactory,
            FirehoseQueueProducer firehoseQueueProducer,
            MetricRegistry metricRegistry,
            MerchantRepository merchantRepository,
            GiroRepository giroRepository,
            LabelIndexCache labelIndexCache,
            LoanDAO loanDAO,
            ListenableExecutor asyncExecutor,
            TransactionDao transactionDao,
            TransferDetectionScorerFactory transferDetectionScorerFactory,
            SystemServiceFactory systemServiceFactory,
            AccountRepository accountRepository,
            SimilarTransactionsSearcher similarTransactionsSearcher,
            com.google.inject.Provider<MarketDescriptionFormatterFactory> descriptionFormatterFactory,
            com.google.inject.Provider<MarketDescriptionExtractorFactory> descriptionExtractorFactory,
            CategorizerFactory categorizerFactory,
            CategoryChangeRecordDao categoryChangeRecordDao,
            ExternallyDeletedTransactionRepository externallyDeletedTransactionRepository,
            TransactionExternalIdRepository transactionExternalIdRepository,
            CategorizationConfiguration categorizationConfiguration) {
        this.cluster = cluster;
        this.citiesByMarket = citiesByMarket;
        this.categories = categories;
        this.categoryConfiguration = categoryConfiguration;
        this.targetProductsRunnableFactory = targetProductsRunnableFactory;
        this.firehoseQueueProducer = firehoseQueueProducer;
        this.metricRegistry = metricRegistry;
        this.merchantRepository = merchantRepository;
        this.giroRepository = giroRepository;
        this.labelIndexCache = labelIndexCache;
        this.loanDAO = loanDAO;
        this.asyncExecutor = asyncExecutor;
        this.transactionDao = transactionDao;
        this.transferDetectionScorerFactory = transferDetectionScorerFactory;
        this.systemServiceFactory = systemServiceFactory;
        this.accountRepository = accountRepository;
        this.similarTransactionsSearcher = similarTransactionsSearcher;
        this.descriptionFormatterFactory = descriptionFormatterFactory;
        this.descriptionExtractorFactory = descriptionExtractorFactory;
        this.categorizerFactory = categorizerFactory;
        this.categoryChangeRecordDao = categoryChangeRecordDao;
        this.externallyDeletedTransactionRepository = externallyDeletedTransactionRepository;
        this.transactionExternalIdRepository = transactionExternalIdRepository;
        this.categorizationConfiguration = categorizationConfiguration;
    }

    @Override
    public ImmutableList<TransactionProcessorCommand> build(TransactionProcessorContext context) {
        Provider provider = context.getProvider();

        ImmutableList.Builder<TransactionProcessorCommand> builder = ImmutableList.builder();

        builder.add(new HandleAlreadyExternallyDeletedTransactionCommand(externallyDeletedTransactionRepository,
                        transactionExternalIdRepository),
                new FilterOrphansCommand(context, context.getUser().getId()));

        builder.add(
                new GlobalDeduplicationCommand(metricRegistry, context),
                new EnrichTransferObjectOnPayloadCommand(),
                new FormatDescriptionCommand(
                        context,
                        descriptionFormatterFactory.get(),
                        descriptionExtractorFactory.get(),
                        metricRegistry,
                        provider
                ),
                new PaymentDetectionCommand()
        );

        LookupGiroCommand.build(provider, merchantRepository, giroRepository, metricRegistry, categoryChangeRecordDao,
                categorizationConfiguration).ifPresent(builder::add);

        builder.add(
                new TransferDetectionCommand(context, categoryConfiguration, transferDetectionScorerFactory,
                        categories, categoryChangeRecordDao),
                new CategorizerCommand(
                        categorizerFactory.build(
                                context.getUser(),
                                provider,
                                context.getUserData().getInStoreTransactions().values(),
                                labelIndexCache, citiesByMarket,
                                categorizationConfiguration), categoryChangeRecordDao
                ),
                new RandomCategoryResetCommand(categories, categoryConfiguration, metricRegistry,
                        categoryChangeRecordDao, context.getUser(), categorizationConfiguration)
        );

        ProviderDetectionCommand.build(provider).ifPresent(builder::add);

        builder.add(
                new UpdateTransactionsOnContextCommand(context),
                new UpdateCredentialsStatusAndCertainDateCommand(context, systemServiceFactory, accountRepository)
        );

        if (Objects.equals(cluster, Cluster.TINK)) {
            CalculateLoanInterestRateCommand
                    .build(provider, context, loanDAO, targetProductsRunnableFactory, asyncExecutor)
                    .ifPresent(builder::add);
        }

        builder.add(
                new SaveTransactionCommand(context, transactionDao, metricRegistry),
                new SendTransactionsToFirehoseCommand(context, firehoseQueueProducer),
                new UpdateTransactionsOnContextCommand(context)
        );

        return builder.build();
    }

    private Collection<Classifier> buildClassifiers(TransactionProcessorContext context, Provider provider) {
        ImmutableList.Builder<Classifier> builder = ImmutableList.builder();

        NaiveBayesCategorizationCommand.buildAllTypes(labelIndexCache, citiesByMarket, provider).forEach(builder::add);

        builder.add(new UserLearningCommand(
                context.getUser().getId(), similarTransactionsSearcher, categories,
                context.getUserData().getInStoreTransactions().values()
        ));

        return builder.build();
    }

    /**
     * Generally use {@link DefaultUserChainFactoryCreator} instead.
     * <p>
     * TODO: Remove this. We don't want to depend on the ServiceFactory.
     */
    @Deprecated
    @VisibleForTesting
    public static GlobalChainFactory fromServiceContext(
            ServiceContext context, FirehoseQueueProducer firehoseQueueProducer,
            com.google.inject.Provider<MarketDescriptionFormatterFactory> descriptionFormatterFactory,
            com.google.inject.Provider<MarketDescriptionExtractorFactory> descriptionExtractorFactory,
            MetricRegistry metricRegistry, CategorizerFactory categorizerFactory) {

        Cluster cluster = context.getConfiguration().getCluster();

        CitiesByMarket citiesByMarket;
        if (context.isProvidersOnAggregation()) {
            citiesByMarket = CitiesByMarket.build(context.getAggregationControllerCommonClient().listProviders());
        } else {
            citiesByMarket = CitiesByMarket.build(context.getRepository(ProviderRepository.class).findAll());
        }

        ClusterCategories categories = new ClusterCategories(context.getRepository(CategoryRepository.class).findAll());
        CategoryConfiguration categoryConfiguration = context.getCategoryConfiguration();
        TargetProductsRunnableFactory targetProductsRunnableFactory = new TargetProductsRunnableFactory(context);
        MerchantRepository merchantRepository = context.getRepository(MerchantRepository.class);
        GiroRepository giroRepository = context.getRepository(GiroRepository.class);
        LabelIndexCache labelIndexCache = LabelIndexCache.build(cluster);
        LoanDAO loanDAO = context.getDao(LoanDAO.class);
        TransactionDao transactionDao = context.getDao(TransactionDao.class);
        TransferDetectionScorerFactory transferDetectionScorerFactory = TransferDetectionScorerFactory
                .byCluster(cluster);
        SystemServiceFactory systemServiceFactory = context.getSystemServiceFactory();
        AccountRepository accountRepository = context.getRepository(AccountRepository.class);
        Client searchClient = context.getSearchClient();
        PostalCodeAreaRepository postalCodeAreaRepository = context.getRepository(PostalCodeAreaRepository.class);
        CategoryRepository categoryRepository = context.getRepository(CategoryRepository.class);
        SimilarTransactionsSearcher similarTransactionsSearcher = new SimilarTransactionsSearcher(searchClient,
                accountRepository, postalCodeAreaRepository, categoryRepository);
        CategoryChangeRecordDao categoryChangeRecordDao = new CategoryChangeRecordDao(
                context.getRepository(CategoryChangeRecordRepository.class), metricRegistry);
        ExternallyDeletedTransactionRepository externallyDeletedTransactionRepository = context
                .getRepository(ExternallyDeletedTransactionRepository.class);
        TransactionExternalIdRepository transactionExternalIdRepository = context
                .getRepository(TransactionExternalIdRepository.class);
        CategorizationConfiguration categorizationConfiguration = context.getConfiguration().getCategorization();

        return new GlobalChainFactory(
                cluster,
                citiesByMarket, categories,
                categoryConfiguration,
                targetProductsRunnableFactory, firehoseQueueProducer, metricRegistry, merchantRepository,
                giroRepository, labelIndexCache,
                loanDAO, context.getExecutorService(),
                transactionDao,
                transferDetectionScorerFactory,
                systemServiceFactory,
                accountRepository,
                similarTransactionsSearcher,
                descriptionFormatterFactory,
                descriptionExtractorFactory, categorizerFactory,
                categoryChangeRecordDao,
                externallyDeletedTransactionRepository,
                transactionExternalIdRepository,
                categorizationConfiguration);
    }

    @Override
    public String getUniqueIdentifier() {
        return "oxford";
    }
    
    @Override
    public void close() throws IOException {
        // TODO lifecycling should be handled by Guice
        categorizerFactory.close();
    }
}
