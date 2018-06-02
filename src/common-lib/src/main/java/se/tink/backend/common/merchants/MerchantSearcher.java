package se.tink.backend.common.merchants;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CharMatcher;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multiset.Entry;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.time.DateUtils;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FieldQueryBuilder;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.NumericRangeFilterBuilder;
import org.elasticsearch.index.query.OrFilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.location.transaction.TransactionBasedCityEstimator;
import se.tink.backend.common.search.client.ElasticSearchClient;
import se.tink.backend.common.repository.cassandra.MerchantWizardSkippedTransactionRepository;
import se.tink.backend.common.search.strategies.CoordinateNamingStrategy;
import se.tink.backend.core.Category;
import se.tink.backend.core.Merchant;
import se.tink.backend.core.MerchantCluster;
import se.tink.backend.core.MerchantSources;
import se.tink.backend.core.SearchQuery;
import se.tink.backend.core.SearchResult;
import se.tink.backend.core.StringStringPair;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.UserLocation;
import se.tink.backend.rpc.MerchantQuery;
import se.tink.backend.rpc.SuggestMerchantizeRequest;
import se.tink.backend.rpc.SuggestMerchantizeResponse;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.MerchantUtils;
import se.tink.backend.utils.StringUtils;

/**
 * This searcher is a factory for different search APIs, such as Google Places, Factual, Yelp etc.
 */
public class MerchantSearcher {

    private static final ObjectMapper MERCHANT_MAPPER = new ObjectMapper();
    private static final ObjectMapper TRANSACTION_MAPPER = new ObjectMapper();

    private static final int DEFAULT_NUMBER_OF_CLUSTERS = 7;
    private static final CharMatcher TRIMMER = CharMatcher.WHITESPACE;

    private static Ordering<String> descendingCountOrdering(final Multiset<String> multiset) {
        return new Ordering<String>() {
            @Override
            public int compare(String left, String right) {
                return Ints.compare(multiset.count(right), multiset.count(left));
            }
        };
    }

    private static final LogUtils log = new LogUtils(MerchantSearcher.class);
    private Client esSearchClient;
    private TransactionDao transactionDao;
    private final ElasticSearchClient elasticSearchClient;
    private MerchantWizardSkippedTransactionRepository skippedTransactionRepository;
    private MerchantSearcherUserLocationEstimator merchantUserLocationEstimator;
    private Set<String> merchantizeCategories;

    public MerchantSearcher(ServiceContext serviceContext, ElasticSearchClient elasticSearchClient) {
        esSearchClient = serviceContext.getSearchClient();
        transactionDao = serviceContext.getDao(TransactionDao.class);
        this.elasticSearchClient = elasticSearchClient;
        skippedTransactionRepository = serviceContext.getRepository(MerchantWizardSkippedTransactionRepository.class);
        MERCHANT_MAPPER.setPropertyNamingStrategy(new CoordinateNamingStrategy());
        merchantUserLocationEstimator = new MerchantSearcherUserLocationEstimator(serviceContext);
        merchantizeCategories = serviceContext.getCategoryConfiguration().getMerchantizeCodes();
    }

    /**
     * Several thread cannot user same instance of HttpDefaultClient
     *
     * @return
     */
    public GooglePlacesSearcher getGooglePlacesSearcher() {
        return new GooglePlacesSearcher();
    }

    /**
     * Searches elasticsearch for the merchant name. Special characters in the search query are escaped
     *
     * @param request  lookup request
     * @param location
     */
    public List<Merchant> lookupMerchantLocally(User user, MerchantQuery request, UserLocation location) {
        List<Merchant> merchants = Lists.newArrayList();

        SearchRequestBuilder searchRequestBuilder = new SearchRequestBuilder(esSearchClient)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setIndices("merchants").setTypes("merchant");

        String escapedQuery = QueryParser.escape(request.getQueryString());

        // Use both query and filter since "must", "must-not" logic for queries doesn't support or-logic
        searchRequestBuilder.setQuery(createLocalSearchQuery(escapedQuery, location));
        searchRequestBuilder.setFilter(createLocalSearchFilter(user));

        log.debug(user.getId(), "Query: " + formatJsonUnPretty(searchRequestBuilder.toString()));

        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();

        SearchHit[] searchHits = searchResponse.getHits().hits();

        if (searchHits.length == 0) {
            log.info(user.getId(),
                    "No hits searching merchant index for (not including google and file) " + escapedQuery);
            return merchants;
        }

        try {
            for (int i = 0; i < searchHits.length; i++) {
                SearchHit searchHit = searchHits[i];
                merchants.add(MERCHANT_MAPPER.readValue(searchHit.getSourceAsString(), Merchant.class));
            }
        } catch (Exception e) {
            log.error(user.getId(), "Could not fetch search hits", e);
        }

        return merchants;
    }

