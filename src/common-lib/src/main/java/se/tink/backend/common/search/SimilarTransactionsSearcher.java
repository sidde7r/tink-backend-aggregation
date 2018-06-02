package se.tink.backend.common.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.PostalCodeAreaRepository;
import se.tink.backend.common.search.containers.TransactionSearchContainer;
import se.tink.backend.common.utils.EsSearchUtils;
import se.tink.backend.core.Account;
import se.tink.backend.core.Category;
import se.tink.backend.core.Transaction;

/**
 * Looks for similar transactions that can be categorized the same way.
 */
public class SimilarTransactionsSearcher {
    private static ObjectMapper mapper = new ObjectMapper();
    private final Client client;
    private final String[] stopWords;
    private final AccountRepository accountRepository;
    private final Category excludedCategory;

    @Inject
    public SimilarTransactionsSearcher(Client client, AccountRepository accountRepository,
            PostalCodeAreaRepository postalCodeAreaRepository, CategoryRepository categoryRepository) {
        Collection<Category> categories = categoryRepository.findAll();

        this.client = client;
        this.accountRepository = accountRepository;
        this.stopWords = readStopWords(postalCodeAreaRepository);

        this.excludedCategory = categories.stream()
                .filter(c -> "transfers:exclude.other".equals(c.getCode()))
                .findFirst()
                .orElse(null); // TODO: Throw exception if missing instead.
    }

    public ArrayList<Transaction> findSimilarTransactions(Transaction transaction, String userId, String categoryId) {
        SearchResponse searchResponse = queryElasticSearchForSimilarTransactions(transaction, userId, categoryId);
        SearchHit[] hits = searchResponse.getHits().hits();

        ArrayList<Transaction> similarTransactions = new ArrayList<Transaction>();
        for (SearchHit hit : hits) {

            TransactionSearchContainer container = mapper.convertValue(hit.getSource(),
                    TransactionSearchContainer.class);

            if (!container.getTransaction().getId().equals(transaction.getId())) {
                similarTransactions.add(container.getTransaction());
            }
        }
        return similarTransactions;
    }

    public SearchResponse queryElasticSearchForSimilarTransactions(Transaction transaction, String userId,
            String categoryId) {

        SearchRequestBuilder searchRequestBuilder = new SearchRequestBuilder(client)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setIndices("transactions")
                .setTypes("transaction").setRouting(userId);

        // find excluded accounts and don't accept transactions from these accounts

        Iterable<String> excludedAccountsIds = accountRepository.findByUserId(userId).stream()
                .filter(Account::isExcluded)
                .map(Account::getId)
                .collect(Collectors.toList());

        // create query
        // Percent match
        // 1/2 = 0.50 >= 0.4
        // 1/3 = 0.33 <= 0.4
        // 2/4 = 0.50 >= 0.4
        // 2/5 = 0.40 >= 0.4
        QueryBuilder query = EsSearchUtils.getSimilarQuery(transaction.getDescription(), stopWords,
                getPercentageMatch(transaction.getDescription()));

        LinkedList<FilterBuilder> allFilters = new LinkedList<FilterBuilder>();

        allFilters.add(FilterBuilders.termFilter(Transaction.Fields.UserId, userId));

        if (!Strings.isNullOrEmpty(categoryId)) {
            allFilters.add(FilterBuilders.termFilter(Transaction.Fields.CategoryId, categoryId));
        }

        allFilters.add(FilterBuilders.notFilter(FilterBuilders.termFilter("category.id",
                excludedCategory.getId())));

        if (Iterables.size(excludedAccountsIds) > 0) {
            for (String accountId : excludedAccountsIds) {
                allFilters.add(FilterBuilders
                        .notFilter(FilterBuilders.termFilter(Transaction.Fields.AccountId, accountId)));
            }
        }

        // only allow amount sign same as search transaction

        if (transaction.getOriginalAmount() > 0) {
            allFilters.add(FilterBuilders.rangeFilter(Transaction.Fields.OriginalAmount).from(0));
        } else {
            allFilters.add(FilterBuilders.rangeFilter(Transaction.Fields.OriginalAmount).to(0));
        }

        searchRequestBuilder.setQuery(query);
        searchRequestBuilder.setFilter(FilterBuilders.andFilter(allFilters.toArray(new FilterBuilder[] {})));
        searchRequestBuilder.setSize(1000);


        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
        return searchResponse;
    }

    private float getPercentageMatch(String description) {
        int wordCount = new StringTokenizer(description).countTokens();
        if (wordCount < 3) {
            return 1.0f;
        } else {
            // For 3 or more words we allow one of the words to not be present
            return 0.9f;
        }
    }

    private String[] readStopWords(PostalCodeAreaRepository postalCodeAreaRepo) {
        List<String> stopWordsList = EsSearchUtils.getCities(postalCodeAreaRepo);
        stopWordsList.addAll(EsSearchUtils.getStopWords());
        return stopWordsList.toArray(new String[stopWordsList.size()]);
    }
}
