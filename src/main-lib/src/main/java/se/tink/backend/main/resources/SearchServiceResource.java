package se.tink.backend.main.resources;

import com.google.common.base.Strings;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response.Status;
import se.tink.backend.api.SearchService;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.controllers.AnalyticsController;
import se.tink.backend.common.search.client.ElasticSearchClient;
import se.tink.backend.core.OrderTypes;
import se.tink.backend.core.SearchQuery;
import se.tink.backend.core.SearchSortTypes;
import se.tink.backend.core.User;
import se.tink.backend.rpc.SearchResponse;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.http.utils.HttpResponseHelper;

@Path("/api/v1/search")
public class SearchServiceResource implements SearchService {
    private final AnalyticsController analyticsController;
    private final ElasticSearchClient elasticSearchClient;
    private static final LogUtils log = new LogUtils(SearchServiceResource.class);

    public SearchServiceResource(ServiceContext context, ElasticSearchClient elasticSearchClient) {
        analyticsController = new AnalyticsController(context.getEventTracker());
        this.elasticSearchClient = elasticSearchClient;
    }

    @Override
    public SearchResponse searchQuery(User user, final SearchQuery searchQuery) {
        if (searchQuery == null) {
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        try {
            return elasticSearchClient.findTransactions(user, searchQuery);
        } catch (Exception e) {
            log.error(user.getId(), "Could not execute query", e);
            HttpResponseHelper.error(Status.INTERNAL_SERVER_ERROR);
            return null;
        }
    }

    @Override
    public SearchResponse searchQuery(User user, String queryString, int offset, int limit, String sort, String order) {
        SearchQuery searchQuery = new SearchQuery();

        searchQuery.setQueryString(StringUtils.trim(Strings.nullToEmpty(queryString)));
        searchQuery.setOffset(offset);
        searchQuery.setLimit(limit);
        searchQuery.setSort(SearchSortTypes.SCORE);
        searchQuery.setOrder(OrderTypes.DESC);

        if (sort != null) {
            try {
                searchQuery.setSort(SearchSortTypes.valueOf(sort.toUpperCase()));
            } catch (Exception e) {
                log.warn("Could not parse sort: " + sort);
            }
        }

        if (order != null) {
            try {
                searchQuery.setOrder(OrderTypes.valueOf(order.toUpperCase()));
            } catch (Exception e) {
                log.warn("Could not parse order: " + order);
            }
        }

        return searchQuery(user, searchQuery);
    }
}
