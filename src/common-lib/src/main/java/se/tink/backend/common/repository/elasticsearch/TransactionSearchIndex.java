package se.tink.backend.common.repository.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.deletebyquery.DeleteByQueryRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import se.tink.backend.common.search.SearchProxy;
import se.tink.backend.common.search.containers.TransactionSearchContainer;
import se.tink.backend.core.Category;
import se.tink.backend.core.Transaction;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;

public class TransactionSearchIndex {
    private static String TRANSACTIONS_INDEX = "transactions";
    private static String TRANSACTION_TYPE = "transaction";
    private static final MetricId INDEX_CATEGORY_METRIC = MetricId
            .newId("persist_transactions_to_index");
    private static final LogUtils log = new LogUtils(TransactionSearchIndex.class);
    private final Client elasticSearchClient;
    private final ObjectMapper objectMapper;

    private final Timer indexCategoryTimer;

    @Inject
    public TransactionSearchIndex(@Nullable Client elasticSearchClient, ObjectMapper objectMapper,
            MetricRegistry metricRegistry) {
        this.elasticSearchClient = elasticSearchClient;
        this.objectMapper = objectMapper;
        this.indexCategoryTimer = metricRegistry.timer(INDEX_CATEGORY_METRIC);
    }

    public void index(Collection<Transaction> transactions, boolean sync,
            Function<String, Optional<Category>> categoryIdToCategory) {
        Timer.Context indexTimer = indexCategoryTimer.time();
        try {
            Client client = SearchProxy.getInstance().getClient();

            BulkRequestBuilder bulkRequest = client.prepareBulk();

            log.info("Bulk indexing " + transactions.size() + " transactions");

            for (Transaction transaction : transactions) {
                Category category = clearCategorySearchTerms(categoryIdToCategory.apply(transaction.getCategoryId()));
                // Fields marked with JsonIgnore will not be included in merchant index
                String content = objectMapper.writeValueAsString(new TransactionSearchContainer(
                        transaction, category));
                bulkRequest.add(client.prepareIndex("transactions", "transaction", transaction.getId())
                        .setSource(content).setRouting(transaction.getUserId()));

            }

            if (sync) {
                bulkRequest.setRefresh(true);
            }

            bulkRequest.execute().actionGet();
        } catch (Exception e) {
            log.error("Could not index transactions", e);
        } finally {
            indexTimer.stop();
        }
    }

    private Category clearCategorySearchTerms(Optional<Category> category) {
        return category
                .map(c -> {
                    c.setSearchTerms(null);
                    return c;
                })
                .orElse(null);
    }

    public void delete(Iterable<Transaction> transactionsToDelete) {
        final BulkRequestBuilder bulkRequest = elasticSearchClient.prepareBulk();
        for (final Transaction transaction : transactionsToDelete) {
            log.debug(transaction.getUserId(), transaction.getCredentialsId(),
                    "Deleting transaction: " + transaction.getId());
            bulkRequest.add(elasticSearchClient.prepareDelete("transactions", "transaction", transaction.getId())
                    .setRouting(transaction.getUserId()));
        }
        bulkRequest.execute().actionGet();
    }

    public void deleteByUserIdAndCredentialId(String userId, String credentialsId) {
        DeleteByQueryRequest request = buildDeleteByQueryRequest(userId);
        QueryBuilder qb = QueryBuilders.boolQuery().must(
                QueryBuilders.matchQuery("credentialsId", credentialsId));
        request.query(qb);
        elasticSearchClient.deleteByQuery(request);
    }

    public void deleteByUserIdAndAccountId(String userId, String accountId) {
        DeleteByQueryRequest request = buildDeleteByQueryRequest(userId);
        QueryBuilder qb = QueryBuilders.boolQuery().must(
                QueryBuilders.matchQuery("accountId", accountId));
        request.query(qb);
        elasticSearchClient.deleteByQuery(request);
    }

    public void deleteByUserId(String userId) {
        DeleteByQueryRequest request = buildDeleteByQueryRequest(userId);
        QueryBuilder qb = QueryBuilders.boolQuery().must(
                QueryBuilders.matchQuery("userId", userId));
        request.query(qb);
        elasticSearchClient.deleteByQuery(request);
    }

    public void deleteByUserIdAndId(String userid, String transactionid) {
        DeleteRequest request = new DeleteRequest();
        request.index(TRANSACTIONS_INDEX);
        request.type(TRANSACTION_TYPE);
        request.id(transactionid);
        request.routing(userid);
        elasticSearchClient.delete(request);
    }

    private DeleteByQueryRequest buildDeleteByQueryRequest(String userId) {
        DeleteByQueryRequest request = new DeleteByQueryRequest();
        String[] indices = { TRANSACTIONS_INDEX };
        request.indices(indices);
        request.types(TRANSACTION_TYPE);
        request.routing(userId);
        return request;
    }
}
