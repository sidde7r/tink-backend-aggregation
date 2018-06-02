package se.tink.backend.system.cli.seeding.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;
import com.google.common.io.Files;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.sourceforge.argparse4j.inf.Namespace;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.action.deletebyquery.DeleteByQueryRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.indices.IndexAlreadyExistsException;
import rx.Observable;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.mysql.main.MerchantRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.search.strategies.CoordinateNamingStrategy;
import se.tink.backend.core.Merchant;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.cli.helper.traversal.CommandLineInterfaceUserTraverser;
import se.tink.backend.system.cli.helper.traversal.CommandLineUserIdFilter;
import se.tink.backend.system.cli.helper.traversal.ThreadPoolObserverTransformer;
import se.tink.backend.utils.LogUtils;

public class RebuildSearchIndicesCommand extends ServiceContextCommand<ServiceConfiguration> {

    public RebuildSearchIndicesCommand() {
        super("rebuild-indices",
                "Rebuild (and re-seed) the search indices. Flags: index [transaction, merchant], transactionIndexMode ["
                        + RECREATE_INDEX_COMPLETELY_FOR_ALL_USERS + ", " + FOR_EACH_USER_DELETE_THEN_ADD + ", "
                        + ONLY_ADD_USER_TRANSACTIONS + "]");
    }

    private static String RECREATE_INDEX_COMPLETELY_FOR_ALL_USERS = "recreate-for-all-users";
    private static String FOR_EACH_USER_DELETE_THEN_ADD = "for-each-user-delete-then-add";
    private static String ONLY_ADD_USER_TRANSACTIONS = "for-each-user-only-add-missing";

    private static final LogUtils log = new LogUtils(RebuildSearchIndicesCommand.class);
    private static Client searchServer;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void rebuildTransactionsIndex(final ServiceContext serviceContext,
            final String indexMode, final Observable<User> allUsers) throws Exception {
        log.info("Doing transaction index");

        if (searchServer == null) {
            searchServer = serviceContext.getSearchClient();
        }

        final TransactionDao transactionDao = serviceContext.getDao(TransactionDao.class);

        if (indexMode.equals(RECREATE_INDEX_COMPLETELY_FOR_ALL_USERS)) {

            log.info("\tDeleting whole index...");

            try {
                searchServer.admin().indices().delete(new DeleteIndexRequest("transactions")).actionGet();
            } catch (Exception e) {
                // NOOP.
            }
        }

        // Always do this to make sure the index actually exists
        createTransactionIndex();

        allUsers.compose(ThreadPoolObserverTransformer.buildFromSystemPropertiesWithConcurrency(20).<User>build())
                .forEach(user -> {
                    final String userId = user.getId();
                    try {

                        List<Transaction> transactions;
                        {
                            // Separate scope to not bloat the namespace with temporary variable.

                            Stopwatch readTransactionsWatch = Stopwatch.createStarted();
                            transactions = transactionDao.findAllByUserId(userId);
                            log.trace(
                                    user.getId(),
                                    String.format("Reading up transactions took %d milliseconds.",
                                            readTransactionsWatch.elapsed(TimeUnit.MILLISECONDS)));
                        }

                        if (indexMode.equals(FOR_EACH_USER_DELETE_THEN_ADD)) {
                            DeleteByQueryRequestBuilder deleteRequestBuilder = new DeleteByQueryRequestBuilder(
                                    serviceContext.getSearchClient()).setIndices("transactions")
                                    .setTypes("transaction");

                            QueryBuilder query = QueryBuilders.termQuery(Transaction.Fields.UserId, userId);
                            deleteRequestBuilder.setQuery(query);

                            log.info(userId, "\t\tdeleting transactions");

                            Stopwatch deleteOldIndexesTransactionsWatch = Stopwatch.createStarted();
                            deleteRequestBuilder.execute().actionGet();
                            log.trace(
                                    user.getId(),
                                    String.format("Deleting old indexed transactions took %d milliseconds.",
                                            deleteOldIndexesTransactionsWatch.elapsed(TimeUnit.MILLISECONDS)));
                        }

                        log.info(userId, "\t\tindexing " + transactions.size() + " transactions");

                        {
                            // Separate scope to not bloat the namespace with temporary variable.

                            Stopwatch indexWatch = Stopwatch.createStarted();
                            transactionDao.index(transactions, false);
                            log.trace(
                                    user.getId(),
                                    String.format("Indexing transactions took %d milliseconds.",
                                            indexWatch.elapsed(TimeUnit.MILLISECONDS)));
                        }
                    } catch (Exception e) {
                        log.error("Failed to index transactions for userId: " + userId, e);
                    }
                });

        searchServer.admin().indices().flush(new FlushRequest("transactions")).actionGet();

        log.info("Transaction index rebuilt");
    }

