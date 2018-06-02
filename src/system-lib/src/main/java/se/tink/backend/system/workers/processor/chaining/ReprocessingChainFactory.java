package se.tink.backend.system.workers.processor.chaining;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import se.tink.backend.categorization.ProbabilityCategorizer;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.categorization.interfaces.Classifier;
import se.tink.backend.categorization.learning.UserLearningCommand;
import se.tink.backend.categorization.lookup.CitiesByMarket;
import se.tink.backend.categorization.lookup.LookupGiroCommand;
import se.tink.backend.categorization.rules.AbnAmroCategorizationCommand;
import se.tink.backend.categorization.rules.AbnAmroIcsCategorizationCommand;
import se.tink.backend.categorization.rules.LabelIndexCache;
import se.tink.backend.categorization.rules.NaiveBayesCategorizationCommand;
import se.tink.backend.common.config.CategorizationConfiguration;
import se.tink.backend.common.config.ReprocessTransactionsConfiguration;
import se.tink.backend.common.dao.CategoryChangeRecordDao;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.GiroRepository;
import se.tink.backend.common.repository.mysql.main.MerchantRepository;
import se.tink.backend.common.repository.mysql.main.PostalCodeAreaRepository;
import se.tink.backend.common.search.client.ElasticSearchClient;
import se.tink.backend.core.ClusterCategories;
import se.tink.backend.core.Provider;
import se.tink.backend.firehose.v1.queue.FirehoseQueueProducer;
import se.tink.backend.system.workers.processor.TransactionProcessorCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;
import se.tink.backend.system.workers.processor.categorization.CategorizerCommand;
import se.tink.backend.system.workers.processor.formatting.AbnAmroTikkieDescriptionFormatter;
import se.tink.backend.system.workers.processor.formatting.FormatDescriptionCommand;
import se.tink.backend.system.workers.processor.formatting.MarketDescriptionExtractorFactory;
import se.tink.backend.system.workers.processor.formatting.MarketDescriptionFormatterFactory;
import se.tink.backend.system.workers.processor.other.FilterOrphansCommand;
import se.tink.backend.system.workers.processor.other.ResetExistingCommand;
import se.tink.backend.system.workers.processor.other.SendTransactionsToFirehoseCommand;
import se.tink.backend.system.workers.processor.other.payment.PaymentDetectionCommand;
import se.tink.backend.system.workers.processor.storage.LoadUserDataCommand;
import se.tink.backend.system.workers.processor.storage.PrepareTransactionsToSaveAndDeleteCommand;
import se.tink.backend.system.workers.processor.storage.SaveTransactionCommand;
import se.tink.backend.system.workers.processor.storage.UpdateTransactionsOnContextCommand;
import se.tink.backend.system.workers.processor.transfers.TransferDetectionCommand;
import se.tink.backend.system.workers.processor.transfers.scoring.TransferDetectionScorerFactory;
import se.tink.libraries.metrics.MetricRegistry;

// TODO: Move this to the same package where the reprocessing command lies.
public class ReprocessingChainFactory implements ChainFactory {
    private CitiesByMarket citiesByMarket;
    private ClusterCategories categories;
    private CategoryConfiguration categoryConfiguration;
    private FirehoseQueueProducer firehoseQueueProducer;
    private MetricRegistry metricRegistry;
    private MerchantRepository merchantRepository;
    private GiroRepository giroRepository;
    private LabelIndexCache labelIndexCache;
    private AccountRepository accountRepository;
    private CredentialsRepository credentialsRepository;
    private LoanDataRepository loanDataRepository;
    private ReprocessTransactionsConfiguration reprocessTransactionsConfiguration;
    private TransactionDao transactionDao;
    private TransferDetectionScorerFactory transferDetectionScorerFactory;
    private ElasticSearchClient elasticSearchClient;
    private PostalCodeAreaRepository postalCodeAreaRepository;
    private CategoryRepository categoryRepository;
    private MarketDescriptionFormatterFactory descriptionFormatterFactory;
    private MarketDescriptionExtractorFactory descriptionExtractorFactory;
    private CategoryChangeRecordDao categoryChangeRecordDao;
    private CategorizationConfiguration categorizationConfiguration;

