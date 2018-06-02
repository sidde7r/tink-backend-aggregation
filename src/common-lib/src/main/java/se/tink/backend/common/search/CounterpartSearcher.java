package se.tink.backend.common.search;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.search.SearchHit;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.search.containers.TransactionSearchContainer;
import se.tink.backend.core.Account;
import se.tink.backend.core.SearchResult;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionTypes;
import se.tink.backend.core.exceptions.InvalidSuggestLimitException;
import se.tink.libraries.date.DateUtils;

public class CounterpartSearcher {
    private static final TypeReference<TransactionSearchContainer> CONTAINER_TYPE_REFERENCE = new TypeReference<TransactionSearchContainer>() {
    };
    private static ObjectMapper MAPPER = new ObjectMapper();

    private final static double PRICE_SCORE_WEIGHT = 1;
    private final static double TIME_SCORE_WEIGHT = 2;
    private final Integer SUGGEST_RESULT_LIMIT = 100;

    private static final Comparator<SearchResult> SORT_BY_SCORE_DESC = Comparator.comparing(SearchResult::getScore)
            .reversed();

    private AccountRepository accountRepository;
    private Client searchClient;

    @Inject
    public CounterpartSearcher(Client searchClient, AccountRepository accountRepository) {
        this.searchClient = searchClient;
        this.accountRepository = accountRepository;
    }

    private List<FilterBuilder> getDefaultFilters(String userId) {
        LinkedList<FilterBuilder> filters = Lists.newLinkedList();

        // Only include transactions for the user doing the request.
        filters.add(FilterBuilders.termFilter(Transaction.Fields.UserId, userId));

        // Exclude transactions belonging to accounts marked as excluded.
        filters.addAll(accountRepository.findByUserId(userId).stream()
                .filter(Account::isExcluded)
                .map(CounterpartSearcher::getFilterForExcludingAccount)
                .collect(Collectors.toList()));

        return filters;
    }

    private static FilterBuilder getFilterForExcludingAccount(Account account) {
        return FilterBuilders.notFilter(FilterBuilders.termFilter(Transaction.Fields.AccountId, account.getId()));
    }

    private List<FilterBuilder> getFilters(Transaction transaction) {
        List<FilterBuilder> filters = getDefaultFilters(transaction.getUserId());

        if (transaction.getOriginalAmount() > 0) {
            filters.add(FilterBuilders.rangeFilter(Transaction.Fields.OriginalAmount).to(0));
        } else {
            filters.add(FilterBuilders.rangeFilter(Transaction.Fields.OriginalAmount).from(0));
        }

        return filters;
    }

    private static double getScore(Transaction reference, Transaction candidate, float hitScore) {
        double timeScore;
        double priceScore;

        // Calculate time score.
        int daysBetween = DateUtils.daysBetween(candidate.getDate(), reference.getDate());
        if (daysBetween < 0) {
            // Look max 3 days into the future.
            timeScore = Math.max(0, 3 + daysBetween) / 3d;
        } else {
            // Look max 8 days back into the past.
            timeScore = Math.max(0, 8 - daysBetween) / 8d;
        }

        // TODO: Calculate price score.
        priceScore = 0d;

        // TODO: Include the hitScore for transactions that seem to be reimbursements from companies.

        // Calculate score as a weighted sum of scores.
        double score = (TIME_SCORE_WEIGHT * timeScore + PRICE_SCORE_WEIGHT * priceScore);
        // Normalize
        score /= (TIME_SCORE_WEIGHT + PRICE_SCORE_WEIGHT);

        return score;
    }

    private SearchRequestBuilder getSearchRequestBuilder(String userId) {
        return new SearchRequestBuilder(searchClient).setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setIndices("transactions").setTypes("transaction").setRouting(userId);
    }

    private static boolean excludeTransfers(Transaction transaction) {
        return !Objects.equals(transaction.getType(), TransactionTypes.TRANSFER);
    }

    private static SearchResult getSearchResultFromSearchHit(Transaction reference, SearchHit hit) {
        TransactionSearchContainer container = MAPPER.convertValue(hit.getSource(), CONTAINER_TYPE_REFERENCE);
        Transaction transaction = container.getTransaction();
        SearchResult result = SearchResult.fromTransaction(transaction);
        result.setScore(getScore(reference, transaction, hit.getScore()));
        return result;
    }

    public List<Transaction> suggest(Transaction transaction, int limit) throws InvalidSuggestLimitException {
        if (limit > SUGGEST_RESULT_LIMIT) {
            throw new InvalidSuggestLimitException(0, SUGGEST_RESULT_LIMIT, limit);
        }

        List<FilterBuilder> filters = getFilters(transaction);

        SearchRequestBuilder searchRequestBuilder = getSearchRequestBuilder(transaction.getUserId());
        searchRequestBuilder.setFilter(FilterBuilders.andFilter(filters.toArray(new FilterBuilder[] {})));
        searchRequestBuilder.setSize(SUGGEST_RESULT_LIMIT);

        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();

        return Arrays.stream(searchResponse.getHits().hits())
                .map(hit -> getSearchResultFromSearchHit(transaction, hit))
                .sorted(SORT_BY_SCORE_DESC)
                .map(SearchResult::getTransaction)
                // Exclude transfers. The right way would be to add filters to the query to let ES do this, but I
                // couldn't make it work (even after reindexing, including "type" in the transaction search mapping).
                // Probably because of a name clash ("type" defines the _field type_ in ES), since it worked for other
                // fields such as `categoryType`.
                .filter(CounterpartSearcher::excludeTransfers)
                .limit(limit)
                .collect(Collectors.toList());
    }
}
