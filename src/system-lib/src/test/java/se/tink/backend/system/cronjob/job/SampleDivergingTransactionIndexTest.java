package se.tink.backend.system.cronjob.job;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.inject.Inject;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import rx.Observable;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.retry.RetryHelper;
import se.tink.backend.core.Category;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.util.EmbeddedEalsticSearch;
import se.tink.backend.util.GuiceRunner;
import se.tink.backend.util.TestUtil;
import se.tink.libraries.metrics.MetricRegistry;

import static org.mockito.Mockito.when;

@RunWith(GuiceRunner.class)
public class SampleDivergingTransactionIndexTest {
    User user;

    @Rule
    public EmbeddedEalsticSearch embeddedEalsticSearch = new EmbeddedEalsticSearch();

    @Inject
    TestUtil testUtil;

    @Inject
    private UserRepository userRepository;

    @Inject
    private TransactionDao transactionDao;

    @Inject
    private CategoryRepository categoryRepository;

    private MetricRegistry metricRegistry;
    private Map<String, Category> categoriesById;

    @Before
    public void setUp() throws Exception {
        user = testUtil.getTestUser("SampleDivergingTransactionIndexTest");
        metricRegistry = new MetricRegistry();
        testUtil.rebuildTransactionIndex(embeddedEalsticSearch.getClient());
        categoriesById = categoryRepository.findAll().stream().collect(Collectors.toMap(c -> c.getId(), c -> c));
    }

    @Test
    public void detectMissingTransactionInIndex() throws Exception {
        when(userRepository.streamAll()).thenReturn(Observable.just(user));
        when(userRepository.count()).thenReturn(1L);

        // create, save, and index sample transactions
        List<Transaction> transactions = testUtil.getTestTransactions(user.getId());
        when(transactionDao.countByUser(user)).thenReturn(14);

        testUtil.index(embeddedEalsticSearch.getClient(), transactions, categoriesById);

        SampleDivergingTransactionIndex diverger = new SampleDivergingTransactionIndex(embeddedEalsticSearch.getClient(), transactionDao,
                userRepository, metricRegistry);
        diverger.run();

        // There should only be one user in the system (the one we just created)
        Assert.assertEquals(1, diverger.getSampleSize());

        // No diverges (all transactions are in both Cassandra and ElasticSearch)
        Assert.assertEquals(0, diverger.getUsersWithDiverges());

        // Remove a transaction from ElasticSearch
        Client searchClient = embeddedEalsticSearch.getClient();

        // Find transaction from ES
        SearchRequest searchRequest = new SearchRequestBuilder(searchClient).setIndices("transactions")
                .setTypes("transaction").setRouting(user.getId()).request();
        SearchResponse searchResponse = searchClient.search(searchRequest).actionGet();
        SearchHits hits = searchResponse.getHits();
        SearchHit hit = hits.getAt(0);

        // Delete transaction from ES
        DeleteRequest deleteRequest = new DeleteRequestBuilder(searchClient)
                .setIndex("transactions")
                .setType("transaction")
                .setRouting(user.getId())
                .setId(hit.getId()).request();

        DeleteResponse deleteResponse = searchClient.delete(deleteRequest).actionGet();
        Assert.assertFalse(deleteResponse.isNotFound());

        // ElasticSearch will eventually delete the transaction from the search index
        // Loop until we're happy or we time out

        RetryHelper retry = new RetryHelper(100, 500);
        retry.retryUntil(() -> {
            try {
                final SampleDivergingTransactionIndex shouldFailDiverger = new SampleDivergingTransactionIndex(
                        embeddedEalsticSearch.getClient(), transactionDao, userRepository, metricRegistry);
                shouldFailDiverger.run();
                return (shouldFailDiverger.getUsersWithDiverges() == 1);
            } catch (Exception e) {
                return false;
            }
        });
    }
}