    private FilterBuilder createLocalSearchFilter(User user) {
        OrFilterBuilder userFilter = FilterBuilders.orFilter()
                .add(FilterBuilders.queryFilter(QueryBuilders.fieldQuery("visibleToUsers", user.getId())))
                .add(FilterBuilders.termFilter("online", true)); // Always include online merchants

        return userFilter;
    }

    private QueryBuilder createLocalSearchQuery(String query, UserLocation location) {
        MatchQueryBuilder googleSourceFilter = QueryBuilders.matchQuery("source", "GOOGLE");
        MatchQueryBuilder fileSourceFilter = QueryBuilders.matchQuery("source", "FILE");

        FieldQueryBuilder nameFilter = QueryBuilders.fieldQuery("name", query);

        BoolQueryBuilder filterQuery = QueryBuilders.boolQuery().mustNot(googleSourceFilter).mustNot(fileSourceFilter)
                .should(nameFilter);

        FunctionScoreQueryBuilder scoreQuery = QueryBuilders.functionScoreQuery(filterQuery);

        // Boost query on if have coordinates.
        scoreQuery.add(FilterBuilders.existsFilter("coordinates"), ScoreFunctionBuilders.factorFunction(1.5F));

        // Provide coordinates if we have any
        if (location != null) {
            // Function will boost search results if they are closer to the coordinates
            GaussDistanceDecayFunctionBuilder scoreFunction = new GaussDistanceDecayFunctionBuilder();
            scoreFunction.setLat(location.getLatitude());
            scoreFunction.setLon(location.getLongitude());
            scoreFunction.setScale("30km");
            scoreFunction.setField("coordinates");

            scoreQuery.add(scoreFunction);
        }

        return scoreQuery;
    }

    /**
     * Sort results from google search and local search.
     *
     * @param request
     * @param localHits
     * @param googleHits
     */
    public List<Merchant> sortSearchResults(MerchantQuery request, List<Merchant> localHits,
            List<Merchant> googleHits) {
        List<Merchant> allHits = Lists.newArrayList();

        // Construct a sorting key from Jaro-Winkler distance, location information and original sort order.

        TreeMap<Float, Merchant> merchantTreeMap = Maps.newTreeMap();

        // If one of the sources is empty, keep search sorting.

        if (localHits == null || localHits.size() == 0) {
            addMerchantWithKey(request, googleHits, merchantTreeMap);

        } else if (googleHits == null || googleHits.size() == 0) {
            addMerchantWithKey(request, localHits, merchantTreeMap);

        } else {

            // Remove google merchants with same placeId as in database.

            googleHits.removeAll(localHits);

            if (Strings.isNullOrEmpty(request.getQueryString())) {
                allHits.addAll(localHits);
                allHits.addAll(googleHits);
                return allHits;
            }

            addMerchantWithKey(request, localHits, merchantTreeMap);
            addMerchantWithKey(request, googleHits, merchantTreeMap);
        }

        return Lists.newArrayList(merchantTreeMap.values());
    }

    /**
     * Extra check so that we don't return merchants with the same name and address
     * Lower scored Merchants will be removed
     *
     * @param merchants
     * @return
     */
    private List<Merchant> removeDuplicatesByNameAndAddress(List<Merchant> merchants) {
        List<Merchant> result = Lists.newArrayList();
        for (Merchant m : merchants) {

            final String name = Strings.nullToEmpty(m.getName());
            final String address = Strings.nullToEmpty(m.getFormattedAddress());

            // Check so that we don't return duplicates
            boolean alreadyAdded = Iterables.any(result,
                    m1 -> Strings.nullToEmpty(m1.getName()).equalsIgnoreCase(name) && Strings
                            .nullToEmpty(m1.getFormattedAddress()).equalsIgnoreCase(address));

            if (!alreadyAdded) {
                result.add(m);
            }

        }

        return Lists.newArrayList(result);
    }

    /**
     * Creates a key from Jaro-Winkler distance from queryString and order of original search sorting.
     *
     * @param request
     * @param hits
     * @param map
     */
    private void addMerchantWithKey(MerchantQuery request, List<Merchant> hits, TreeMap<Float, Merchant> map) {
        for (int i = 1; i < hits.size() + 1; i++) {
            float keyValue = i;

            Merchant merchant = hits.get(i - 1);

            if (!Strings.isNullOrEmpty(merchant.getName())) {
                keyValue -= StringUtils.getJaroWinklerDistance(request.getQueryString(), merchant.getName());
            }

            // An item with the same name could already been added
            if (!map.containsKey(keyValue)) {
                map.put(keyValue, merchant);
            } else {
                map.put(keyValue + 0.00001F, merchant);
            }
        }
    }

