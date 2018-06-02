package se.tink.backend.combined;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.netflix.governator.guice.LifecycleInjector;
import io.dropwizard.configuration.ConfigurationFactory;
import java.util.Collections;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.Client;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.mockito.Mockito;
import se.tink.backend.aggregation.aggregationcontroller.AggregationControllerAggregationClient;
import se.tink.backend.aggregation.client.AggregationServiceFactory;
import se.tink.backend.aggregation.client.InProcessAggregationServiceFactory;
import se.tink.backend.aggregation.resources.AggregationServiceResource;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.categorization.client.FastTextServiceFactory;
import se.tink.backend.client.InProcessServiceFactoryBuilder;
import se.tink.backend.client.ServiceFactory;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.CategorizationConfiguration;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.FraudDetailsRepository;
import se.tink.backend.common.repository.mysql.main.GiroRepository;
import se.tink.backend.common.repository.mysql.main.MerchantRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.search.client.ElasticSearchClient;
import se.tink.backend.common.tasks.kafka.KafkaQueueResetter;
import se.tink.backend.core.Account;
import se.tink.backend.core.Category;
import se.tink.backend.core.ClusterCategories;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.Market;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionTypes;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.encryption.client.InProcessEncryptionServiceFactory;
import se.tink.backend.encryption.resources.EncryptionServiceResource;
import se.tink.backend.firehose.v1.queue.DummyFirehoseQueueProducer;
import se.tink.backend.guice.configuration.TestModuleFactory;
import se.tink.backend.insights.client.InsightsServiceFactory;
import se.tink.backend.product.execution.ProductExecutorServiceFactory;
import se.tink.backend.rpc.DeleteUserRequest;
import se.tink.backend.rpc.FraudActivationRequest;
import se.tink.backend.system.cli.seeding.search.RebuildSearchIndicesCommand;
import se.tink.backend.system.client.InProcessSystemServiceFactoryBuilder;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.system.resources.CronServiceResource;
import se.tink.backend.system.workers.processor.chaining.DefaultUserChainFactoryCreator;
import se.tink.backend.system.workers.processor.formatting.MarketDescriptionExtractorFactory;
import se.tink.backend.system.workers.processor.formatting.MarketDescriptionFormatterFactory;
import se.tink.backend.utils.StringUtils;
import se.tink.credentials.demo.DemoCredentials;
import se.tink.libraries.auth.HttpAuthenticationMethod;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.libraries.dropwizard.DropwizardObjectMapperConfigurator;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.uuid.UUIDUtils;