    public ReprocessingChainFactory(
            CitiesByMarket citiesByMarket,
            ClusterCategories categories,
            CategoryConfiguration categoryConfiguration,
            FirehoseQueueProducer firehoseQueueProducer,
            MetricRegistry metricRegistry,
            MerchantRepository merchantRepository,
            GiroRepository giroRepository,
            LabelIndexCache labelIndexCache,
            AccountRepository accountRepository,
            CredentialsRepository credentialsRepository,
            LoanDataRepository loanDataRepository,
            ReprocessTransactionsConfiguration reprocessTransactionsConfiguration,
            TransactionDao transactionDao,
            TransferDetectionScorerFactory transferDetectionScorerFactory,
            ElasticSearchClient elasticSearchClient,
            PostalCodeAreaRepository postalCodeAreaRepository,
            CategoryRepository categoryRepository,
            MarketDescriptionFormatterFactory descriptionFormatterFactory,
            MarketDescriptionExtractorFactory descriptionExtractorFactory,
            CategoryChangeRecordDao categoryChangeRecordDao,
            CategorizationConfiguration categorizationConfiguration) {

        this.citiesByMarket = citiesByMarket;
        this.categories = categories;
        this.categoryConfiguration = categoryConfiguration;
        this.firehoseQueueProducer = firehoseQueueProducer;
        this.metricRegistry = metricRegistry;
        this.merchantRepository = merchantRepository;
        this.giroRepository = giroRepository;
        this.labelIndexCache = labelIndexCache;
        this.accountRepository = accountRepository;
        this.credentialsRepository = credentialsRepository;
        this.loanDataRepository = loanDataRepository;
        this.reprocessTransactionsConfiguration = reprocessTransactionsConfiguration;
        this.transactionDao = transactionDao;
        this.transferDetectionScorerFactory = transferDetectionScorerFactory;
        this.elasticSearchClient = elasticSearchClient;
        this.postalCodeAreaRepository = postalCodeAreaRepository;
        this.categoryRepository = categoryRepository;
        this.descriptionFormatterFactory = descriptionFormatterFactory;
        this.descriptionExtractorFactory = descriptionExtractorFactory;
        this.categoryChangeRecordDao = categoryChangeRecordDao;
        this.categorizationConfiguration = categorizationConfiguration;
    }

    @Override
    public ImmutableList<TransactionProcessorCommand> build(TransactionProcessorContext context) {
        Provider provider = context.getProvider();

        ImmutableList.Builder<TransactionProcessorCommand> builder = ImmutableList.builder();
        builder.add(
                new ResetExistingCommand(context, reprocessTransactionsConfiguration.includeModifiedByUser()),
                new LoadUserDataCommand(
                        context, credentialsRepository, loanDataRepository, transactionDao, accountRepository),
                new FilterOrphansCommand(context, context.getUser().getId())
        );

        AbnAmroTikkieDescriptionFormatter.build(provider).ifPresent(builder::add);

        builder.add(
                new FormatDescriptionCommand(
                        context,
                        descriptionFormatterFactory,
                        descriptionExtractorFactory,
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
                        new ProbabilityCategorizer(
                                context.getUser(),
                                categoryConfiguration,
                                metricRegistry,
                                categories,
                                buildCategorizers(context, provider),
                                "reprocessing-chain"), categoryChangeRecordDao
                ),
                new PrepareTransactionsToSaveAndDeleteCommand(context, metricRegistry),
                new SaveTransactionCommand(context, transactionDao, metricRegistry),
                new SendTransactionsToFirehoseCommand(context, firehoseQueueProducer),
                new UpdateTransactionsOnContextCommand(context)
        );

        return builder.build();
    }

    private Collection<Classifier> buildCategorizers(TransactionProcessorContext context, Provider provider) {
        ImmutableList.Builder<Classifier> builder = ImmutableList.builder();

        NaiveBayesCategorizationCommand.buildAllTypes(labelIndexCache, citiesByMarket, provider).forEach(builder::add);
        AbnAmroCategorizationCommand.build(provider).ifPresent(builder::add);
        AbnAmroIcsCategorizationCommand.build(provider).ifPresent(builder::add);

        builder.add(new UserLearningCommand(
                context.getUser().getId(),
                elasticSearchClient.getSimilarTransactionsSearcher(), categories,
                context.getUserData().getInStoreTransactions().values()
        ));

        return builder.build();
    }

    @Override
    public String getUniqueIdentifier() {
        return "reprocessing";
    }

    @Override
    public void close() {
        // Intentionally left empty
    }
}
