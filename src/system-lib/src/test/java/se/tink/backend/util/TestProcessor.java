package se.tink.backend.util;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.categorization.lookup.LookupGiroCommand;
import se.tink.backend.common.config.CategorizationConfiguration;
import se.tink.backend.common.dao.CategoryChangeRecordDao;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.GiroRepository;
import se.tink.backend.common.repository.mysql.main.MerchantRepository;
import se.tink.backend.core.Category;
import se.tink.backend.core.ClusterCategories;
import se.tink.backend.core.Provider;
import se.tink.backend.core.UserData;
import se.tink.backend.system.workers.processor.TransactionProcessor;
import se.tink.backend.system.workers.processor.TransactionProcessorCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;
import se.tink.backend.system.workers.processor.categorization.UnknownCategorizationCommand;
import se.tink.backend.system.workers.processor.chaining.ChainFactory;
import se.tink.backend.system.workers.processor.chaining.SimpleChainFactory;
import se.tink.backend.system.workers.processor.deduplication.DeduplicationCommand;
import se.tink.backend.system.workers.processor.deduplication.PendingTransactionCommand;
import se.tink.backend.system.workers.processor.formatting.FormatDescriptionCommand;
import se.tink.backend.system.workers.processor.formatting.MarketDescriptionExtractorFactory;
import se.tink.backend.system.workers.processor.formatting.MarketDescriptionFormatterFactory;
import se.tink.backend.system.workers.processor.other.payment.PaymentDetectionCommand;
import se.tink.backend.system.workers.processor.storage.LoadUserDataCommand;
import se.tink.backend.system.workers.processor.storage.PrepareTransactionsToSaveAndDeleteCommand;
import se.tink.backend.system.workers.processor.storage.RemoveRedundantTransactionsCommand;
import se.tink.backend.system.workers.processor.storage.SaveTransactionCommand;
import se.tink.backend.system.workers.processor.storage.UpdateTransactionsOnContextCommand;
import se.tink.backend.system.workers.processor.transfers.TransferDetectionCommand;
import se.tink.backend.system.workers.processor.transfers.scoring.TransferDetectionScorerFactory;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.metrics.MetricRegistry;

import java.util.List;


public class TestProcessor {

    @Inject
    protected TransactionProcessor transactionProcessor;

    @Inject
    Cluster cluster;

    protected Supplier<MarketDescriptionExtractorFactory> descriptionExtractorFactory = Suppliers.memoize(() -> MarketDescriptionExtractorFactory.byCluster(cluster));
    protected Supplier<MarketDescriptionFormatterFactory> descriptionFormatterFactory = Suppliers.memoize(() -> MarketDescriptionFormatterFactory.byCluster(cluster));
    private CategoryRepository categoryRepository;
    private CredentialsRepository credentialsRepository;
    private LoanDataRepository loanDataRepository;
    private TransactionDao transactionDao;
    private AccountRepository accountRepository;
    private MetricRegistry metricRegistry;
    private GiroRepository giroRepository;
    private MerchantRepository merchantRepository;
    private CategoryChangeRecordDao categoryChangeRecordDao;
    private CategorizationConfiguration categorizationConfiguration ;
    private CategoryConfiguration categoryConfiguration;

    @Inject
    public TestProcessor(CategoryRepository categoryRepository, CredentialsRepository credentialsRepository, LoanDataRepository loanDataRepository,
                         TransactionDao transactionDao, AccountRepository accountRepository, MetricRegistry metricRegistry, GiroRepository giroRepository,
                         MerchantRepository merchantRepository, CategoryChangeRecordDao categoryChangeRecordDao, CategorizationConfiguration categorizationConfiguration,
                         CategoryConfiguration categoryConfiguration) {
        this.categoryRepository = categoryRepository;
        this.credentialsRepository = credentialsRepository;
        this.loanDataRepository = loanDataRepository;
        this.transactionDao = transactionDao;
        this.accountRepository = accountRepository;
        this.metricRegistry = metricRegistry;
        this.giroRepository = giroRepository;
        this.merchantRepository = merchantRepository;
        this.categoryChangeRecordDao = categoryChangeRecordDao;
        this.categorizationConfiguration = categorizationConfiguration;
        this.categoryConfiguration = categoryConfiguration;
    }

    private ImmutableList<TransactionProcessorCommand> getTestCommandChain(TransactionProcessorContext context) {
        List<Category> categories = categoryRepository.findAll();
        Provider provider = context.getProvider();

        ImmutableList.Builder<TransactionProcessorCommand> builder = ImmutableList.builder();
        builder.add(
                new LoadUserDataCommand(
                        context, credentialsRepository, loanDataRepository, transactionDao, accountRepository),
                new DeduplicationCommand(context),
                new PendingTransactionCommand(context),
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
                new TransferDetectionCommand(
                        context, categoryConfiguration,
                        TransferDetectionScorerFactory.byCluster(cluster),
                        new ClusterCategories(categories), categoryChangeRecordDao
                ),
                new UnknownCategorizationCommand(
                        // TODO: Migrate to ClusterCategories.
                        categories,
                        categoryConfiguration
                ),
                new PrepareTransactionsToSaveAndDeleteCommand(context, metricRegistry),
                new SaveTransactionCommand(context, transactionDao, metricRegistry),
                new RemoveRedundantTransactionsCommand(
                        context,
                        metricRegistry,
                        transactionDao,
                        provider
                ),
                new UpdateTransactionsOnContextCommand(context)
        );

        return builder.build();
    }

    public TransactionProcessorContext process (TransactionProcessorContext context, UserData userData) {
        transactionProcessor
                .processTransactions(context, new SimpleChainFactory(this::getTestCommandChain), userData,
                        false);
        return  context;
    }

    public TransactionProcessorContext process (TransactionProcessorContext context, UserData userData, Supplier<ChainFactory> chainFactorySupplier, Boolean rethrow) {
        transactionProcessor
                .processTransactions(context, chainFactorySupplier.get(), userData,
                        rethrow);
        return  context;
    }
}
