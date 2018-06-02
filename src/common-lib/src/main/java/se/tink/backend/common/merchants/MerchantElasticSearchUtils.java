package se.tink.backend.common.merchants;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.FieldQueryBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import se.tink.backend.common.search.containers.TransactionSearchContainer;
import se.tink.backend.core.Transaction;
import se.tink.backend.utils.LogUtils;

public class MerchantElasticSearchUtils {
    private static final LogUtils log = new LogUtils(MerchantElasticSearchUtils.class);
    private Client searchClient;
    private ObjectMapper mapper;

    public MerchantElasticSearchUtils(Client searchClient) {
        this.searchClient = searchClient;
        this.mapper = new ObjectMapper();
    }

    /**
     * Return a set of users that has one or several transactions that have been merchantized with the specified
     * merchantId
     *
     * @param merchantId
     * @return
     */
    public Set<String> findUsersWithMerchantOnTransactions(String merchantId) {

        TimeValue defaultTime = new TimeValue(60000);

        HashSet<String> users = Sets.newHashSet();

        // Make a scan and scroll search in elastic for results
        SearchRequestBuilder searchRequestBuilder = new SearchRequestBuilder(searchClient)
                .setSearchType(SearchType.SCAN).setScroll(defaultTime).setIndices("transactions")
                .setTypes("transaction");

        FieldQueryBuilder merchantFilter = QueryBuilders.fieldQuery("merchantId", merchantId);
        searchRequestBuilder.setFilter(FilterBuilders.queryFilter(merchantFilter));

        SearchResponse scrollResponse = searchRequestBuilder.execute().actionGet();

        while (true) {

            scrollResponse = searchClient.prepareSearchScroll(scrollResponse.getScrollId()).setScroll(defaultTime)
                    .execute().actionGet();

            if (scrollResponse.getHits().getHits().length == 0) {
                break;
            }

            for (SearchHit hit : scrollResponse.getHits().getHits()) {
                Transaction t = mapper.convertValue(hit.getSource(), TransactionSearchContainer.class).getTransaction();
                users.add(t.getUserId());
            }

        }

        log.debug(String.format("Search for %s found %s users", merchantId, users.size()));

        return users;
    }

    /**
     * Return a set of users that has one or several transactions that have been merchantized with any of the specified
     * merchantIds
     *
     * @param merchantIds
     * @return
     */
    public Set<String> findUsersWithMerchantOnTransactions(List<String> merchantIds) {
        HashSet<String> users = Sets.newHashSet();

        for (String merchantId : merchantIds) {
            users.addAll(findUsersWithMerchantOnTransactions(merchantId));
        }

        return users;
    }
}