    /**
     * Get a suggestion result with merchants that could improve the merchantization level. Search is done with
     * transactionsearcher to find the transactions that matches the categoryid in the request
     */
    public SuggestMerchantizeResponse suggest(User user, SuggestMerchantizeRequest request,
            List<Category> categories, boolean includeMerchants, String locale, String country) {

        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setLimit(Integer.MAX_VALUE);

        if (Strings.isNullOrEmpty(request.getCategoryId())) {

            // Use default categories.

            Iterable<String> categoryIds = Iterables.transform(Iterables.filter(categories,
                    c -> merchantizeCategories.contains(c.getCode())), Category::getId);
            searchQuery.setCategories(Lists.newArrayList(categoryIds));

        } else {
            searchQuery.setCategories(Lists.newArrayList(request.getCategoryId()));
        }

        Iterable<Transaction> transactions = Iterables
                .transform(elasticSearchClient.findTransactions(user, searchQuery).getResults(),
                        SearchResult::getTransaction);

        SuggestMerchantizeResponse response = suggest(user, request.getNumberOfClusters(), includeMerchants, locale,
                country, transactions);

        response.setClusterCategoryId(request.getCategoryId());

        return response;
    }

    /**
     * Get a suggestion result with merchants that could improve the merchantization level. Suggestion is create from
     * the transactions as input.
     */
    public SuggestMerchantizeResponse suggestFromTransactions(User user, final SuggestMerchantizeRequest request,
            boolean includeMerchants, String locale, String country, Iterable<Transaction> transactions) {

        Preconditions.checkNotNull(request.getCategoryId());

        ImmutableList<Transaction> filteredTransactions = FluentIterable.from(transactions)
                .filter(transaction -> request.getCategoryId().equals(transaction.getCategoryId())).toList();

        SuggestMerchantizeResponse response = suggest(user, request.getNumberOfClusters(), includeMerchants, locale,
                country, filteredTransactions);

        response.setClusterCategoryId(request.getCategoryId());

        return response;
    }

    /**
     * Get a suggestion result with merchants that could improve the merchantization level. Suggestion is create from
     * the transactions as input.
     */
    private SuggestMerchantizeResponse suggest(User user, int numberOfClusters, boolean includeMerchants,
            String locale, String country, Iterable<Transaction> transactions) {

        if (numberOfClusters == 0) {
            numberOfClusters = DEFAULT_NUMBER_OF_CLUSTERS;
        }

        SuggestMerchantizeResponse response = new SuggestMerchantizeResponse();
        List<MerchantCluster> clusters = Lists.newArrayList();
        response.setClusters(clusters);

        // Key on have merchant on not.

        ImmutableListMultimap<Boolean, Transaction> transactionsByHasMerchant = Multimaps
                .index(transactions, t -> t.getMerchantId() != null);

        double numberOfTransactions = Iterables.size(transactions);
        double numberOfTransactionWithMerchant = Iterables.size(transactionsByHasMerchant.get(true));
        response.setMerchantificationLevel(numberOfTransactionWithMerchant / numberOfTransactions);

        // Key transaction on description

        ImmutableListMultimap<String, Transaction> transactionsByDescription = Multimaps
                .index(transactionsByHasMerchant.get(false), Transaction::getDescription);

        // Sort multimap on descending keys.size

        ImmutableMultimap<String, Transaction> sortedTransactionsByDescription = ImmutableMultimap
                .<String, Transaction>builder().orderKeysBy(descendingCountOrdering(transactionsByDescription.keys()))
                .putAll(transactionsByDescription).build();

        // Create clusters.

        double merchantificationImprovment = 0;

        for (Multiset.Entry<String> entry : sortedTransactionsByDescription.keys().entrySet()) {
            if (clusters.size() < numberOfClusters) {
                ImmutableCollection<Transaction> elements = sortedTransactionsByDescription.get(entry.getElement());
                MerchantCluster cluster = addMerchantCluster(entry, elements, numberOfTransactions);

                // Make a query and add merchants for cluster.

                if (includeMerchants) {
                    MerchantQuery query = new MerchantQuery();
                    query.setQueryString(cluster.getDescription());
                    query.setLimit(7);
                    cluster.setMerchants(query(user, query, locale, country));
                }

                clusters.add(cluster);
                merchantificationImprovment += Iterables.size(elements);

            }
        }

        response.setMerchantificationImprovement(merchantificationImprovment / numberOfTransactions);
        return response;
    }

    /**
     * Creates a merchant cluster out of grouped transactions.
     *
     * @param entry
     * @param elements
     * @param numberOfTransactions
     */
    private MerchantCluster addMerchantCluster(Entry<String> entry, ImmutableCollection<Transaction> elements,
            double numberOfTransactions) {
        MerchantCluster cluster = new MerchantCluster();
        cluster.setDescription(entry.getElement());
        cluster.setTransactions(Lists.newArrayList(elements));
        cluster.setMerchantificationImprovement(Iterables.size(elements) / numberOfTransactions);
        return cluster;
    }

