package se.tink.backend.common.search;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.inject.Inject;
import java.util.LinkedList;
import java.util.List;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.OrFilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder.Operator;
import org.elasticsearch.index.query.TermFilterBuilder;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.common.search.parsers.AmountSpanSearchParserCommand;
import se.tink.backend.common.search.parsers.CategorySearchParserCommand;
import se.tink.backend.common.search.parsers.LocationSearchParserCommand;
import se.tink.backend.common.search.parsers.StopwordSearchParserCommand;
import se.tink.backend.common.search.parsers.TagSearchParserCommand;
import se.tink.backend.common.search.parsers.TimeSpanParserCommand;
import se.tink.backend.core.Category;
import se.tink.backend.core.SearchQuery;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.utils.LogUtils;
import static org.elasticsearch.index.query.QueryBuilders.filteredQuery;

public class SearchParser {
    private static final boolean DEBUG = false;
    private static final LogUtils log = new LogUtils(SearchParser.class);

    public static final Joiner QUERY_STRING_JOINER = Joiner.on(" ").skipNulls();
    public static final Splitter QUERY_STRING_SPITTER = Splitter.on(" ").trimResults().omitEmptyStrings();

    private static final String TRANSACTION_PREFIX = "transaction.";

    private final List<SearchParserCommand> commands = createDefaultCommands();

    @Inject
    public SearchParser() {
    }

    private static List<SearchParserCommand> createDefaultCommands() {
        List<SearchParserCommand> commands = new LinkedList<SearchParserCommand>();

        commands.add(new TagSearchParserCommand());
        commands.add(new AmountSpanSearchParserCommand());
        commands.add(new TimeSpanParserCommand());
        commands.add(new LocationSearchParserCommand());
        commands.add(new StopwordSearchParserCommand());
        commands.add(new CategorySearchParserCommand());

        return commands;
    }

    public SearchParserContext parse(Client searchServer, User user, SearchQuery searchQuery, String locale,
            List<Category> leafCategories, List<String> queryStringWords) {
        
        List<String> queryWords = queryStringWords;

        // Construct the initial search parser context.

        SearchParserContext context = new SearchParserContext();

        context.setSearchQuery(searchQuery);
        context.setLocale(Catalog.getLocale(locale));
        context.setCatalog(Catalog.getCatalog(locale));
        context.setCategories(leafCategories);
        context.setUser(user);

        QueryBuilder query = QueryBuilders.matchAllQuery();

        context.setQueryBuilder(query);
        
        // Set filters based on IDs. 

        setIdFilters(user, searchQuery, leafCategories, context);
        
        // Loop through the search parser commands and parse the query string.
        // Replace the queryWords with the command filtered words in order to find the real search string. 

        for (SearchParserCommand command : commands) {
            if (DEBUG) {
                log.info(user.getId(), "Stripping: " + command.getClass().getSimpleName());
            }

            queryWords = command.parse(queryWords, context, false);

            if (DEBUG) {
                log.info(user.getId(), "\tQueryString: " + QUERY_STRING_JOINER.join(queryWords));
            }
        }

        // Do everything again.

        // TODO: Don't run through the chain twice. Fix this somehow.
        // remove single quotes if any

        if (queryWords.size() > 0) {
            String queryString = QUERY_STRING_JOINER.join(queryWords);

            // remove single quotes

            if (org.apache.commons.lang.StringUtils.countMatches(queryString, "\"") == 1) {
                queryString = queryString.replace("\"", "");
            }

            query = QueryBuilders.queryString("*" + queryString + "*").field("description")
                    .field("originalDescription").field("notes").useDisMax(true).defaultOperator(Operator.AND);
        }

        context.setQueryBuilder(query);

        // Reset the queryWords string and parse it with commands again and set filters.
        
        queryWords = queryStringWords;

        for (SearchParserCommand command : commands) {
            if (DEBUG) {
                log.info(user.getId(), "Parsing: " + command.getClass().getSimpleName());
            }

            queryWords = command.parse(queryWords, context, true);
        }

        // Add any structured date range filters.

        if (searchQuery.getStartDate() != null && searchQuery.getEndDate() != null) {
            context.getQueryFilters().add(
                    FilterBuilders.rangeFilter("date").from(DateUtils.toInteger(searchQuery.getStartDate()))
                            .to(DateUtils.toInteger(searchQuery.getEndDate())));
        }

        // Construct the final query, including filters.

        QueryBuilder finalQueryBuilder = filteredQuery(context.getQueryBuilder(),
                FilterBuilders.andFilter(context.getQueryFilters().toArray(new FilterBuilder[] {})));

        context.setQueryBuilder(finalQueryBuilder);

        return context;
    }

