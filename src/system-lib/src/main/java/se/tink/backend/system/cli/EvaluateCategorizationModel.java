package se.tink.backend.system.cli;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import se.tink.backend.categorization.ProbabilityCategorizer;
import se.tink.backend.categorization.interfaces.Classifier;
import se.tink.backend.categorization.lookup.CitiesByMarket;
import se.tink.backend.categorization.rules.AbnAmroCategorizationCommand;
import se.tink.backend.categorization.rules.LabelIndexCache;
import se.tink.backend.categorization.rules.NaiveBayesCategorizationCommand;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.CategoryChangeRecordDao;
import se.tink.backend.common.dao.ProviderDao;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.MarketRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.ClusterCategories;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Market;
import se.tink.backend.core.Provider;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.UserData;
import se.tink.backend.core.UserProfile;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.system.workers.processor.TransactionProcessor;
import se.tink.backend.system.workers.processor.TransactionProcessorCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;
import se.tink.backend.system.workers.processor.categorization.CategorizerCommand;
import se.tink.backend.system.workers.processor.chaining.ChainFactory;
import se.tink.backend.system.workers.processor.chaining.SimpleChainFactory;
import se.tink.backend.system.workers.processor.formatting.FormatDescriptionCommand;
import se.tink.backend.system.workers.processor.formatting.MarketDescriptionExtractorFactory;
import se.tink.backend.system.workers.processor.formatting.MarketDescriptionFormatterFactory;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.metrics.MetricRegistry;

public class EvaluateCategorizationModel extends ServiceContextCommand<ServiceConfiguration> {

    private static final CSVFormat CSV_FORMAT = CSVFormat.newFormat(';').withRecordSeparator('\n').withQuote('"')
            .withEscape('\\').withNullString("NULL");

    private final static String DEFAULT_OUTPUT_FILENAME = "data/categorization-test-output.txt";

    private final static LogUtils log = new LogUtils(EvaluateCategorizationModel.class);

    private ImmutableMap<String, Category> categoriesByCode;
    private ImmutableMap<String, Category> categoriesById;
    private ImmutableMap<String, Market> marketsByCode;
    private ClusterCategories categories;

    public EvaluateCategorizationModel() {
        super("evaluate-categorization-model", "Evaluate the categorization model for a list of transactions.");
    }

    @Override
    protected void run(
            Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext
    ) throws Exception {
        if (!configuration.isDevelopmentMode()) {
            System.err.println("Development command, please run locally");
            return;
        }

        final String input = System.getProperty("input");

        Preconditions.checkNotNull(input);

        loadCategories(serviceContext);
        loadMarkets(serviceContext);

        UserData userData = getUserData();
        List<Transaction> transactions = getTransactions(input, userData);

        process(serviceContext, userData, transactions, injector.getInstance(MetricRegistry.class));
    }

