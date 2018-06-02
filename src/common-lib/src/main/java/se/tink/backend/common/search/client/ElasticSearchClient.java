package se.tink.backend.common.search.client;

import com.google.common.base.Function;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import org.apache.lucene.search.Query;
import org.elasticsearch.client.Client;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryParseContext;
import org.elasticsearch.index.query.QueryParser;
import org.elasticsearch.index.query.QueryParsingException;
import se.tink.backend.common.repository.elasticsearch.TransactionSearchIndex;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.search.CounterpartSearcher;
import se.tink.backend.common.search.SimilarTransactionsSearcher;
import se.tink.backend.common.search.TransactionsSearcher;
import se.tink.backend.core.Category;
import se.tink.backend.core.SearchQuery;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;

public class ElasticSearchClient {
    private final SimilarTransactionsSearcher similarTransactionsSearcher;
    private final TransactionSearchIndex transactionSearchIndex;
    private final TransactionsSearcher transactionsSearcher;
    private final CounterpartSearcher counterpartSearcher;

    @Inject
    public ElasticSearchClient(SimilarTransactionsSearcher similarTransactionsSearcher,
            TransactionsSearcher transactionSearcher, CounterpartSearcher counterpartSearcher,
            TransactionSearchIndex transactionSearchIndex) {
        this.counterpartSearcher = counterpartSearcher;
        this.similarTransactionsSearcher = similarTransactionsSearcher;
        this.transactionsSearcher = transactionSearcher;
        this.transactionSearchIndex = transactionSearchIndex;
    }

    // Search operations
    public List<Transaction> findSimilarTransactions(Transaction transaction, String userId, String categoryId) {
        return similarTransactionsSearcher.findSimilarTransactions(transaction, userId, categoryId);
    }

    public SearchResponse queryForSimilarTransactions(Transaction transaction, String userId,
            String categoryId) {
        return similarTransactionsSearcher.queryElasticSearchForSimilarTransactions(transaction, userId, categoryId);
    }

    public List<Transaction> counterpartTransactionSuggestion(Transaction transaction, int limit) {
        return counterpartSearcher.suggest(transaction, limit);
    }

    public se.tink.backend.rpc.SearchResponse findTransactions(User user, final SearchQuery searchQuery) {
        return transactionsSearcher.query(user, searchQuery);
    }

    // Index operations
    public void index(Collection<Transaction> transactions, boolean sync,
            Function<String, Optional<Category>> categoryIdToCategory) {
        transactionSearchIndex.index(transactions, sync, categoryIdToCategory);
    }

    public void delete(Iterable<Transaction> transactionToDelete) {
        transactionSearchIndex.delete(transactionToDelete);
    }

    public void deleteByUserId(String userId, String transactionId) {
        transactionSearchIndex.deleteByUserIdAndId(userId, transactionId);
    }

    // Getters for internal ES-classes
    public CounterpartSearcher getCounterpartSearcher() {
        return counterpartSearcher;
    }

    public SimilarTransactionsSearcher getSimilarTransactionsSearcher() {
        return similarTransactionsSearcher;
    }

    public TransactionsSearcher getTransactionsSearcher() {
        return transactionsSearcher;
    }
}
