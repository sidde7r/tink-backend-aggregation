package se.tink.backend.system.workers.processor.chaining;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.io.IOException;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.categorization.interfaces.CategorizerFactory;
import se.tink.backend.categorization.lookup.CitiesByMarket;
import se.tink.backend.categorization.rules.LabelIndexCache;
import se.tink.backend.common.config.CategorizationConfiguration;
import se.tink.backend.common.dao.CategoryChangeRecordDao;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.PostalCodeAreaRepository;
import se.tink.backend.core.ClusterCategories;
import se.tink.backend.core.Provider;
import se.tink.backend.firehose.v1.queue.FirehoseQueueProducer;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.system.workers.processor.TransactionProcessorCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;
import se.tink.backend.system.workers.processor.categorization.CategorizerCommand;
import se.tink.backend.system.workers.processor.deduplication.AbnAmroDuplicateTransactionCommand;
import se.tink.backend.system.workers.processor.deduplication.DeduplicationCommand;
import se.tink.backend.system.workers.processor.deduplication.PendingTransactionCommand;
import se.tink.backend.system.workers.processor.formatting.AbnAmroTikkieDescriptionFormatter;
import se.tink.backend.system.workers.processor.formatting.FormatDescriptionCommand;
import se.tink.backend.system.workers.processor.formatting.MarketDescriptionExtractorFactory;
import se.tink.backend.system.workers.processor.formatting.MarketDescriptionFormatterFactory;
import se.tink.backend.system.workers.processor.other.FilterOrphansCommand;
import se.tink.backend.system.workers.processor.other.RandomCategoryResetCommand;
import se.tink.backend.system.workers.processor.other.SendTransactionsToFirehoseCommand;
import se.tink.backend.system.workers.processor.other.payment.PaymentDetectionCommand;
import se.tink.backend.system.workers.processor.storage.LoadUserDataCommand;
import se.tink.backend.system.workers.processor.storage.PrepareTransactionsToSaveAndDeleteCommand;
import se.tink.backend.system.workers.processor.storage.RemoveRedundantTransactionsCommand;
import se.tink.backend.system.workers.processor.storage.SaveTransactionCommand;
import se.tink.backend.system.workers.processor.storage.UpdateTransactionsOnContextCommand;
import se.tink.backend.system.workers.processor.system.UpdateCredentialsStatusAndCertainDateCommand;
import se.tink.backend.system.workers.processor.transfers.TransferDetectionCommand;
import se.tink.backend.system.workers.processor.transfers.scoring.TransferDetectionScorerFactory;
import se.tink.libraries.metrics.MetricRegistry;

// TODO: Migrate this to a Guice module.
public class LeedsChainFactory implements ChainFactory {
    private ClusterCategories categories;
    private CategoryConfiguration categoryConfiguration;
    private MetricRegistry metricRegistry;
    private AccountRepository accountRepository;
    private CredentialsRepository credentialsRepository;
    private LoanDataRepository loanDataRepository;
    private TransactionDao indexedTransactionDAO;
    private TransferDetectionScorerFactory transferDetectionScorerFactory;
    private SystemServiceFactory systemServiceFactory;
    private PostalCodeAreaRepository postalCodeAreaRepository;
    private com.google.inject.Provider<MarketDescriptionFormatterFactory> descriptionFormatterFactory;
    private com.google.inject.Provider<MarketDescriptionExtractorFactory> descriptionExtractorFactory;
    private CategorizerFactory categorizer;
    private CategoryChangeRecordDao categoryChangeRecordDao;
    private LabelIndexCache labelIndexCache;
    private CitiesByMarket citiesByMarket;
    private CategorizationConfiguration categorizationConfiguration;
    private FirehoseQueueProducer firehoseQueueProducer;

