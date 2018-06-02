package se.tink.backend.system.cronjob.job;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import org.elasticsearch.action.count.CountRequestBuilder;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.User;
import se.tink.backend.system.cli.helper.traversal.UserSampleFilter;
import se.tink.backend.utils.LogUtils;

/**
 * SampleDivergingTransactionIndex
 * <p>
 * Optimistic way of finding diverging transactions:
 * <p>
 * Counts the transactions for a user in both Cassandra and ElasticSearch
 * If the results are not the same, a diverge will be reported.
 * <p>
 * The result is reported to Prometheus.
 */
public class SampleDivergingTransactionIndex {
    private AtomicInteger sampleSize = new AtomicInteger(0);
    private AtomicInteger usersWithDiverges = new AtomicInteger(0);

    private static final LogUtils log = new LogUtils(SampleDivergingTransactionIndex.class);

    private TransactionDao transactionDao;
    private UserRepository userRepository;
    private MetricRegistry metricRegistry;
    private Client searchClient;

    @Inject
    public SampleDivergingTransactionIndex(
            Client searchClient,
            TransactionDao transactionDao,
            UserRepository userRepository,
            MetricRegistry metricRegistry
    ) {
        this.transactionDao = transactionDao;
        this.userRepository = userRepository;
        this.metricRegistry = metricRegistry;
        this.searchClient = searchClient;
    }

    public void run() throws IOException {
        userRepository.streamAll()
                .filter(new UserSampleFilter(userRepository))
                .forEach(this::userDetectTransactionDivergence);

        metricRegistry.meter(MetricId.newId("diverging_transactions_sampled")).inc(sampleSize.get());
        metricRegistry.meter(MetricId.newId("diverging_transactions_users")).inc(usersWithDiverges.get());
    }

    private void userDetectTransactionDivergence(User user) {
        sampleSize.addAndGet(1);

        long cassandraCount = transactionDao.countByUser(user);

        CountRequestBuilder requestBuilder = new CountRequestBuilder(searchClient)
                .setIndices("transactions")
                .setTypes("transaction")
                .setQuery(QueryBuilders.termQuery("transaction.userId", user.getId()))
                .setRouting(user.getId());

        CountResponse response = searchClient.count(requestBuilder.request()).actionGet();

        long searchCount = response.getCount();

        if (searchCount != cassandraCount) {
            log.warn(user.getId(), String.format("Detected DivergingTransactionIndex [Cassandra:%d ElasticSearch:%d]", cassandraCount, searchCount));
            usersWithDiverges.addAndGet(1);
        }
    }

    @VisibleForTesting
    int getSampleSize() {
        return sampleSize.get();
    }

    @VisibleForTesting
    int getUsersWithDiverges() {
        return usersWithDiverges.get();
    }
}