    /**
     * Makes a query to google and to elasticsearch for the string.
     *
     * @param user
     * @param query
     * @return list of merchants
     */
    public List<Merchant> query(User user, MerchantQuery query, String locale, String country) {

        query.setQueryString(TRIMMER.trimFrom(query.getQueryString()));

        if(Strings.isNullOrEmpty(query.getQueryString())){
            log.info(user.getId(), "Querying merchants with empty queryString");
            return Lists.newArrayList();
        }

        log.info(user.getId(), "Querying merchants with queryString: " + query.getQueryString());

        UserLocation location = null;

        try {
            if (query.getTransactionId() != null) {
                Transaction t = transactionDao.findOneByUserIdAndId(user.getId(), query.getTransactionId(),
                        Optional.empty());
                if (t != null) {
                    List<Transaction> transactions = findUserTransactionsCloseToDate(user.getId(), t.getDate(),
                            TransactionBasedCityEstimator.DEFAULT_NUMBER_DAYS_RADIUS);
                    location = merchantUserLocationEstimator.getUserLocation(user, t.getDate(), transactions);
                }
            }

        } catch (Exception e) {
            log.error(user.getId(), "Could not find user locations for search query", e);
        }

        List<Merchant> googleHits = Lists.newArrayList();

        try {
            Stopwatch watch = Stopwatch.createStarted();
            googleHits.addAll(getGoogleMerchants(query.getQueryString(), query.getLimit(), location, locale, country));
            log.debug("Merchant Query, google took: " + watch.elapsed(TimeUnit.MILLISECONDS) + "ms");
        } catch (Exception e) {
            log.error(user.getId(), "Could not query merchant from google.", e);
        }
        Stopwatch watch = Stopwatch.createStarted();
        List<Merchant> localHits = lookupMerchantLocally(user, query, location);
        log.debug("Merchant Query, local took: " + watch.elapsed(TimeUnit.MILLISECONDS) + "ms");

        List<Merchant> sortedMerchants = sortSearchResults(query, localHits, googleHits);
        List<Merchant> uniqueMerchants = removeDuplicatesByNameAndAddress(sortedMerchants);

        return uniqueMerchants;
    }

    private List<Transaction> findUserTransactionsCloseToDate(String userId, Date date, int daysRadius) {

        SearchRequestBuilder searchRequestBuilder = new SearchRequestBuilder(esSearchClient)
                .setSearchType(SearchType.QUERY_THEN_FETCH).setIndices("transactions").setTypes("transaction")
                .setSize(100).setRouting(userId);

        Integer from = se.tink.libraries.date.DateUtils.toInteger(DateUtils.addDays(date, -daysRadius));
        Integer to = se.tink.libraries.date.DateUtils.toInteger(DateUtils.addDays(date, daysRadius));

        NumericRangeFilterBuilder filter = FilterBuilders.numericRangeFilter("date").gte(from).lte(to);

        searchRequestBuilder.setQuery(QueryBuilders.constantScoreQuery(filter));

        log.info(userId, "Query: " + formatJsonUnPretty(searchRequestBuilder.toString()));

        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();

        SearchHit[] searchHits = searchResponse.getHits().hits();

        List<Transaction> transactions = Lists.newArrayList();

        try {
            for (SearchHit searchHit : searchHits) {
                transactions.add(TRANSACTION_MAPPER.readValue(searchHit.getSourceAsString(), Transaction.class));
            }
        } catch (Exception e) {
            log.error(userId, "Could not fetch search hits", e);
        }

        return transactions;

    }

    private List<Merchant> getGoogleMerchants(String queryString, int limit, UserLocation location, String locale, String country) throws Exception {

        GooglePlacesSearcher searcher = getGooglePlacesSearcher();
        List<StringStringPair> places;

        if (location == null) {
            log.debug("Querying google without location");
            places = searcher.autocompleteEstablishment(queryString, limit, locale, country);
        } else {
            log.info(String.format("Querying google with location %s, %s", location.getLatitude(),
                    location.getLongitude()));
            // Using constant radius of 30km since we know that that the user has been close to the location.
            // The radius has very very small weight compared to the actual search string and location
            places = searcher
                    .autocompleteEstablishment(queryString, limit, location.getLatitude(), location.getLongitude(),
                            30000, locale, country);
        }

        return MerchantUtils.createMerchantsFromAutocomplete(places, MerchantSources.GOOGLE);
    }

    private String formatJsonUnPretty(String s) {
        if (s == null) {
            return null;
        }
        return s.replace("\n", "").replace("\r", "").replaceAll("\\s+", " ");
    }
}
