package se.tink.backend.system.guice.providers;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.google.inject.util.Providers;
import javax.annotation.Nullable;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.categorization.interfaces.CategorizerFactory;
import se.tink.backend.categorization.lookup.CitiesByMarket;
import se.tink.backend.categorization.rules.LabelIndexCache;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.config.CategorizationConfiguration;
import se.tink.backend.common.dao.CategoryChangeRecordDao;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.product.targeting.TargetProductsRunnableFactory;
import se.tink.backend.common.repository.cassandra.DAO.LoanDAO;
import se.tink.backend.common.repository.cassandra.ExternallyDeletedTransactionRepository;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.cassandra.TransactionCheckpointRepository;
import se.tink.backend.common.repository.cassandra.TransactionExternalIdRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.GiroRepository;
import se.tink.backend.common.repository.mysql.main.MerchantRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.common.search.SimilarTransactionsSearcher;
import se.tink.backend.core.ClusterCategories;
import se.tink.backend.core.User;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.firehose.v1.queue.FirehoseQueueProducer;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.system.workers.processor.chaining.ChainFactory;
import se.tink.backend.system.workers.processor.chaining.CornwallChainFactory;
import se.tink.backend.system.workers.processor.chaining.GlobalChainFactory;
import se.tink.backend.system.workers.processor.chaining.LeedsChainFactory;
import se.tink.backend.system.workers.processor.formatting.MarketDescriptionExtractorFactory;
import se.tink.backend.system.workers.processor.formatting.MarketDescriptionFormatterFactory;
import se.tink.backend.system.workers.processor.transfers.scoring.TransferDetectionScorerFactory;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.metrics.MetricRegistry;

public class ChainFactoryProvider implements Provider<ChainFactory> {
    private final Cluster cluster;
    private final CategorizationConfiguration categorizationConfiguration;
    private final ClusterCategories categories;
    private final CategoryConfiguration categoryConfiguration;
    private final MetricRegistry metricRegistry;
    private final AccountRepository accountRepository;
    private final CredentialsRepository credentialsRepository;
    private final LoanDataRepository loanDataRepository;
    private final TransactionDao transactionDao;
    private final SystemServiceFactory systemServiceFactory;
    private final CategoryChangeRecordDao categoryChangeRecordDao;
    private final LabelIndexCache labelIndexCache;
    private final FirehoseQueueProducer firehoseQueueProducer;
    private final ExternallyDeletedTransactionRepository externallyDeletedTransactionRepository;
    private final TransactionExternalIdRepository transactionExternalIdRepository;
    private final TransactionCheckpointRepository transactionCheckpointRepository;
    private final TargetProductsRunnableFactory targetProductsRunnableFactory;
    private final MerchantRepository merchantRepository;
    private final GiroRepository giroRepository;
    private final LoanDAO loanDAO;
    private final SimilarTransactionsSearcher similarTransactionsSearcher;
    private final ProviderRepository providerRepository;
    private final AggregationControllerCommonClient aggregationControllerCommonClient;
    private ListenableThreadPoolExecutor<Runnable> executorService;
    private final boolean isProvidersOnAggregation;

    private final CategorizerFactory legacyCategorizerFactory;
    private final CategorizerFactory fastTextCategorizerFactory;

    @Inject
    public ChainFactoryProvider(Cluster cluster,
            CategorizationConfiguration categorizationConfiguration,
            ClusterCategories categories,
            CategoryConfiguration categoryConfiguration,
            MetricRegistry metricRegistry,
            AccountRepository accountRepository,
            CredentialsRepository credentialsRepository,
            LoanDataRepository loanDataRepository,
            TransactionDao transactionDao,
            SystemServiceFactory systemServiceFactory,
            CategoryChangeRecordDao categoryChangeRecordDao,
            FirehoseQueueProducer firehoseQueueProducer,
            ExternallyDeletedTransactionRepository externallyDeletedTransactionRepository,
            TransactionExternalIdRepository transactionExternalIdRepository,
            TransactionCheckpointRepository transactionCheckpointRepository,
            TargetProductsRunnableFactory targetProductsRunnableFactory,
            MerchantRepository merchantRepository,
            GiroRepository giroRepository,
            LoanDAO loanDAO,
            SimilarTransactionsSearcher similarTransactionsSearcher,
            ProviderRepository providerRepository,
            AggregationControllerCommonClient aggregationControllerCommonClient,
            @Named("executor") ListenableThreadPoolExecutor<Runnable> executorService,
            @Named("isProvidersOnAggregation") boolean isProvidersOnAggregation,
            @Named("legacyCategorizerFactory") CategorizerFactory legacyCategorizerFactory,
            @Named("fastTextCategorizerFactory") @Nullable CategorizerFactory fastTextCategorizerFactory
    ) {
        this.cluster = cluster;
        this.categories = categories;
        this.categoryConfiguration = categoryConfiguration;
        this.metricRegistry = metricRegistry;
        this.accountRepository = accountRepository;
        this.credentialsRepository = credentialsRepository;
        this.loanDataRepository = loanDataRepository;
        this.transactionDao = transactionDao;
        this.systemServiceFactory = systemServiceFactory;
        this.categoryChangeRecordDao = categoryChangeRecordDao;
        this.labelIndexCache = LabelIndexCache.build(cluster);
        this.firehoseQueueProducer = firehoseQueueProducer;
        this.externallyDeletedTransactionRepository = externallyDeletedTransactionRepository;
        this.transactionExternalIdRepository = transactionExternalIdRepository;
        this.transactionCheckpointRepository = transactionCheckpointRepository;
        this.targetProductsRunnableFactory = targetProductsRunnableFactory;
        this.merchantRepository = merchantRepository;
        this.giroRepository = giroRepository;
        this.loanDAO = loanDAO;
        this.similarTransactionsSearcher = similarTransactionsSearcher;
        this.providerRepository = providerRepository;
        this.aggregationControllerCommonClient = aggregationControllerCommonClient;
        this.executorService = executorService;
        this.isProvidersOnAggregation = isProvidersOnAggregation;

        this.categorizationConfiguration = categorizationConfiguration;
        this.legacyCategorizerFactory = legacyCategorizerFactory;
        this.fastTextCategorizerFactory = fastTextCategorizerFactory;
    }