    /**
     * Package protected; You probably want to use {@link DefaultUserChainFactoryCreator}.
     */
    @Inject
    public LeedsChainFactory(
            ClusterCategories categories,
            CategoryConfiguration categoryConfiguration,
            MetricRegistry metricRegistry,
            AccountRepository accountRepository,
            CredentialsRepository credentialsRepository,
            LoanDataRepository loanDataRepository,
            TransactionDao indexedTransactionDAO,
            TransferDetectionScorerFactory transferDetectionScorerFactory,
            SystemServiceFactory systemServiceFactory,
            com.google.inject.Provider<MarketDescriptionFormatterFactory> descriptionFormatterFactory,
            com.google.inject.Provider<MarketDescriptionExtractorFactory> descriptionExtractorFactory,
            CategorizerFactory categorizer,
            CategoryChangeRecordDao categoryChangeRecordDao,
            LabelIndexCache labelIndexCache,
            CitiesByMarket citiesByMarket,
            FirehoseQueueProducer firehoseQueueProducer,
            CategorizationConfiguration categorizationConfiguration) {

        this.categories = categories;
        this.categoryConfiguration = categoryConfiguration;
        this.metricRegistry = metricRegistry;
        this.accountRepository = accountRepository;
        this.credentialsRepository = credentialsRepository;
        this.loanDataRepository = loanDataRepository;
        this.indexedTransactionDAO = indexedTransactionDAO;
        this.transferDetectionScorerFactory = transferDetectionScorerFactory;
        this.systemServiceFactory = systemServiceFactory;
        this.descriptionFormatterFactory = descriptionFormatterFactory;
        this.descriptionExtractorFactory = descriptionExtractorFactory;
        this.categorizer = categorizer;
        this.categoryChangeRecordDao = categoryChangeRecordDao;
        this.labelIndexCache = labelIndexCache;
        this.citiesByMarket = citiesByMarket;
        this.categorizationConfiguration = categorizationConfiguration;
        this.firehoseQueueProducer = firehoseQueueProducer;
    }

    @Override
    public ImmutableList<TransactionProcessorCommand> build(TransactionProcessorContext context) {
        Provider provider = context.getProvider();

        ImmutableList.Builder<TransactionProcessorCommand> builder = ImmutableList.builder();
        builder.add(
                new AbnAmroDuplicateTransactionCommand(),
                new LoadUserDataCommand(
                        context, credentialsRepository, loanDataRepository, indexedTransactionDAO, accountRepository),
                new FilterOrphansCommand(context, context.getUser().getId()),
                new DeduplicationCommand(context),
                new PendingTransactionCommand(context)
        );

        AbnAmroTikkieDescriptionFormatter.build(provider).ifPresent(builder::add);

        builder.add(
                new FormatDescriptionCommand(
                        context,
                        descriptionFormatterFactory.get(),
                        descriptionExtractorFactory.get(),
                        metricRegistry,
                        provider
                ),
                new PaymentDetectionCommand()
        );

        builder.add(
                new TransferDetectionCommand(context, categoryConfiguration, transferDetectionScorerFactory,
                        categories, categoryChangeRecordDao),
                new CategorizerCommand(categorizer.build(
                        context.getUser(),
                        provider,
                        context.getUserData().getInStoreTransactions().values(),
                        labelIndexCache,
                        citiesByMarket,
                        categorizationConfiguration), categoryChangeRecordDao),
                new RandomCategoryResetCommand(categories, categoryConfiguration, metricRegistry,
                        categoryChangeRecordDao, context.getUser(), categorizationConfiguration),
                new PrepareTransactionsToSaveAndDeleteCommand(context, metricRegistry),
                new SaveTransactionCommand(context, indexedTransactionDAO, metricRegistry),
                new RemoveRedundantTransactionsCommand(context, metricRegistry, indexedTransactionDAO, provider),
                new UpdateTransactionsOnContextCommand(context),
                new SendTransactionsToFirehoseCommand(context, firehoseQueueProducer),
                new UpdateCredentialsStatusAndCertainDateCommand(context, systemServiceFactory, accountRepository)
        );

        return builder.build();
    }

    @Override
    public String getUniqueIdentifier() {
        return "leeds";
    }

    @Override
    public void close() throws IOException {
        // TODO lifecycling should be handled by Guice
        categorizer.close();
    }
}