    private void process(final ServiceContext serviceContext, UserData userData, List<Transaction> transactions,
            MetricRegistry metricRegistry) {

        final String output = System.getProperty("output", DEFAULT_OUTPUT_FILENAME);

        TransactionProcessorContext context = createTransactionProcessorContext(serviceContext, userData, transactions);
        ChainFactory commandChain = new SimpleChainFactory(
                ctx -> buildTransactionProcessorCommands(serviceContext, ctx, metricRegistry));

        final Map<String, String> categorizationReferences = createCategorizationReferences(transactions);

        TransactionProcessor processor = new TransactionProcessor(
                metricRegistry
        );
        processor.processTransactions(context, commandChain, userData, false);

        try {
            BufferedWriter writer = Files.newWriter(new File(output), Charsets.UTF_8);

            for (Transaction transaction : transactions) {

                String reference = categorizationReferences.get(transaction.getId());
                String prediction = categoriesById.get(transaction.getCategoryId()).getCode();
                String description = transaction.getOriginalDescription().replace('\t', ' ');

                writer.write(String.format("%s\t%s\t%s", description, reference, prediction));
                writer.newLine();
            }

            writer.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private ImmutableList<TransactionProcessorCommand> buildTransactionProcessorCommands(ServiceContext serviceContext,
            TransactionProcessorContext context, MetricRegistry metricRegistry) {

        final Cluster cluster = serviceContext.getConfiguration().getCluster();

        LabelIndexCache labelIndexCache = LabelIndexCache.build(cluster);

        CitiesByMarket citiesByMarket;
        if (serviceContext.isProvidersOnAggregation()) {
            citiesByMarket = CitiesByMarket.build(
                    serviceContext.getAggregationControllerCommonClient().listProviders());
        } else {
            citiesByMarket = CitiesByMarket.build(serviceContext.getRepository(ProviderRepository.class).findAll());
        }

        Provider provider = context.getProvider();

        return ImmutableList.of(
                new FormatDescriptionCommand(
                        context,
                        MarketDescriptionFormatterFactory.byCluster(cluster),
                        MarketDescriptionExtractorFactory.byCluster(cluster),
                        metricRegistry,
                        provider
                ),
                new CategorizerCommand(
                        new ProbabilityCategorizer(
                                context.getUser(),
                                serviceContext.getCategoryConfiguration(),
                                metricRegistry,
                                categories,
                                buildClassifiers(labelIndexCache, citiesByMarket, provider),
                                "evaluate-categorization"), serviceContext.getDao(CategoryChangeRecordDao.class)
                )
        );
    }

    private Collection<Classifier> buildClassifiers(LabelIndexCache labelIndexCache,
            CitiesByMarket citiesByMarket, Provider provider) {

        ImmutableList.Builder<Classifier> categorizers = ImmutableList.builder();

        AbnAmroCategorizationCommand.build(provider).ifPresent(categorizers::add);
        NaiveBayesCategorizationCommand.buildAllTypes(labelIndexCache, citiesByMarket, provider)
                .forEach(categorizers::add);

        return categorizers.build();
    }

    private ImmutableMap<String, Transaction> transactionsById(List<Transaction> transactions) {
        return Maps.uniqueIndex(transactions, Transaction::getId);
    }

    private Map<String, String> createCategorizationReferences(List<Transaction> transactions) {
        return Maps.newHashMap(Maps.transformEntries(
                transactionsById(transactions),
                (transactionId, transaction) -> categoriesById.get(transaction.getCategoryId()).getCode()
        ));
    }

    private UserData getUserData() {

        UserProfile profile = new UserProfile();
        profile.setMarket(System.getProperty("market", "se").toLowerCase());

        List<String> flags = Lists.newArrayList(FeatureFlags.TINK_EMPLOYEE);

        User user = new User();
        user.setProfile(profile);
        user.setFlags(flags);

        Credentials credentials = new Credentials();
        credentials.setProviderName(System.getProperty("provider", "seb-bankid").toLowerCase());

        UserData userData = new UserData();
        userData.setUser(user);
        userData.setCredentials(Lists.newArrayList(credentials));
        userData.setTransactions(Lists.<Transaction>newArrayList());

        return userData;
    }

    private Category getCategoryByCode(String categoryCode) {
        return categoriesByCode.get(categoryCode);
    }

    private List<Transaction> getTransactions(String fileName, UserData userData) {

        final String userId = userData.getUser().getId();
        final String credentialsId = userData.getCredentials().get(0).getId();

        List<Transaction> transactions = null;

        try {
            transactions = Files.readLines(new File(fileName), Charsets.UTF_8, new LineProcessor<List<Transaction>>() {

                List<Transaction> transactions = Lists.newArrayList();

                @Override
                public List<Transaction> getResult() {
                    return transactions;
                }

                @Override
                public boolean processLine(String line) throws IOException {

                    try {
                        CSVParser parser = CSVParser.parse(line, CSV_FORMAT);
                        CSVRecord r = parser.getRecords().get(0);

                        String categoryCode = r.get(0);
                        String description = r.get(1);

                        Category category = getCategoryByCode(categoryCode);

                        if (category == null) {
                            log.error(String.format("Unable to find category for code=%s", categoryCode));
                            return true;
                        }

                        double amount = 0;

                        if (category.getType() == CategoryTypes.EXPENSES) {
                            amount = -1;
                        } else if (category.getType() == CategoryTypes.INCOME) {
                            amount = 1;
                        }

                        Transaction transaction = new Transaction();
                        transaction.setUserId(userId);
                        transaction.setCredentialsId(credentialsId);
                        transaction.setOriginalDescription(description);
                        transaction.setCategory(category);
                        transaction.setAmount(amount);

                        transactions.add(transaction);
                    } catch (IOException e) {
                        log.info(String.format("Unable to process line: [%s]", line));
                        throw e;
                    }

                    return true;
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

        return transactions;
    }

    private TransactionProcessorContext createTransactionProcessorContext(
            ServiceContext serviceContext, UserData userData, List<Transaction> transactions
    ) {
        return new TransactionProcessorContext(
                userData.getUser(),
                serviceContext.getDao(ProviderDao.class).getProvidersByName(),
                transactions,
                userData,
                userData.getCredentials().get(0).getId()
        );
    }

    private void loadMarkets(ServiceContext serviceContext) {
        setMarkets(serviceContext.getRepository(MarketRepository.class).findAll());
    }

    private void setMarkets(Iterable<Market> markets) {
        marketsByCode = Maps.uniqueIndex(markets, Market::getCodeAsString);
    }

    private void loadCategories(ServiceContext serviceContext) {
        setCategories(serviceContext.getRepository(CategoryRepository.class).findAll());
    }

    private void setCategories(List<Category> categories) {
        this.categories = new ClusterCategories(categories);
        this.categoriesByCode = Maps.uniqueIndex(categories, Category::getCode);
        this.categoriesById = Maps.uniqueIndex(categories, Category::getId);
    }
}