    private static void createTransactionIndex() throws IOException {
        log.info("\tCreating whole index...");

        String settings = Files.toString(new File("data/search/search-settings-transaction.json"), Charsets.UTF_8);

        String transactionMappings = Files
                .toString(new File("data/search/search-mappings-transaction.json"), Charsets.UTF_8);

        try {
            searchServer.admin().indices().prepareCreate("transactions").setSettings(settings)
                    .addMapping("transaction", transactionMappings).execute().actionGet();
        } catch (IndexAlreadyExistsException e) {
            log.info("\t\tTransaction index already existed.");
        }
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        final UserRepository userRepository = serviceContext.getRepository(UserRepository.class);
        CommandLineUserIdFilter commandLineUserIdFilter = new CommandLineUserIdFilter();
        Observable<User> users = userRepository.streamAll()
                .compose(new CommandLineInterfaceUserTraverser(20));

        rebuildIndices(serviceContext, users);
    }

    public static void rebuildIndices(ServiceContext serviceContext, Observable<User> users)
            throws Exception {

        String indexOption = System.getProperty("index");

        String indexMode = System.getProperty("transactionIndexMode", ONLY_ADD_USER_TRANSACTIONS);

        if (indexOption == null || "transaction".equals(indexOption)) {
            rebuildTransactionsIndex(serviceContext, indexMode, users);
        }

        if (indexOption == null || "merchant".equals(indexOption)) {
            rebuildMerchantIndex(serviceContext);
        }

        searchServer.close();
        log.info("Closing search server connection");
    }

    private static void rebuildMerchantIndex(ServiceContext serviceContext) throws IOException {
        log.info("Doing merchant index");

        if (searchServer == null) {
            searchServer = serviceContext.getSearchClient();
        }

        MerchantRepository merchantRepository = serviceContext.getRepository(MerchantRepository.class);

        log.info("\tDeleting index...");

        try {
            searchServer.admin().indices().delete(new DeleteIndexRequest("merchants")).actionGet();
        } catch (Exception e) {
            // NOOP.
        }

        log.info("\tCreating index...");

        String settings = Files.toString(new File("data/search/search-settings-merchants.json"), Charsets.UTF_8);

        String merchantMappings = Files
                .toString(new File("data/search/search-mappings-merchants.json"), Charsets.UTF_8);

        searchServer.admin().indices().prepareCreate("merchants").setSettings(settings)
                .addMapping("merchant", merchantMappings).execute().actionGet();

        log.info("\tIndexing merchants...");

        Iterable<Merchant> merchants = merchantRepository.findAll();

        int count = 0;

        // Strategy will rename latitude and longitude properties
        MAPPER.setPropertyNamingStrategy(new CoordinateNamingStrategy());

        for (Merchant m : merchants) {

            // Fields marked with JsonIgnore will not be included in merchant index
            searchServer.prepareIndex("merchants", "merchant", m.getId()).setSource(MAPPER.writeValueAsString(m))
                    .execute().actionGet();
            count++;
            if (count % 10000 == 0) {
                log.info("\t\tHave indexed " + count + " merchants");
            }
        }

        searchServer.admin().indices().flush(new FlushRequest("merchants")).actionGet();

        log.info("Merchant index rebuilt");
    }

}
