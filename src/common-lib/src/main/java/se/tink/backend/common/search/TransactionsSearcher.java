package se.tink.backend.common.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;
import org.apache.commons.lang.time.StopWatch;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import se.tink.backend.common.ServiceContext;
import se.tink.libraries.metrics.Histogram;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.search.containers.TransactionSearchContainer;
import se.tink.backend.common.search.parsers.TimeSpanParserCommand;
import se.tink.backend.common.statistics.StatisticsGeneratorFunctions;
import se.tink.backend.common.statistics.functions.MonthlyAdjustedPeriodizationFunction;
import se.tink.backend.common.statistics.functions.WeeklyPeriodizationFunction;
import se.tink.backend.common.utils.CommonStringUtils;
import se.tink.backend.common.utils.DataUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.core.Account;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.OrderTypes;
import se.tink.backend.core.SearchQuery;
import se.tink.backend.core.SearchResult;
import se.tink.backend.core.SearchSortTypes;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.StringDoublePair;
import se.tink.backend.core.User;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.rpc.SearchResponse;
import se.tink.backend.rpc.SearchResponseMetricTypes;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.guavaimpl.Predicates;

public class TransactionsSearcher {
    private static final int DEFAULT_NUMBER_OF_RESULTS = 50;
    private static final LogUtils log = new LogUtils(TransactionsSearcher.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int MAXIMUM_NUMBER_OF_RESULTS = 100000;
    public static final Splitter QUERY_STRING_SPITTER = Splitter.on(" ").trimResults().omitEmptyStrings();

    private final Histogram searchResultsHistogram;
    private final Timer searchTimer;
    private final CredentialsRepository credentialsRepository;

    private static String getSearchHitDebugRecord(SearchHit searchHit) {
        return "id:" + searchHit.getId() + "\tindex:" + searchHit.getIndex() + "\tshard:" + searchHit.getShard()
                + "\tsourceRef:" + searchHit.getSourceRef() + "\ttype:" + searchHit.getType() + "\tversion:"
                + searchHit.getVersion() + "\texplanation:" + searchHit.getExplanation();
    }

    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final Client searchClient;
    private final SearchParser searchParser;

    /**
     * Use constructor witch receive all needed object instead.
     */
    @Deprecated
    public TransactionsSearcher(ServiceContext serviceContext, MetricRegistry metricRegistry) {
        this(serviceContext.getRepository(AccountRepository.class),
                serviceContext.getRepository(CategoryRepository.class),
                serviceContext.getRepository(CredentialsRepository.class),
                serviceContext.getSearchClient(),
                new SearchParser(),
                metricRegistry);
    }

    @Inject
    public TransactionsSearcher(AccountRepository accountRepository, CategoryRepository categoryRepository,
            CredentialsRepository credentialsRepository, Client searchClient, SearchParser searchParser,
            MetricRegistry metricRegistry) {
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.credentialsRepository = credentialsRepository;

        this.searchClient = searchClient;
        this.searchParser = searchParser;

        this.searchResultsHistogram = metricRegistry.histogram(MetricId.newId("search_hits"));
        this.searchTimer = metricRegistry.timer(MetricId.newId("search"));
    }

    private Map<String, Double> calculateCategorySplits(List<SearchResult> results, double sum) {
        Map<String, Double> categorySplits = Maps.newTreeMap();

        // Index search results keyed by category id.

        ImmutableListMultimap<String, SearchResult> resultsByCategoryId = Multimaps.index(
                Iterables.filter(results, sr -> (sr.getTransaction().getCategoryType() != CategoryTypes.TRANSFERS)),
                t -> t.getTransaction().getCategoryId());

        // Calculate the absolute sum ratio of the categories.

        for (String categoryId : resultsByCategoryId.keySet()) {
            double categorySum = 0;

            for (SearchResult result : resultsByCategoryId.get(categoryId)) {
                categorySum += Math.abs(result.getTransaction().getAmount());
            }

            categorySplits.put(categoryId, categorySum / sum);
        }

        // Sort the map based on reverse value comparison.

        Ordering<String> valueComparator = Ordering.natural().reverse().onResultOf(Functions.forMap(categorySplits))
                .compound(Ordering.<String>natural());

        return ImmutableSortedMap.copyOf(categorySplits, valueComparator);
    }

    /**
     * Calculates a time series of statistics for the specified resolution.
     *
     * @param searchParserContext
     * @param results
     */
    private List<StringDoublePair> calculateSearchStatistics(final SearchParserContext searchParserContext,
            List<SearchResult> results) {
        Function<Date, String> periodFunction;
        List<StringDoublePair> resultData = Lists.newArrayList();

        if (searchParserContext == null || searchParserContext.getResponseResolution() == null) {
            return resultData;
        }

        switch (searchParserContext.getResponseResolution()) {
        case YEARLY:
            periodFunction = StatisticsGeneratorFunctions.YEARLY_PERIODIZATION_FUNCTION;
            break;
        case MONTHLY_ADJUSTED:
            periodFunction = new MonthlyAdjustedPeriodizationFunction(searchParserContext.getUser().getProfile()
                    .getPeriodAdjustedDay());
            break;
        case MONTHLY:
            periodFunction = StatisticsGeneratorFunctions.MONTHLY_PERIODIZATION_FUNCTION;
            break;
        case DAILY:
            periodFunction = StatisticsGeneratorFunctions.DAILY_PERIODIZATION_FUNCTION;
            break;
        case WEEKLY:
            periodFunction = new WeeklyPeriodizationFunction(searchParserContext.getUser().getProfile().getLocale());
            break;
        default:
            periodFunction = StatisticsGeneratorFunctions.MONTHLY_PERIODIZATION_FUNCTION;
            break;
        }

        // Filter the search result on the most common of incomes or expenses.
        // Can only plot one way client side.

        ImmutableListMultimap<CategoryTypes, SearchResult> resultsByCategoryType = Multimaps.index(results,
                sr -> sr.getTransaction().getCategoryType());

        List<SearchResult> resultsFiltered = Lists.newArrayList();

        for (CategoryTypes type : CategoryTypes.values()) {
            if (resultsByCategoryType.get(type).size() > resultsFiltered.size()) {
                resultsFiltered = resultsByCategoryType.get(type);
            }
        }

        // Transform SearchResults in to Statistics.

        final Function<Date, String> finalPerdiodFunction = periodFunction;

        Iterable<Statistic> individualStatistics = Iterables.transform(resultsFiltered,
                sr -> {
                    Statistic statistic = new Statistic();
                    statistic.setUserId(searchParserContext.getUser().getId());
                    statistic.setResolution(searchParserContext.getResponseResolution());
                    statistic.setPeriod(finalPerdiodFunction.apply(sr.getTimestamp()));
                    statistic.setValue(sr.getTransaction().getAmount());
                    return statistic;
                });

        // Group statistics, reduce the statistic batches to single statistic per period and transform to KVPairs.

        resultData.addAll(DataUtils.aggregateStatisticsToKVPairs(individualStatistics));

        // Fill period holes from first to today's date with value = 0.

        return DataUtils.limit(DataUtils.zeroFill(
                DataUtils.pad(resultData, Sets.newHashSet(periodFunction.apply(new Date()))),
                searchParserContext.getResponseResolution()), 12);
    }

    public SearchResponse query(User user, final SearchQuery searchQuery) {
        List<Account> accounts = accountRepository.findByUserId(user.getId());

        List<Category> categories = categoryRepository.findAll(user.getProfile().getLocale());

        List<Credentials> credentials = credentialsRepository.findAllByUserId(user.getId());

        final Map<String, Account> accountsById = Maps.uniqueIndex(accounts, Account::getId);

        final Map<String, Category> categoriesById = Maps.uniqueIndex(categories, Category::getId);

        final Map<String, Date> updatedDateByCredentialsId = Maps.newHashMap();
        for (Credentials credential : credentials) {
            Date updated = credential.getUpdated();
            if (updated == null) {
                updated = new Date();
            }
            updated = DateUtils.setInclusiveEndTime(updated);
            updatedDateByCredentialsId.put(credential.getId(), updated);
        }

        // Get a list of the user's excluded account and filter the results
        // based on it.

        Set<String> excludedAccounts = Sets.newHashSet(Iterables.transform(
                Iterables.filter(accounts, a -> (a.isExcluded())), Account::getId));

        Timer.Context timerContext = searchTimer.time();

        StopWatch watch = new StopWatch();
        watch.start();

        if (searchQuery.getSort() == null) {
            searchQuery.setSort(SearchSortTypes.SCORE);
        }

        if (searchQuery.getOrder() == null) {
            searchQuery.setOrder(OrderTypes.DESC);
        }

        // Parse the query using the query parser chain.

        if (log.isTraceEnabled()) {
            if (Strings.isNullOrEmpty(searchQuery.getQueryString())) {
                log.trace(user.getId(), "Executing query: " + SerializationUtils.serializeToString(searchQuery));
            } else {
                log.trace(user.getId(), "Parsing query: " + searchQuery.getQueryString());
            }
        }

        // Build, execute and parser the search result.

        List<String> queryWords = cleanAndSplitQueryString(searchQuery);

        SearchParserContext searchParserContext = searchParser.parse(searchClient, user, searchQuery, user.getProfile()
                .getLocale(), categories, queryWords);

        List<SearchResult> results = Lists.newArrayList();

        searchAndParseResult(user, searchParserContext, excludedAccounts, results);

        // Filter the time span query words

        TimeSpanParserCommand timeSpanCommand = new TimeSpanParserCommand();
        List<String> queryWordsNoTimeTerm = timeSpanCommand.parse(queryWords, searchParserContext, false);

        // If there is a time span term, do the query again but without the time span term.

        List<SearchResult> resultsNoTimeTerm = Lists.newArrayList();

        if (queryWords.size() != queryWordsNoTimeTerm.size()) {

            // Make the search again, without the time term.

            SearchParserContext searchParserContextNoTimeTerm = searchParser.parse(searchClient, user, searchQuery,
                    user.getProfile().getLocale(), categories, queryWordsNoTimeTerm);

            searchAndParseResult(user, searchParserContextNoTimeTerm, excludedAccounts, resultsNoTimeTerm);
        } else {

            // If no time term in query, use users monthly period mode

            resultsNoTimeTerm = results;
            searchParserContext.setResponseResolution(user.getProfile().getPeriodMode());
        }

        // If client is not interested in upcoming transactions, filter them out here.

        if (!searchQuery.isIncludeUpcoming() || !FeatureFlags.FeatureFlagGroup.TRANSFERS_FEATURE
                .isFlagInGroup(user.getFlags())) {
            results = excludeUpcomingTransactions(results, updatedDateByCredentialsId);
        }

        // Sort the transactions.

        List<SearchResult> sortedResults = sortSearchResults(searchQuery, accountsById, categoriesById, results);

        // Get the page we're looking for and put upcoming flag on transaction.

        int limit = (searchQuery.getLimit() == 0) ? DEFAULT_NUMBER_OF_RESULTS : searchQuery.getLimit();
        int offset = Strings.isNullOrEmpty(searchQuery.getLastTransactionId()) ? searchQuery.getOffset() :
                findOffsetByLastTransactionId(sortedResults, searchQuery.getLastTransactionId());
        List<SearchResult> finalResults = paginateAndSetUpcoming(updatedDateByCredentialsId, sortedResults, limit,
                offset);

        // Create response.

        SearchResponse response = new SearchResponse();

        response.setCount(results.size());
        response.setQuery(searchQuery);
        response.setResults(finalResults);

        // Calculate and add search response metrics.

        response.setMetric(SearchResponseMetricTypes.COUNT, results.size());

        double sum = 0, net = 0;

        for (SearchResult result : results) {
            if (result.getTransaction().getCategoryType() == CategoryTypes.TRANSFERS) {
                continue;
            }

            sum += Math.abs(result.getTransaction().getAmount());
            net += result.getTransaction().getAmount();
        }

        response.setMetric(SearchResponseMetricTypes.SUM, sum);
        response.setMetric(SearchResponseMetricTypes.NET, net);
        response.setNet(net);
        response.setPeriodAmounts(calculateSearchStatistics(searchParserContext, resultsNoTimeTerm));

        if (results.isEmpty()) {
            response.setMetric(SearchResponseMetricTypes.AVG, 0);
            response.setMetric(SearchResponseMetricTypes.CATEGORIES, Maps.newHashMap());
        } else {
            response.setMetric(SearchResponseMetricTypes.AVG, sum / results.size());
            response.setMetric(SearchResponseMetricTypes.CATEGORIES, calculateCategorySplits(results, sum));
        }

        watch.stop();
        timerContext.stop();

        if (log.isDebugEnabled()) {
            log.debug(user.getId(), "Executed query in " + watch.toString() + " and found " + results.size()
                    + " results");
        }

        return response;
    }

    protected List<SearchResult> excludeUpcomingTransactions(List<SearchResult> results,
            Map<String, Date> updatedDateByCredentialsId) {
        results = FluentIterable
                .from(results)
                .filter(Predicates.removeUpcomingTransactions(updatedDateByCredentialsId))
                .toList();
        return results;
    }

    protected List<SearchResult> paginateAndSetUpcoming(Map<String, Date> updatedDateByCredentialsId,
            List<SearchResult> sortedResults, int limit, int offset) {

        List<SearchResult> finalResults = Lists.newArrayList();

        for (int i = offset; i < sortedResults.size(); i++) {

            if (i < (offset + limit)) {
                SearchResult searchResult = sortedResults.get(i);
                Date credentialsUpdated = updatedDateByCredentialsId
                        .get(searchResult.getTransaction().getCredentialsId());

                if (credentialsUpdated != null && searchResult.getTransaction().getDate().after(credentialsUpdated)) {
                    searchResult.getTransaction().setUpcoming(true);
                }
                finalResults.add(searchResult);
            }

        }
        return finalResults;
    }

    private int findOffsetByLastTransactionId(List<SearchResult> sortedResults, String transactionId) {
        OptionalInt index = IntStream.range(0, sortedResults.size())
                .filter(i -> Objects.equals(sortedResults.get(i).getTransaction().getId(), transactionId))
                .findFirst();

        return index.isPresent() ? index.getAsInt() + 1 : 0;
    }

    protected List<SearchResult> sortSearchResults(SearchQuery searchQuery, final Map<String, Account> accountsById,
            final Map<String, Category> categoriesById, List<SearchResult> results) {
        final SearchSortTypes sort = searchQuery.getSort();
        final OrderTypes order = searchQuery.getOrder();
        List<SearchResult> sortedResults;

        switch (sort) {
        case DATE:
            if (order == OrderTypes.ASC) {
                sortedResults = SearchResultOrdering.orderingOnDate.sortedCopy(results);
            } else {
                sortedResults = SearchResultOrdering.orderingOnDate.reverse().sortedCopy(results);
            }
            break;
        case SCORE:
            if (order == OrderTypes.ASC) {
                sortedResults = SearchResultOrdering.orderingOnScore.sortedCopy(results);
            } else {
                sortedResults = SearchResultOrdering.orderingOnScore.reverse().sortedCopy(results);
            }
            break;
        case AMOUNT:
            if (order == OrderTypes.ASC) {
                sortedResults = SearchResultOrdering.orderingOnAmount.sortedCopy(results);
            } else {
                sortedResults = SearchResultOrdering.orderingOnAmount.reverse().sortedCopy(results);
            }
            break;
        case ACCOUNT:
            Ordering<SearchResult> orderingOnAccount = new Ordering<SearchResult>() {
                @Override
                public int compare(SearchResult left, SearchResult right) {
                    int result = 0;

                    // We were seeing NPEs in this #compare implementation. Adding a NP checks for debuggability.
                    Preconditions.checkNotNull(left.getTransaction(), left);
                    Preconditions.checkNotNull(right.getTransaction(), right);
                    Preconditions.checkNotNull(accountsById.get(left.getTransaction().getAccountId()), left
                            .getTransaction().getAccountId());
                    Preconditions.checkNotNull(accountsById.get(right.getTransaction().getAccountId()), right
                            .getTransaction().getAccountId());
                    Preconditions.checkNotNull(accountsById.get(left.getTransaction().getAccountId()).getName(), left
                            .getTransaction().getAccountId());
                    Preconditions.checkNotNull(accountsById.get(right.getTransaction().getAccountId()).getName(), left
                            .getTransaction().getAccountId());

                    result = (accountsById.get(left.getTransaction().getAccountId()).getName()
                            .compareToIgnoreCase(accountsById.get(right.getTransaction().getAccountId()).getName()));

                    Preconditions.checkNotNull(left.getTransaction(), left);
                    Preconditions.checkNotNull(left.getTransaction(), right);

                    if (result == 0) {
                        result = left.getTransaction().getId().compareTo(right.getTransaction().getId());
                    }

                    if (order == OrderTypes.DESC) {
                        result = result * -1;
                    }

                    return result;
                }
            };
            sortedResults = orderingOnAccount.sortedCopy(results);
            break;
        case CATEGORY:
            Ordering<SearchResult> orderingOnCategory = new Ordering<SearchResult>() {
                @Override
                public int compare(SearchResult left, SearchResult right) {
                    int result = 0;
                    // We were seeing NPEs in this #compare implementation. Adding NP checks for debuggability.
                    Preconditions.checkNotNull(left.getTransaction(), left);
                    Preconditions.checkNotNull(right.getTransaction(), right);
                    Preconditions.checkNotNull(categoriesById.get(left.getTransaction().getCategoryId()), left
                            .getTransaction().getCategoryId());
                    Preconditions.checkNotNull(categoriesById.get(right.getTransaction().getCategoryId()), right
                            .getTransaction().getCategoryId());
                    Preconditions.checkNotNull(categoriesById.get(left.getTransaction().getCategoryId())
                            .getDisplayName(), left.getTransaction().getCategoryId());
                    Preconditions.checkNotNull(categoriesById.get(right.getTransaction().getCategoryId())
                            .getDisplayName(), right.getTransaction().getCategoryId());

                    result = (categoriesById.get(left.getTransaction().getCategoryId()).getDisplayName()
                            .compareToIgnoreCase(categoriesById.get(right.getTransaction().getCategoryId())
                                    .getDisplayName()));
                    Preconditions.checkNotNull(left.getTransaction(), left);
                    Preconditions.checkNotNull(left.getTransaction(), right);

                    if (result == 0) {
                        result = left.getTransaction().getId().compareTo(right.getTransaction().getId());
                    }

                    if (order == OrderTypes.DESC) {
                        result = result * -1;
                    }

                    return result;
                }
            };
            sortedResults = orderingOnCategory.sortedCopy(results);
            break;
        case DESCRIPTION:
            if (order == OrderTypes.ASC) {
                sortedResults = SearchResultOrdering.orderingOnDescription.sortedCopy(results);
            } else {
                sortedResults = SearchResultOrdering.orderingOnDescription.reverse().sortedCopy(results);
            }

            break;
        default:
            sortedResults = Lists.newArrayList();
            break;
        }
        return sortedResults;
    }

    /**
     * Execute query and parse result to result list.
     *
     * @param user
     * @param searchParserContext
     * @param excludedAccounts
     * @param results
     */
    private void searchAndParseResult(User user, SearchParserContext searchParserContext, Set<String> excludedAccounts,
            List<SearchResult> results) {

        // Build and execute the query without time term.

        org.elasticsearch.action.search.SearchResponse searchResponse = buildAndExecuteSearch(user,
                searchParserContext);

        SearchHit[] searchHits = searchResponse.getHits().getHits();

        // Loop hits and convert result.

        searchResultsHistogram.update(searchHits.length);

        try {
            for (int i = 0; i < searchHits.length; i++) {
                SearchHit searchHit = searchHits[i];

                if (log.isTraceEnabled()) {
                    log.trace(user.getId(), "Found: " + getSearchHitDebugRecord(searchHit));
                }

                TransactionSearchContainer transactionSearchContainer = MAPPER.readValue(searchHit.getSourceAsString(),
                        TransactionSearchContainer.class);

                // Don't include if the account is excluded.

                if (excludedAccounts.contains(transactionSearchContainer.getTransaction().getAccountId())) {
                    continue;
                }

                results.add(SearchResult.fromTransaction(transactionSearchContainer.getTransaction()));
            }
        } catch (Exception e) {
            log.error(user.getId(), "Could not fetch search hits", e);
        }
    }

    private List<String> cleanAndSplitQueryString(final SearchQuery searchQuery) {
        String cleanedQueryString = CommonStringUtils.escapeElasticSearchSearchString(searchQuery.getQueryString());
        if (cleanedQueryString != null && cleanedQueryString.length() > 0) {
            return Lists.newArrayList(QUERY_STRING_SPITTER.split(cleanedQueryString));
        }

        return Lists.newArrayList();
    }

    private org.elasticsearch.action.search.SearchResponse buildAndExecuteSearch(User user,
            SearchParserContext searchParserContext) {
        if (log.isTraceEnabled()) {
            log.trace(user.getId(), "Parsed query string: " + searchParserContext.getSearchQuery().getQueryString());
            log.trace(user.getId(), "Elastic search query: " + searchParserContext.getQueryBuilder());
        }

        SearchRequestBuilder searchRequestBuilder = new SearchRequestBuilder(searchClient)
                .setSearchType(SearchType.QUERY_THEN_FETCH).setIndices("transactions").setTypes("transaction")
                .setRouting(user.getId());

        searchRequestBuilder.setQuery(searchParserContext.getQueryBuilder());

        // We need to fetch all transactions.

        searchRequestBuilder.setSize(MAXIMUM_NUMBER_OF_RESULTS);

        return searchRequestBuilder.execute().actionGet();
    }
}
