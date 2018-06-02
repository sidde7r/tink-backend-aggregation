package se.tink.backend.system.cli.debug;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import java.util.Optional;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.categorization.interfaces.Classifier;
import se.tink.backend.categorization.learning.UserLearningCommand;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.ProviderDao;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.search.SimilarTransactionsSearcher;
import se.tink.backend.common.search.client.ElasticSearchClient;
import se.tink.backend.core.ClusterCategories;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.UserData;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;
import se.tink.backend.system.workers.processor.formatting.FormatDescriptionCommand;
import se.tink.backend.system.workers.processor.formatting.MarketDescriptionExtractorFactory;
import se.tink.backend.system.workers.processor.formatting.MarketDescriptionFormatterFactory;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.metrics.MetricRegistry;

public class TransactionUserLearningDebugCommand extends ServiceContextCommand<ServiceConfiguration> {
    private static final LogUtils log = new LogUtils(TransactionUserLearningDebugCommand.class);
    private Cluster cluster;
    private CredentialsRepository credentialsRepository;
    private TransactionDao transactionDao;
    private ProviderDao providerDao;
    private MetricRegistry metricRegistry;
    private ClusterCategories categories;
    private SimilarTransactionsSearcher similarTransactionsSearcher;

    public TransactionUserLearningDebugCommand() {
        super("transaction-user-learning-debug",
                "Debugs a transaction string through a specific users learning to see what it would be classified as");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        final String username = System.getProperty("username");
        final String userid = System.getProperty("userid");
        final String transactionString = System.getProperty("transaction");
        final int amount = Integer.getInteger("amount", 0);
        final String provider = System.getProperty("provider", "");

        Preconditions.checkNotNull(transactionString);
        ElasticSearchClient elasticSearchClient = injector.getInstance(ElasticSearchClient.class);
        UserRepository userRepository = injector.getInstance(UserRepository.class);
        credentialsRepository = injector.getInstance(CredentialsRepository.class);
        transactionDao = injector.getInstance(TransactionDao.class);
        similarTransactionsSearcher = elasticSearchClient.getSimilarTransactionsSearcher();
        categories = injector.getInstance(ClusterCategories.class);
        providerDao = injector.getInstance(ProviderDao.class);
        metricRegistry = injector.getInstance(MetricRegistry.class);
        cluster = injector.getInstance(ServiceConfiguration.class).getCluster();

        User user;
        if (username != null) {
            user = userRepository.findOneByUsername(username);
        } else if (userid != null) {
            user = userRepository.findOne(userid);
        } else {
            throw new NullPointerException("Username and userid cannot both be null");
        }

        log.info("user to search for is: " + user.getUsername());
        log.info("transaction to categorize is: " + transactionString + ", of value " + String.valueOf(amount));
        if (!provider.isEmpty()) {
            log.info("using provider " + provider + " for formatting");
        }

        UserData userData = createUserData(user);
        Transaction transaction = createTransaction(transactionString, amount, userData);
        process(userData,
                transaction, provider);
    }

    private void process(UserData userData, Transaction transaction, String provider) {
        TransactionProcessorContext context = new TransactionProcessorContext(
                userData.getUser(),
                providerDao.getProvidersByName(),
                Lists.newArrayList(transaction),
                userData,
                userData.getCredentials().get(0).getId()
        );

        UserLearningCommand userLearningCommand = new UserLearningCommand(userData.getUser().getId(),
                similarTransactionsSearcher, categories,
                context.getUserData().getInStoreTransactions().values());

        if (!provider.isEmpty()) {

            FormatDescriptionCommand formatDescriptionCommand = new FormatDescriptionCommand(context,
                    MarketDescriptionFormatterFactory
                            .byCluster(cluster),
                    MarketDescriptionExtractorFactory.byCluster(cluster),
                    metricRegistry,
                    providerDao.getProvidersByName().get(provider));
            formatDescriptionCommand.execute(transaction);
            log.info("description reformatted as: " + transaction.getDescription());
        } else {
            transaction.setDescription(transaction.getOriginalDescription());
        }

        Optional<Classifier.Outcome> userLearningOutcome = userLearningCommand.categorize(transaction);
        if (userLearningOutcome.isPresent()) {
            log.info("Re-classification: " + userLearningOutcome.get().vector.distributionToString());
        } else {
            log.info("Was not classified by User Learning");
        }
    }

    private UserData createUserData(User user) {
        List<Transaction> transactions = transactionDao.findAllByUserId(user.getId());
        UserData userData = new UserData();
        userData.setUser(user);
        userData.setCredentials(credentialsRepository.findAllByUserId(user.getId()));
        userData.setTransactions(transactions);

        return userData;
    }

    private Transaction createTransaction(String description, int amount, UserData userData) {
        final String userId = userData.getUser().getId();
        final String credentialsId = userData.getCredentials().get(0).getId();

        Transaction transaction = new Transaction();

        transaction.setUserId(userId);
        transaction.setCredentialsId(credentialsId);
        transaction.setOriginalDescription(description);
        transaction.setAmount((double) amount);

        return transaction;
    }
}