    @Override
    public ChainFactory get() {
        CategorizerFactory globalCategorizer = (user, provider, inStoreTransactions, labelIndexCacheLocal, citiesByMarketLocal, config) -> {
            boolean useBeta = shouldUseBeta(categorizationConfiguration, user);
            return useBeta
                    ?
                    fastTextCategorizerFactory
                            .build(user, provider, inStoreTransactions, labelIndexCacheLocal,
                                    citiesByMarketLocal, categorizationConfiguration)
                    :
                    legacyCategorizerFactory.build(user, provider, inStoreTransactions, labelIndexCacheLocal,
                            citiesByMarketLocal, categorizationConfiguration);
        };

        CitiesByMarket citiesByMarket;
        if (isProvidersOnAggregation) {
            citiesByMarket = CitiesByMarket.build(aggregationControllerCommonClient.listProviders());
        } else {
            citiesByMarket = CitiesByMarket.build(providerRepository.findAll());
        }

        switch (cluster) {
        case ABNAMRO:
            return new LeedsChainFactory(
                    categories,
                    categoryConfiguration,
                    metricRegistry,
                    accountRepository,
                    credentialsRepository,
                    loanDataRepository,
                    transactionDao,
                    TransferDetectionScorerFactory.byCluster(cluster),
                    systemServiceFactory,
                    Providers.of(MarketDescriptionFormatterFactory.byCluster(cluster)),
                    Providers.of(MarketDescriptionExtractorFactory.byCluster(cluster)),
                    globalCategorizer,
                    categoryChangeRecordDao,
                    labelIndexCache,
                    citiesByMarket,
                    firehoseQueueProducer,
                    categorizationConfiguration
            );
        case CORNWALL:
            return new CornwallChainFactory(
                    categories,
                    categoryConfiguration,
                    metricRegistry,
                    externallyDeletedTransactionRepository,
                    transactionExternalIdRepository,
                    accountRepository,
                    credentialsRepository,
                    loanDataRepository,
                    transactionCheckpointRepository,
                    transactionDao,
                    TransferDetectionScorerFactory.byCluster(cluster),
                    Providers.of(MarketDescriptionFormatterFactory.byCluster(cluster)),
                    Providers.of(MarketDescriptionExtractorFactory.byCluster(cluster)),
                    globalCategorizer,
                    categoryChangeRecordDao,
                    labelIndexCache,
                    citiesByMarket,
                    categorizationConfiguration
            );
        default:
            return new GlobalChainFactory(
                    cluster,
                    citiesByMarket,
                    categories,
                    categoryConfiguration,
                    targetProductsRunnableFactory,
                    firehoseQueueProducer,
                    metricRegistry,
                    merchantRepository,
                    giroRepository,
                    labelIndexCache,
                    loanDAO,
                    executorService,
                    transactionDao,
                    TransferDetectionScorerFactory.byCluster(cluster),
                    systemServiceFactory,
                    accountRepository,
                    similarTransactionsSearcher,
                    Providers.of(MarketDescriptionFormatterFactory.byCluster(cluster)),
                    Providers.of(MarketDescriptionExtractorFactory.byCluster(cluster)),
                    globalCategorizer,
                    categoryChangeRecordDao,
                    externallyDeletedTransactionRepository,
                    transactionExternalIdRepository,
                    categorizationConfiguration);

        }
    }

    private static boolean shouldUseBeta(CategorizationConfiguration categorizationConfiguration, User user) {
        boolean userIdStartsWithSomeBetaPrefix = categorizationConfiguration.getBetaUserIdPrefixes()
                .stream().anyMatch(p -> user.getId().startsWith(p));
        boolean hasFeatureFlag = user.getFlags().contains(FeatureFlags.FASTTEXT_CATEGORIZER);

        return categorizationConfiguration.isBetaForAllUsers() || (!categorizationConfiguration.getBetaUserIdPrefixes().isEmpty()
                && userIdStartsWithSomeBetaPrefix) || hasFeatureFlag;
    }
}