    /**
     * Sets ES search filters based on object IDs in query. 
     * 
     * @param user
     * @param searchQuery
     * @param leafCategories
     * @param context
     */
    private void setIdFilters(User user, SearchQuery searchQuery, List<Category> leafCategories,
            SearchParserContext context) {

        // Add the only default filter.

        LinkedList<FilterBuilder> queryFilters = new LinkedList<FilterBuilder>();

        queryFilters.add(FilterBuilders.termFilter(TRANSACTION_PREFIX + Transaction.Fields.UserId, user.getId()));
        
        // Add filter for transaction id
        
        if (searchQuery.getTransactionId() != null) {
            queryFilters.add(FilterBuilders.termFilter(TRANSACTION_PREFIX + Transaction.Fields.Id, searchQuery.getTransactionId()));
        }

        // Add filters for credentials if present.
        
        if (searchQuery.getCredentials() != null) {
            
            OrFilterBuilder credentialsFilter = FilterBuilders.orFilter();
            
            for (String credentialsId : searchQuery.getCredentials()) {
                TermFilterBuilder filter = FilterBuilders.termFilter(TRANSACTION_PREFIX + Transaction.Fields.CredentialsId, credentialsId);
                credentialsFilter.add(filter);
            }
            
            queryFilters.add(credentialsFilter);
        }
        
        // Add filters for accounts if present.
        
        if (searchQuery.getAccounts() != null) {
            
            OrFilterBuilder accountsFilter = FilterBuilders.orFilter();
            
            for (String accountId : searchQuery.getAccounts()) {
                TermFilterBuilder filter = FilterBuilders.termFilter(TRANSACTION_PREFIX + Transaction.Fields.AccountId, accountId);
                accountsFilter.add(filter);
            }
            
            queryFilters.add(accountsFilter);
        }
        
        // Add filter for categories if present.

        if (searchQuery.getCategories() != null) {
            
            LinkedList<String> fannedOutCategories = new LinkedList<String>();

            for (String categoryId : searchQuery.getCategories()) {
                fannedOutCategories.add(categoryId);
                fannedOutCategories.addAll(Category.getCategoryIds(Category.getAllChildCategories(categoryId, leafCategories)));
            }
            
            OrFilterBuilder categoryFilter = FilterBuilders.orFilter();
            
            for (String categoryId : fannedOutCategories) {
                TermFilterBuilder filter = FilterBuilders.termFilter(TRANSACTION_PREFIX + Transaction.Fields.CategoryId, categoryId);
                categoryFilter.add(filter);
            }
            
            queryFilters.add(categoryFilter);
        }

        // Add filters for external ids if present.

        if (searchQuery.getExternalIds() != null) {
            OrFilterBuilder externalIdFilter = FilterBuilders.orFilter();

            for(String externalId: searchQuery.getExternalIds()){
                TermFilterBuilder filter = FilterBuilders.termFilter(TRANSACTION_PREFIX + Transaction.Fields.externalId, externalId);
                externalIdFilter.add(filter);
            }
            queryFilters.add(externalIdFilter);
        }

        context.setQueryFilters(queryFilters);
    }
}