import javax.validation.Validation;
import javax.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class AbstractServiceIntegrationTest extends AbstractTest {
    protected static final Market DEFAULT_MARKET = new Market("SE", "sv_SE");
    private static final String LOCALHOST_URL = "http://localhost:9090/";
    private static final File CONFIG_FILE = new File("etc/development.yml");
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
    private static final ObjectMapper MAPPER = DropwizardObjectMapperConfigurator
            .newUnknownPropertiesSafeObjectMapper();
    private static ConfigurationFactory<ServiceConfiguration> CONFIGURATION_FACTORY = new ConfigurationFactory<>(
            ServiceConfiguration.class, VALIDATOR, MAPPER, "");

    protected static boolean initialized;
    protected static Injector injector;

    protected static ServiceContext serviceContext;
    protected static ServiceFactory serviceFactory;

    private static AggregationServiceResource aggregationServiceResource;
    protected static SystemServiceFactory systemServiceFactory;
    private static EncryptionServiceResource encryptionServiceResource;
    protected static ServiceConfiguration configuration;
    protected static CategoryRepository categoryRepository;
    protected static CategoryConfiguration categoryConfiguration;
    protected static CredentialsRepository credentialsRepository;
    protected static LoanDataRepository loanDataRepository;
    protected static TransactionDao transactionDao;
    protected static AccountRepository accountRepository;
    protected static MerchantRepository merchantRepository;
    protected static GiroRepository giroRepository;
    private static final String STATIC_ACCOUNT_ID = UUIDUtils.toTinkUUID(UUID.randomUUID());
    protected static UserRepository userRepository;
    protected static MetricRegistry metricRegistry;
    protected static AggregationServiceFactory aggregationServiceFactory;

    protected static CategorizationConfiguration categorizationConfiguration;
    protected static ProductExecutorServiceFactory productExecutorServiceFactory;

    /**
     * Deletes the current user.
     */
    protected void deleteUser(User user) {
        serviceFactory.getUserService().delete(authenticated(user), new DeleteUserRequest());
    }

    @AfterClass
    public static void postCleanUp() throws Exception {
        try {
            try {
                RebuildSearchIndicesCommand.rebuildTransactionsIndex(serviceContext, "recreate",
                        userRepository.streamAll().publish());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            // Yes, these should probably be nested in lots of try/finally, to try to shut down as many as possible.

            if (aggregationServiceResource != null) {
                aggregationServiceResource.stop();
                aggregationServiceResource = null;
            }

            encryptionServiceResource = null;
        }

    initialized = false;
    }

    @BeforeClass
    public static void preSetup() throws Exception {
        if (initialized) {
            return;
        }

        configuration = CONFIGURATION_FACTORY.build(CONFIG_FILE);
        List<Module> modules = TestModuleFactory.getDefaultModules(configuration);
        injector = LifecycleInjector.builder()
                .inStage(Stage.PRODUCTION)
                .withModules(modules)
                .build().createInjector();

        setupInProcessServices();

        categoryRepository = serviceContext.getRepository(CategoryRepository.class);
        categoryConfiguration = serviceContext.getCategoryConfiguration();
        userRepository = serviceContext.getRepository(UserRepository.class);
        transactionDao = serviceContext.getDao(TransactionDao.class);
        credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        loanDataRepository = serviceContext.getRepository(LoanDataRepository.class);
        accountRepository = serviceContext.getRepository(AccountRepository.class);
        merchantRepository = serviceContext.getRepository(MerchantRepository.class);
        giroRepository = serviceContext.getRepository(GiroRepository.class);
        categorizationConfiguration = serviceContext.getConfiguration().getCategorization();

        productExecutorServiceFactory = injector.getInstance(ProductExecutorServiceFactory.class);

        initialized = true;
    }

    /**
     * Helper method to create a user without demo credentials and data.
     */
    protected User registerUser(String username, String password, UserProfile profile) {
        return super.registerUser(serviceFactory.getUserService(), username, password, profile);
    }

    /**
     * Helper method to create a user with demo credentials and data.
     */
    protected User registerTestUserWithDemoCredentialsAndData() throws Exception {
        return registerTestUserWithDemoCredentialsAndData(createUserProfile(), "anv1");
    }

    protected User registerTestUserWithDemoCredentialsAndData(String userName) throws Exception {
        return registerTestUserWithDemoCredentialsAndData(createUserProfile(), userName);
    }

    /**
     * Helper method to create a user with demo credentials and data.
     */
    protected User registerTestUserWithDemoCredentialsAndData(UserProfile profile, String userName) throws Exception {
        User user = registerUser(randomUsername(), "testing", profile);

        serviceFactory.getCredentialsService().create(authenticated(user), null,
                createCredentials(userName), Collections.emptySet());

        waitForRefresh(user);

        return user;
    }

    /**
     * Helper method to create credentials
     */
    protected Credentials createCredentials(String userName) {
        Credentials credentials = new Credentials();

        credentials.setProviderName("demo");
        credentials.setType(CredentialsTypes.PASSWORD);
        credentials.setUsername(userName);
        credentials.setPassword("demo");

        return credentials;
    }

    /**
     * Helper method to create a user with real credentials
     */
    protected User registerRealUser(String userName, String password, String agent) throws InterruptedException,
            ParseException {

        User sessionId = registerUser(randomUsername(), "testing", createUserProfile());

        Credentials newCredentials = new Credentials();

        newCredentials.setProviderName(agent);
        newCredentials.setType(CredentialsTypes.PASSWORD);
        newCredentials.setUsername(userName);
        newCredentials.setPassword(password);

        serviceFactory.getCredentialsService().create(authenticated(sessionId), null,
                newCredentials, Collections.emptySet());

        waitForRefresh(sessionId);
        return sessionId;
    }

    /**
     * Helper method to create a user with real credentials
     */
    protected User registerRealUserWithBankId(String userName, String agent) throws InterruptedException,
            ParseException {

        User sessionId = registerUser(randomUsername(), "testing", createUserProfile());

        Credentials newCredentials = new Credentials();

        newCredentials.setProviderName(agent);
        newCredentials.setType(CredentialsTypes.MOBILE_BANKID);
        newCredentials.setUsername(userName);

        serviceFactory.getCredentialsService().create(authenticated(sessionId), null, newCredentials,
                Collections.emptySet());

        waitForRefresh(sessionId);
        return sessionId;
    }

    /**
     * Getter method to get all service implementation classes
     *
     * @throws Exception
     */
    private static void setupInProcessServices() throws Exception {
        serviceContext = injector.getInstance(ServiceContext.class);
        metricRegistry = injector.getInstance(MetricRegistry.class);
        serviceContext.start();


        InProcessServiceFactoryBuilder inProcessServiceFactoryBuilder = new InProcessServiceFactoryBuilder(
                serviceContext, Optional.empty(), AbstractServiceIntegrationTest.metricRegistry, injector.getInstance(ClusterCategories.class));

        InProcessAggregationServiceFactory inProcessAggregationServiceFactory = (InProcessAggregationServiceFactory) serviceContext
                .getAggregationServiceFactory();
        InProcessEncryptionServiceFactory inProcessEncryptionServiceFactory = (InProcessEncryptionServiceFactory) serviceContext
                .getEncryptionServiceFactory();

        Cluster cluster = serviceContext.getConfiguration().getCluster();

        // Important that instantiation of these are done lazily to speed up tests.
        Supplier<MarketDescriptionFormatterFactory> descriptionFormatterFactory = Suppliers
                .memoize(() -> MarketDescriptionFormatterFactory.byCluster(cluster));
        Supplier<MarketDescriptionExtractorFactory> descriptionExtractorFactory = Suppliers
                .memoize(() -> MarketDescriptionExtractorFactory.byCluster(cluster));

        InProcessSystemServiceFactoryBuilder inProcessSystemServiceFactoryBuilder = new InProcessSystemServiceFactoryBuilder(
                serviceContext, injector.getInstance(InsightsServiceFactory.class), Optional.empty(), Optional.empty(), injector.getInstance(CronServiceResource.class),
                metricRegistry);

        DefaultUserChainFactoryCreator userChainFactory = Mockito.mock(DefaultUserChainFactoryCreator.class);
        systemServiceFactory = inProcessSystemServiceFactoryBuilder
                .buildAndRegister(new KafkaQueueResetter(), new DummyFirehoseQueueProducer(),
                        () -> descriptionFormatterFactory.get(), () -> descriptionExtractorFactory.get(),
                        injector.getInstance(ClusterCategories.class),
                        injector.getInstance(FastTextServiceFactory.class),
                        injector.getInstance(ElasticSearchClient.class),
                        userChainFactory);

        Assert.assertNull(aggregationServiceResource);
        aggregationServiceResource = new AggregationServiceResource(serviceContext,
                AbstractServiceIntegrationTest.metricRegistry, false,
                Mockito.mock(AggregationControllerAggregationClient.class));
        aggregationServiceResource.start(); // Starting before handing it over to inProcessAggregationServiceFactory in
        // case inProcessAggregationServiceFactory does something to/with it.
        inProcessAggregationServiceFactory.setAggregationService(aggregationServiceResource);
        aggregationServiceFactory = inProcessAggregationServiceFactory;
        Assert.assertNull(encryptionServiceResource);
        encryptionServiceResource = new EncryptionServiceResource(
                serviceContext.getConfiguration().isRequireInjection());
        inProcessEncryptionServiceFactory.setEncryptionService(encryptionServiceResource);

        serviceFactory = inProcessServiceFactoryBuilder.buildAndRegister(injector);
    }

    /**
     * Helper method for polling the status of the refresh-credentials request.
     *
     * @throws InterruptedException
     */
    protected void waitForRefresh(User sessionId) throws InterruptedException {
        waitForRefresh(sessionId, serviceFactory.getCredentialsService());
    }

    protected User createUserAndActivateFraud(boolean useCredentialsService) throws Exception {
        return createUserAndActivateFraud(DemoCredentials.USER4.getUsername(), useCredentialsService);
    }

    protected User createUserAndActivateFraud(String userName, boolean useCredentialsService) throws Exception {
        User user = registerTestUserWithDemoCredentialsAndData(userName);
        user.getFlags().add(FeatureFlags.FRAUD_PROTECTION);
        user.getProfile().setFraudPersonNumber(userName);

        waitForRefresh(user);

        if (useCredentialsService) {
            Credentials fraudCredentials = new Credentials();
            fraudCredentials.setProviderName("creditsafe");
            fraudCredentials.setType(CredentialsTypes.FRAUD);
            fraudCredentials.setUsername(userName);

            serviceFactory.getCredentialsService().create(authenticated(user), null, fraudCredentials,
                    Collections.emptySet());
        } else {
            FraudActivationRequest fraudActivationReq = new FraudActivationRequest();
            fraudActivationReq.setActivate(true);
            fraudActivationReq.setPersonIdentityNumber(userName);

            serviceFactory.getFraudService().activation(authenticated(user), fraudActivationReq);
        }

        waitForFraudDataToRefreshRefresh(user);
        return user;
    }

    protected AuthenticatedUser authenticated(User user) {
        return new AuthenticatedUser(HttpAuthenticationMethod.BASIC, user);
    }

    protected void waitForFraudDataToRefreshRefresh(User user) throws InterruptedException {
        for (int i = 0; i < 15; i++) {
            List<FraudDetails> fraudDetails = serviceContext.getRepository(FraudDetailsRepository.class)
                    .findAllByUserId(user.getId());

            if (fraudDetails == null || fraudDetails.size() == 0) {
                log.info(user.getId(), "Waiting for fraud to be activated.");
                Thread.sleep(1000);
            } else {
                return;
            }
        }
        Assert.assertTrue("Timed out waiting for fraud to be activated", false);
    }

    protected User getTestUser(String testName) {
        UserProfile profile = new UserProfile();
        profile.setCurrency("SEK");
        profile.setPeriodAdjustedDay(25);
        profile.setPeriodMode(ResolutionTypes.MONTHLY_ADJUSTED);
        profile.setLocale("sv_SE");
        profile.setMarket("SE");

        User user = new User();
        user.setId(StringUtils.generateUUID());
        user.setPassword(testName);
        user.setUsername(testName);
        user.setProfile(profile);
        user.setFlags(Collections.emptyList());
        return user;
    }

    protected List<User> getTestUsers(String testName) {
        return getTestUserWithFeatures(testName, Collections.emptyList());
    }

    protected List<User> getTestUserWithFeatures(String testName, List<String> flags) {
        List<User> users = ImmutableList.of(flags)
                .stream()
                .map(f -> {
                    User user;
                    String userName;
                    if (f.isEmpty()){
                        userName = testName;
                        user = userRepository.findOneByUsername(userName);
                    }
                    else{
                        userName = testName + ".flagged";
                        user = userRepository.findOneByUsername(userName);
                    }

                    if (user == null) {
                        user = new User();
                        user.setId(StringUtils.generateUUID());
                        user.setPassword(userName);
                        user.setUsername(userName);
                        user.setFlags(f);
                        UserProfile profile = new UserProfile();
                        profile.setCurrency("SEK");
                        profile.setPeriodAdjustedDay(25);
                        profile.setPeriodMode(ResolutionTypes.MONTHLY_ADJUSTED);
                        profile.setLocale("sv_SE");
                        profile.setMarket("SE");
                        user.setProfile(profile);
                        userRepository.save(user);
                    }

                    Account account = new Account();
                    account.setBalance(550);
                    account.setName(testName);
                    account.setUserId(user.getId());
                    account.setId(testName);
                    accountRepository.save(account);
                    return user;
                }).collect(Collectors.toList());
        return users;
    }

    protected List<Transaction> getTestTransactions(String userId) {
        List<Transaction> transactions = new ArrayList<>();

        transactions.add(getNewTransaction(userId, -10, "BEIJING 8 DROTTN"));
        transactions.add(getNewTransaction(userId, -20, "HAIR SOLUTION"));
        transactions.add(getNewTransaction(userId, -30, "SALUPLATS HUSMAN"));
        transactions.add(getNewTransaction(userId, -40, "URBAN CAFE"));
        transactions.add(getNewTransaction(userId, -50, "ICA MAXI MATHORNAN S"));
        transactions.add(getNewTransaction(userId, -60, "CSN"));
        transactions.add(getNewTransaction(userId, -70, "B2 Bredband AB"));
        transactions.add(getNewTransaction(userId, -80, "TELE2 SVERIGE AB"));
        transactions.add(getNewTransaction(userId, -90, "TRANAN RESTAURAN"));
        transactions.add(getNewTransaction(userId, -100, "ICA DALASTAN"));
        transactions.add(getNewTransaction(userId, -80, "MAXI MAXI"));
        transactions.add(getNewTransaction(userId, -80, "COOP MAXI"));
        transactions.add(getNewTransaction(userId, -40, "URBAN CAFE"));
        transactions.add(getNewTransaction(userId, 400, "URBAN CAFE"));

        return transactions;
    }

    protected Transaction getNewTransaction(String userId, double amount, String description) {
        return getNewTransaction(userId, amount, description, "0",
                "" + LocalDate.now().getDayOfMonth());
    }

    protected Transaction getNewTransaction(String userId, double amount, String description, String monthsAgo,
                                         String day) {
        Transaction transaction = new Transaction();
        transaction.setOriginalDate(stringToDate(monthsAgo, day));
        transaction.setDate(stringToDate(monthsAgo, day));
        transaction.setAmount(amount);
        transaction.setOriginalAmount(amount);
        transaction.setDescription(description);
        transaction.setOriginalDescription(description);
        transaction.setAccountId(STATIC_ACCOUNT_ID);
        transaction.setTimestamp(System.currentTimeMillis());
        transaction.setUserId(userId);
        transaction.setCredentialsId(userId);
        transaction.setType(TransactionTypes.DEFAULT);

        Category barCategory = categoryRepository.findByCode(categoryConfiguration.getBarsCode());

        transaction.setCategory(barCategory);

        return transaction;
    }

    protected Date stringToDate(String monthsAgo, String day) {
        try {
            int dayOfMonth = Integer.parseInt(day);
            int minusMonths = Integer.parseInt(monthsAgo);
            LocalDate now = LocalDate.now();
            LocalDate twoMonthsAgo = now.minusMonths(minusMonths);

            if (dayOfMonth > 28 && twoMonthsAgo.getMonthOfYear() == 2) {
                dayOfMonth = 28;
            }

            twoMonthsAgo = twoMonthsAgo.withDayOfMonth(dayOfMonth);
            Date instant = twoMonthsAgo.toDate();
            return instant;
        } catch (NumberFormatException e) {
            throw new AssertionError("Couldn't string date to timestamp");
        }
    }

    protected List<Transaction> queryUsersPendingTransactions(User user) {
        return Lists.newArrayList(
                Iterables.filter(transactionDao.findAllByUserIdAndTime(user.getId(), DateTime.now().minusYears(1), DateTime.now().plusYears(1)), Transaction::isPending));
    }

    protected List<Transaction> queryTransactionsByUserId(String userId) {
        return transactionDao.findAllByUserIdAndTime(userId, DateTime.now().minusYears(1), DateTime.now().plusYears(1));
    }

    protected List<Transaction> queryTransactionsByUserIdAndAccountId(String userId, final String accountId) {
        return Lists.newArrayList(
                Iterables.filter(transactionDao.findAllByUserIdAndTime(userId, DateTime.now().minusYears(1), DateTime.now().plusYears(1)),
                        t -> t.getAccountId().equals(accountId)));
    }

    protected Transaction queryTransactionsByUserIdAndId(String userId, final String id) {
        return transactionDao.findOneByUserIdAndId(userId, id, Optional.empty());
    }

    protected void rebuildTransactionIndex() {
        log.info("\tDeleting index...");

        Client searchServer = serviceContext.getSearchClient();
        try {
            searchServer.admin().indices().delete(new DeleteIndexRequest("transactions")).actionGet();
        } catch (Exception e) {
            // NOOP.
        }

        log.info("\tCreating index...");

        String settings = null;
        String transactionMappings = null;
        try {
            settings = Files.toString(new File("data/search/search-settings-transaction.json"), Charsets.UTF_8);
            transactionMappings = Files.toString(new File("data/search/search-mappings-transaction.json"),
                    Charsets.UTF_8);

        } catch (IOException e) {
            e.printStackTrace();
        }

        searchServer.admin().indices().prepareCreate("transactions").setSettings(settings)
                .addMapping("transaction", transactionMappings).execute().actionGet();

    }
}
