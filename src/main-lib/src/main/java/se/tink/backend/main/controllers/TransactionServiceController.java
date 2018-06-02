package se.tink.backend.main.controllers;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;
import se.tink.backend.categorization.factory.DefaultCategorizerFactoryCreator;
import se.tink.backend.categorization.factory.ShadowCategorizersFactoryCreator;
import se.tink.backend.categorization.interfaces.Categorizer;
import se.tink.backend.categorization.interfaces.CategorizerFactory;
import se.tink.backend.categorization.lookup.CitiesByMarket;
import se.tink.backend.categorization.rules.LabelIndexCache;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.cache.CacheScope;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.config.CategorizationConfiguration;
import se.tink.backend.common.dao.CategoryChangeRecordDao;
import se.tink.backend.common.dao.transactions.TransactionCleaner;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.exceptions.LockException;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.common.search.SuggestTransactionsSearcher;
import se.tink.backend.common.search.client.ElasticSearchClient;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryChangeRecord;
import se.tink.backend.core.ClusterCategories;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Provider;
import se.tink.backend.core.SearchQuery;
import se.tink.backend.core.SearchResult;
import se.tink.backend.core.StatisticMode;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionCluster;
import se.tink.backend.core.TransactionLink;
import se.tink.backend.core.TransactionPart;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.core.User;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.core.exceptions.InvalidCandidateException;
import se.tink.backend.core.exceptions.InvalidSuggestLimitException;
import se.tink.backend.core.exceptions.TransactionNotFoundException;
import se.tink.backend.core.exceptions.TransactionPartNotFoundException;
import se.tink.backend.firehose.v1.queue.FirehoseQueueProducer;
import se.tink.backend.firehose.v1.rpc.FirehoseMessage;
import se.tink.backend.main.controllers.exceptions.CategoryNotFoundException;
import se.tink.backend.main.controllers.exceptions.InvalidCategoryException;
import se.tink.backend.rpc.CategorizeTransactionsRequest;
import se.tink.backend.rpc.SearchResponse;
import se.tink.backend.rpc.SuggestTransactionsResponse;
import se.tink.backend.rpc.UpdateTransactionRequest;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.utils.Doubles;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.TagsUtils;
import se.tink.backend.utils.TransactionPartUtils;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.http.utils.HttpResponseHelper;
import se.tink.libraries.metrics.Histogram;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.SequenceTimer;
import se.tink.libraries.metrics.SequenceTimers;
import se.tink.libraries.metrics.Timer;
import se.tink.libraries.metrics.Timer.Context;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class TransactionServiceController {
    private static final MetricId INSTRUMENT_CATEGORIZER_PERFORMANCE_METRIC = MetricId
            .newId("instrument_categorizer_performance");
    private static final MetricId TRANSACTIONS_REQUESTED_TO_CATEGORIZE = MetricId
            .newId("requested_to_categorize_transactions");
    private static final MetricId UNIQUE_CATEGORIES_REQUESTED_TO_CATEGORIZE = MetricId
            .newId("requested_to_categorize_transactions");
    public static final ImmutableList<Integer> TRANSACTIONS_REQUESTED_TO_CATEGORIZE_BUCKETS = ImmutableList
            .of(0, 10, 20, 30, 40, 50, 100, 200, 500, 1000, 5000);

    private static class CategorizeTimers {
        private static final String CHANGE_CATEGORY = "change_category";
        private static final String GENERATE_STATISTICS = "generate_statistics";
        private static final String INSTRUMENT_CATEGORIZER_PERFORMANCE = "instrument_categorizer_performance";
        private static final String PURGE_FROM_CACHE = "purge_from_cache";
        private static final String PREPARE_CATEGORIZATION_REQUEST = "prepare_categorization_request";
    }

    private static class UpdateTransactionTimers {
        private static final String FIND_ONE_TRANSACTION = "find_one_transaction";
        private static final String UPDATE_TRANSACTION_PROPERTIES = "update_transaction_properties";
        private static final String SAVE_CATEGORY_CHANGE_RECORD = "save_category_change_record";
        private static final String SAVE_AND_INDEX = "save_and_index";
        private static final String GENERATE_STATISTICS_AND_ACTIVITIES = "generate_statistics_and_activities";
    }

    private static final LogUtils log = new LogUtils(TransactionServiceController.class);
    private static final int DEFAULT_NUMBER_OF_SUGGESTING_CLUSTERS = 7;
    private static final MetricId MANUAL_RECATEGORIZATION = MetricId.newId("manual_recategorization");
    private static final Joiner COMMA_JOINER = Joiner.on(",").skipNulls();
    private static final MetricId CATEGORIZER_PERFORMANCE_METRIC = MetricId.newId("categorizer_performance");

    private final CategoryChangeRecordDao categoryChangeRecordsDao;
    private final CategoryRepository categoryRepository;
    private final TransactionDao transactionDao;
    private final SystemServiceFactory systemServiceFactory;
    private final FirehoseQueueProducer firehoseQueueProducer;
    private final SuggestTransactionsSearcher suggestSearcher;
    private final TransactionCleaner transactionCleaner;
    private final CacheClient cacheClient;
    private final MetricRegistry metricRegistry;
    private final ListenableThreadPoolExecutor<Runnable> executor;
    private final ElasticSearchClient elasticSearchClient;
    private final Category excludedCategory;
    private final CredentialsRepository credentialsRepository;
    private final ProviderRepository providerRepository;
    private final CategorizerFactory categorizerFactory;
    private final Collection<CategorizerFactory> shadowCategorizersFactory;
    private final LabelIndexCache labelIndexCache;
    private final CitiesByMarket citiesByMarket;
    private final ClusterCategories clusterCategories;
    private final AggregationControllerCommonClient aggregationControllerClient;

    private final SequenceTimer categorizeTimer;
    private final SequenceTimer updateTransactionsTimer;
    private final CategorizationConfiguration categorizationConfiguration;
    
    private final Histogram transactionsPerCategorizationRequest;
    private final Histogram uniqueCategoriesPerCategorizationRequest;
    private final Timer instrumentCategorizerPerformanceTimer;

    private final boolean isProvidersOnAggregation;

    // TODO: Make private. See https://github.com/google/guice/wiki/KeepConstructorsHidden.
    @Inject
    public TransactionServiceController(
            CategoryChangeRecordDao categoryChangeRecordsDao,
            CategoryRepository categoryRepository,
            TransactionDao transactionDao, SystemServiceFactory systemServiceFactory,
            FirehoseQueueProducer firehoseQueueProducer,
            SuggestTransactionsSearcher suggestSearcher,
            ElasticSearchClient elasticSearchClient,
            TransactionCleaner transactionCleaner, CacheClient cacheClient,
            MetricRegistry metricRegistry,
            @Named("executor") ListenableThreadPoolExecutor<Runnable> executor,
            Category excludedCategory,
            CredentialsRepository credentialsRepository,
            ProviderRepository providerRepository,
            AggregationControllerCommonClient aggregationControllerClient,
            DefaultCategorizerFactoryCreator categorizerFactoryCreator,
            ShadowCategorizersFactoryCreator shadowCategorizersFactoryCreator,
            Cluster cluster,
            ClusterCategories clusterCategories,
            CategorizationConfiguration categorizationConfiguration,
            @Named("isProvidersOnAggregation") boolean isProvidersOnAggregation) {

        this.categoryChangeRecordsDao = categoryChangeRecordsDao;
        this.categoryRepository = categoryRepository;
        this.transactionDao = transactionDao;
        this.systemServiceFactory = systemServiceFactory;
        this.firehoseQueueProducer = firehoseQueueProducer;
        this.elasticSearchClient = elasticSearchClient;
        this.suggestSearcher = suggestSearcher;
        this.transactionCleaner = transactionCleaner;
        this.cacheClient = cacheClient;
        this.metricRegistry = metricRegistry;
        this.executor = executor;
        this.excludedCategory = excludedCategory;
        this.credentialsRepository = credentialsRepository;
        this.providerRepository = providerRepository;
        this.aggregationControllerClient = aggregationControllerClient;
        this.categorizerFactory = categorizerFactoryCreator.build();
        this.shadowCategorizersFactory = shadowCategorizersFactoryCreator.build();
        this.clusterCategories = clusterCategories;
        this.categorizationConfiguration = categorizationConfiguration;

        this.isProvidersOnAggregation = isProvidersOnAggregation;
        this.categorizeTimer = new SequenceTimer(TransactionServiceController.class, metricRegistry,
                SequenceTimers.CATEGORIZE);
        this.updateTransactionsTimer = new SequenceTimer(TransactionServiceController.class, metricRegistry,
                SequenceTimers.MAIN_UPDATE_TRANSACTIONS);

        labelIndexCache = LabelIndexCache.build(cluster);
        if (isProvidersOnAggregation) {
            citiesByMarket = CitiesByMarket.build(aggregationControllerClient.listProviders());
        } else {
            citiesByMarket = CitiesByMarket.build(providerRepository.findAll());
        }

        instrumentCategorizerPerformanceTimer = metricRegistry.timer(INSTRUMENT_CATEGORIZER_PERFORMANCE_METRIC);
        transactionsPerCategorizationRequest = metricRegistry.histogram(TRANSACTIONS_REQUESTED_TO_CATEGORIZE,
                TRANSACTIONS_REQUESTED_TO_CATEGORIZE_BUCKETS);
        uniqueCategoriesPerCategorizationRequest = metricRegistry.histogram(UNIQUE_CATEGORIES_REQUESTED_TO_CATEGORIZE);
    }

    public List<Transaction> categorize(User user,
            List<CategorizeTransactionsRequest> categorizeTransactionsRequests) {

        transactionsPerCategorizationRequest
                .update(categorizeTransactionsRequests.stream()
                        .filter(r -> r.getTransactionIds() != null)
                        .mapToInt(r -> r.getTransactionIds().size()).sum());

        final SequenceTimer.Context sequenceTimerContext = categorizeTimer.time();
        final List<CategoryChangeRecord> categoryChangeRecords = Lists.newArrayList();
        // Create a set with category and a list of the transactions that we should be changed.
        sequenceTimerContext.mark(CategorizeTimers.PREPARE_CATEGORIZATION_REQUEST);
        ListMultimap<Category, Transaction> transactionsByCategory = prepareCategorizationRequests(user,
                categorizeTransactionsRequests);
        uniqueCategoriesPerCategorizationRequest.update(transactionsByCategory.asMap().size());

        sequenceTimerContext.mark(CategorizeTimers.CHANGE_CATEGORY);
        for (Category category : transactionsByCategory.keySet()) {
            categoryChangeRecords.addAll(changeCategory(user, category, transactionsByCategory.get(category)));
        }

        executor.execute(() -> {
            Collection<Transaction> transactions = transactionsByCategory.values();
            for (Transaction transaction : transactions) {
                if (categoryChangeRecordsDao.findAllByUserIdAndId(UUIDUtils.fromTinkUUID(user.getId()),
                        UUIDUtils.fromTinkUUID(transaction.getId())).stream()
                        .filter(ccr -> ccr.getCommand().startsWith("RandomCategoryResetCommand")).count() > 0) {
                    instrumentCategorizerPerformance(user, transaction);
                }
            }
        });

        if (categoryChangeRecords.isEmpty()) {
            return Collections.emptyList();
        }

        sequenceTimerContext.mark(CategorizeTimers.PURGE_FROM_CACHE);
        purgeSuggestFromCache(user, transactionsByCategory.values());

        sequenceTimerContext.mark(CategorizeTimers.GENERATE_STATISTICS);
        systemServiceFactory.getProcessService()
                .generateStatisticsAndActivitiesWithoutNotifications(user.getId(), StatisticMode.SIMPLE);
        sequenceTimerContext.stop();

        // Save change records.
        executor.execute(() -> {
            categoryChangeRecordsDao.save(categoryChangeRecords);
            metricRegistry.meter(MANUAL_RECATEGORIZATION).inc(categoryChangeRecords.size());
        });

        ArrayList<Transaction> updatedTransactions = Lists.newArrayList(transactionsByCategory.values());
        firehoseQueueProducer.sendTransactionsMessage(user.getId(), FirehoseMessage.Type.UPDATE, updatedTransactions);
        return updatedTransactions;
    }

    public Transaction getTransaction(User user, String id) throws NoSuchElementException {
        Transaction transaction = transactionDao.findOneByUserAndId(user, id);
        if (transaction == null) {
            throw new NoSuchElementException();
        }
        return transaction;
    }

    public List<Transaction> findSimilarTransactions(User user, String id, boolean includeSelf)
            throws NoSuchElementException {
        return findSimilarTransactions(user, id, null, includeSelf);
    }

    public SearchResponse searchTransactions(User user, SearchQuery searchQuery) {
        return elasticSearchClient.findTransactions(user, searchQuery);
    }

    public List<Transaction> findSimilarTransactions(User user, String id, String categoryId, boolean includeSelf)
            throws NoSuchElementException {

        Transaction transaction = getTransaction(user, id);

        List<Transaction> transactions = elasticSearchClient
                .findSimilarTransactions(transaction, user.getId(), categoryId);

        if (includeSelf) {
            transactions.add(transaction);
        }

        // If user is not allowed TRANSFER feature, don't show upcoming transactions.

        if (!FeatureFlags.FeatureFlagGroup.TRANSFERS_FEATURE.isFlagInGroup(user.getFlags())) {
            transactions = filterOutUpcomingTransactions(transactions);
        }
        return transactions;
    }

    public SuggestTransactionsResponse suggest(User user, int numberOfClusters, final boolean evaluateEverything)
            throws LockException {
        if (numberOfClusters == 0) {
            numberOfClusters = DEFAULT_NUMBER_OF_SUGGESTING_CLUSTERS;
        }

        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setLimit(Integer.MAX_VALUE);
        List<Transaction> transactionsResponse = searchTransactions(user, searchQuery).getResults().stream()
                .map(SearchResult::getTransaction)
                .collect(Collectors.toList());

        return suggestSearcher.suggest(user, numberOfClusters, evaluateEverything, transactionsResponse);
    }

    private List<Transaction> filterOutUpcomingTransactions(List<Transaction> transactions) {
        final Date endOfDayToday = DateUtils.setInclusiveEndTime(new Date());

        Predicate<Transaction> beforeOrTodayPredicate = input -> input != null && input.getDate().before(endOfDayToday);

        return transactions.stream()
                .filter(beforeOrTodayPredicate)
                .collect(Collectors.toList());
    }

    public Transaction updateTransactions(String userId, String id, Transaction transaction, boolean generateStatistics,
            boolean saveAndIndex) throws NoSuchElementException, IllegalArgumentException {
        return updateTransactions(userId, id,
                new UpdateTransactionRequest(transaction.getAmount(), transaction.getCategoryId(),
                        transaction.getDate(), transaction.getOriginalDate(), transaction.getDescription(), transaction.getNotes()),
                generateStatistics, saveAndIndex);
    }

    public Transaction updateTransactions(String userId, String id, UpdateTransactionRequest updateTransactionRequest,
            boolean generateStatistics, boolean saveAndIndex) throws NoSuchElementException, IllegalArgumentException {
        SequenceTimer.Context sequenceTimer = updateTransactionsTimer.time();
        sequenceTimer.mark(UpdateTransactionTimers.FIND_ONE_TRANSACTION);
        Optional<Integer> period = Optional.ofNullable(DateUtils.getYearMonth(updateTransactionRequest.getOriginalDate()));
        Transaction unmodifiedTransaction = transactionDao.findOneByUserIdAndId(userId, id, period);

        if (unmodifiedTransaction == null) {
            throw new NoSuchElementException();
        }

        if (!id.equals(unmodifiedTransaction.getId())) {
            // Putting this check after authorization check so we can trace who did this. This log message could
            // potentially mean someone is trying to circumvent our systems.
            log.warn("Transaction id does not match request body id: " + id + " != " + unmodifiedTransaction.getId());
            throw new IllegalArgumentException();
        }
        sequenceTimer.mark(UpdateTransactionTimers.UPDATE_TRANSACTION_PROPERTIES);
        boolean userModifyCategory = isModifiedField(updateTransactionRequest.getCategoryId(),
                unmodifiedTransaction.getCategoryId());
        Optional<String> modifiedCategory = Optional.ofNullable(unmodifiedTransaction.getCategoryId());

        updateTransactionProperties(unmodifiedTransaction, updateTransactionRequest);
        sequenceTimer.stop();

        if (userModifyCategory) {
            sequenceTimer.mark(UpdateTransactionTimers.SAVE_CATEGORY_CHANGE_RECORD);
            categoryChangeRecordsDao.save(CategoryChangeRecord.createChangeRecord(unmodifiedTransaction,
                    modifiedCategory, "UserChanged"));
            sequenceTimer.stop();
        }

        if (saveAndIndex) {
            sequenceTimer.mark(UpdateTransactionTimers.SAVE_AND_INDEX);
            // Saving transactions synchronous since follow items are streamed to clients and they require all
            // transactions to be updated.
            transactionDao.saveAndIndex(userId, Collections.singletonList(unmodifiedTransaction), true);
            sequenceTimer.stop();
        }

        if (generateStatistics) {
            purgeSuggestFromCache(userId, Sets.newHashSet(unmodifiedTransaction.getId()));
            sequenceTimer.mark(UpdateTransactionTimers.GENERATE_STATISTICS_AND_ACTIVITIES);
            systemServiceFactory.getProcessService()
                    .generateStatisticsAndActivitiesWithoutNotifications(userId, StatisticMode.FULL);
            sequenceTimer.stop();
        }

        firehoseQueueProducer.sendTransactionMessage(userId, FirehoseMessage.Type.UPDATE, unmodifiedTransaction);
        return unmodifiedTransaction;
    }

    private void updateTransactionProperties(Transaction transaction, UpdateTransactionRequest updateRequest) {
        if (isModifiedField(updateRequest.getAmount(), transaction.getAmount())) {
            transaction.setAmount(updateRequest.getAmount());
            transaction.setUserModifiedAmount(true);
        }

        if (isModifiedField(updateRequest.getCategoryId(), transaction.getCategoryId())) {
            String categoryId = transactionCleaner.getReplacementCategory(updateRequest.getCategoryId())
                    .map(Category::getId)
                    .orElse(updateRequest.getCategoryId());
            transaction.setCategory(categoryId, transaction.getCategoryType());
            transaction.setUserModifiedCategory(true);
        }

        if (updateRequest.getDate() != null) {
            // Flatten date to 12:00:00
            final Date newDate = DateUtils.flattenTime(updateRequest.getDate());

            // Don't modify the date if the the user didn't change the day
            if (!DateUtils.isSameDay(newDate, transaction.getDate())) {
                transaction.setDate(newDate);
                transaction.setUserModifiedDate(true);
            }
        }

        if (isModifiedField(updateRequest.getDescription(), transaction.getDescription())) {
            if (Strings.isNullOrEmpty(updateRequest.getDescription())) {
                transaction.setUserModifiedDescription(false);
                String descriptionReset = !Strings.isNullOrEmpty(transaction.getFormattedDescription()) ?
                        transaction.getFormattedDescription() : transaction.getOriginalDescription();
                transaction.setDescription(descriptionReset);
            } else {
                transaction.setDescription(updateRequest.getDescription());
                transaction.setUserModifiedDescription(true);
            }
        }

        String note = TagsUtils.buildNote(updateRequest.getNotes(), updateRequest.getTags());

        if (isModifiedField(note, transaction.getNotes())) {
            transaction.setNotes(note);
        }

    }

    private <T> boolean isModifiedField(T newField, T oldField) {
        return newField != null && !Objects.equals(newField, oldField);
    }

    private boolean isModifiedField(Double newField, Double oldField) {
        return newField != null && !Doubles.fuzzyEquals(newField, oldField, 0.001);
    }

    public void purgeSuggestFromCache(User user, Collection<Transaction> transactions) {
        purgeSuggestFromCache(user.getId(), transactions.stream().map(Transaction::getId).collect(Collectors.toSet()));
    }

    /**
     * Change category on a list of transactions.
     *
     * @return a list of CategoryChangeRecord describing the change.
     */
    private List<CategoryChangeRecord> changeCategory(User user, Category category,
            List<Transaction> transactions) {
        List<CategoryChangeRecord> records = Lists.newArrayList();

        for (Transaction transaction : transactions) {
            Optional<String> oldCategoryId = Optional.ofNullable(transaction.getCategoryId());

            transaction.setCategory(category);
            transaction.setUserModifiedCategory(true);

            records.add(CategoryChangeRecord.createChangeRecord(transaction, oldCategoryId, "UserChanged"));
        }

        Map<String, Integer> transactionIdsToPeriod = transactions.stream()
                .collect(Collectors.toMap(Transaction::getId, t -> {
                    Integer period = DateUtils.getYearMonth(t.getOriginalDate());
                    return period;
                }));

        // Save and index the updated transactions
        transactionDao.updateCategoryByIdsAndUser(user, transactionIdsToPeriod, category);

        // Saving transactions synchronous since follow items are streamed to clients and they require all transactions
        // to be updated.
        transactionDao.index(transactions, true);
        return records;
    }

    private ListMultimap<Category, Transaction> prepareCategorizationRequests(User user,
            List<CategorizeTransactionsRequest> requests) {
        ListMultimap<Category, Transaction> result = ArrayListMultimap.create();

        for (CategorizeTransactionsRequest request : requests) {

            final String categoryId = request.getCategoryId();
            final List<String> transactionIds = request.getTransactionIds();

            if (transactionIds == null || transactionIds.isEmpty()) {
                continue;
            }

            Category category = transactionCleaner.getReplacementCategory(categoryId)
                    .orElseGet(() -> categoryRepository.findById(categoryId));

            if (category == null) {
                continue;
            }

            List<Transaction> transactions = transactionDao.findByUserIdAndId(user, transactionIds);

            // Deduplicate transaction ids to be categorized. There are frontend clients that occasionally submit the
            // same transaction id twice. Without deduplication, this would lead to BAD_REQUEST down the road in this
            // method.

            Set<String> databaseIds = transactions.stream().map(Transaction::getId).collect(Collectors.toSet());

            validateCategorizationInput(user, Sets.newHashSet(transactionIds), databaseIds);

            result.putAll(category, transactions);
        }
        return result;
    }

    /**
     * Validate that the inputs contains the same ids.
     */
    private void validateCategorizationInput(User user, Set<String> clientIds, Set<String> databaseIds) {

        // Debug logging to pinpoint which transaction is missing and where.
        Sets.SetView<String> transactionIdsNotInDatabase = Sets.difference(clientIds, databaseIds);

        // We have all transactions
        if (transactionIdsNotInDatabase.isEmpty()) {
            return;
        }

        log.warn(user.getId(), String.format("Transaction count mismatch (%d != %d). Not in DB: [%s]",
                clientIds.size(), databaseIds.size(), COMMA_JOINER.join(transactionIdsNotInDatabase)));

        // Tell the client something went wrong.
        HttpResponseHelper.error(Response.Status.BAD_REQUEST);
    }

    /**
     * Purges the suggest response from cache if any of the transactions in the suggest clusters has been modified.
     *
     * @param userId
     * @param transactionIds
     */
    private void purgeSuggestFromCache(String userId, Set<String> transactionIds) {
        SuggestTransactionsResponse suggest = SerializationUtils.deserializeFromBinary(
                (byte[]) cacheClient.get(CacheScope.SUGGEST_TRANSACTIONS_RESPONSE_BY_USERID, userId),
                SuggestTransactionsResponse.class);

        if (suggest == null) {
            return;
        }

        boolean purge = false;

        clusterLoop:
        for (TransactionCluster cluster : suggest.getClusters()) {
            for (Transaction transaction : cluster.getTransactions()) {
                if (transactionIds.contains(transaction.getId())) {
                    purge = true;
                    break clusterLoop;
                }
            }
        }

        if (purge) {
            log.info(userId, "Invalidating suggest cache");
            cacheClient.delete(CacheScope.SUGGEST_TRANSACTIONS_RESPONSE_BY_USERID, userId);
        }
    }

    public TransactionLink link(User user, String transactionId1, String transactionId2)
            throws TransactionNotFoundException, InvalidCandidateException {

        Transaction transaction1 = transactionDao.findOneByUserIdAndId(user.getId(), transactionId1, Optional.empty());
        Transaction transaction2 = transactionDao.findOneByUserIdAndId(user.getId(), transactionId2, Optional.empty());

        if (transaction1 == null) {
            throw new TransactionNotFoundException("The primary transaction could not be found.");
        }

        if (transaction2 == null) {
            throw new TransactionNotFoundException("The secondary transaction could not be found.");
        }

        TransactionPartUtils.link(transaction1, transaction2, excludedCategory);

        transactionDao.saveAndIndex(user, Lists.newArrayList(transaction1, transaction2), true);

        return new TransactionLink(transaction1, transaction2);
    }

    public Transaction categorizePart(String userId, String transactionId, String partId, String categoryId)
            throws TransactionNotFoundException, TransactionPartNotFoundException, InvalidCandidateException,
            CategoryNotFoundException, InvalidCategoryException {

        Transaction transaction = transactionDao.findOneByUserIdAndId(userId, transactionId, Optional.empty());

        if (transaction == null) {
            throw new TransactionNotFoundException(
                    String.format("The transaction could not be found [transactionId:%s].", transactionId));
        }

        if (!transaction.hasParts()) {
            throw new TransactionPartNotFoundException(
                    String.format("The transaction has not parts [transactionId:%s].", transactionId));
        }

        TransactionPart part = transaction.getParts().stream().filter(p -> Objects.equals(p.getId(), partId))
                .findFirst().orElse(null);

        if (part == null) {
            throw new TransactionPartNotFoundException(String.format(
                    "The transaction part could not be found on transaction [transactionId:%s, transactionPartId:%s].",
                    transactionId, partId));
        }

        if (!Strings.isNullOrEmpty(part.getCounterpartTransactionId()) && !Strings
                .isNullOrEmpty(part.getCounterpartId())) {
            throw new InvalidCandidateException("Linked transactions parts cannot be re-categorized.");
        }

        Category category = categoryRepository.findById(categoryId);

        if (category == null) {
            throw new CategoryNotFoundException(String.format("Category was not found [categoryId:%s].", categoryId));
        }

        category = transactionCleaner.getReplacementCategory(category.getId()).orElse(category);

        if (!part.isValidCategory(category)) {
            throw new InvalidCategoryException(
                    String.format("The category is not applicable to the part [categoryCode:%s].", category.getCode()));
        }

        part.setCategoryId(category.getId());
        part.setLastModified(new Date());

        // TODO: Update the category of the _transaction_ as well. Based on the dispensable part, or otherwise the biggest category (amount-wise) among the parts?

        transactionDao.saveAndIndex(userId, Collections.singletonList(transaction), false);

        return transaction;
    }

    public TransactionLink deletePart(String userId, String transactionId, String partId)
            throws TransactionNotFoundException, TransactionPartNotFoundException, InvalidCandidateException {

        Transaction transaction = transactionDao.findOneByUserIdAndId(userId, transactionId, Optional.empty());

        if (transaction == null) {
            throw new TransactionNotFoundException("The transaction could not be found.");
        }

        TransactionPart deletedPart = deletePart(userId, transaction, partId);

        Transaction counterPartTransaction = null;
        String counterpartTransactionId = deletedPart.getCounterpartTransactionId();
        String counterpartId = deletedPart.getCounterpartId();

        // If available, delete counterpart as well.
        if (!Strings.isNullOrEmpty(counterpartTransactionId) && !Strings.isNullOrEmpty(counterpartId)) {
            try {
                counterPartTransaction = transactionDao.findOneByUserIdAndId(userId, counterpartTransactionId,
                        Optional.empty());

                if (counterPartTransaction == null) {
                    log.error(userId, "Transaction could not be found.");
                } else {
                    deletePart(userId, counterPartTransaction, counterpartId);
                }
            } catch (Exception e) {
                // Don't fail the primary delete call just because the counterpart couldn't be deleted.
                log.error(userId, "Unable to delete counterpart.", e);
            }
        }

        return new TransactionLink(transaction, counterPartTransaction);
    }

    private TransactionPart deletePart(String userId, Transaction transaction, String partId) {
        if (!transaction.hasParts()) {
            throw new TransactionPartNotFoundException("The transaction part could not be found.");
        }

        TransactionPart partToBeDeleted = transaction.getParts().stream().filter(p -> Objects.equals(p.getId(), partId))
                .findFirst().orElse(null);

        if (partToBeDeleted == null) {
            throw new TransactionPartNotFoundException("The transaction part could not be found.");
        }

        // Remove the part, this will also update the dispensable amount
        transaction.removePart(partToBeDeleted);

        // Update the transaction.
        transactionDao.saveAndIndex(userId, Collections.singletonList(transaction), false);

        return partToBeDeleted;
    }

    public List<Transaction> linkSuggest(String userId, String transactionId, int limit)
            throws TransactionNotFoundException, InvalidSuggestLimitException {

        Transaction transaction = transactionDao.findOneByUserIdAndId(userId, transactionId, Optional.empty());

        if (transaction == null) {
            throw new TransactionNotFoundException("The transaction could not be found.");
        }

        return elasticSearchClient.counterpartTransactionSuggestion(transaction, limit);
    }

    public Transaction setLinkPromptAnswer(String userId, String transactionId, String answer)
            throws TransactionNotFoundException {

        // An answer is required.
        if (Strings.isNullOrEmpty(answer)) {
            throw new IllegalArgumentException("No answer supplied.");
        }

        answer = answer.toUpperCase();

        // Only YES and NO are valid answers.
        if (!"YES".equals(answer) && !"NO".equals(answer)) {
            throw new IllegalArgumentException(
                    String.format("Invalid answer (\"%s\"). Only \"YES\" or \"NO\" allowed.", answer));
        }

        Transaction transaction = transactionDao.findOneByUserIdAndId(userId, transactionId, Optional.empty());

        if (transaction == null) {
            throw new TransactionNotFoundException("The transaction could not be found.");
        }

        transaction.setPayload(TransactionPayloadTypes.LINK_COUNTERPART_PROMPT_ANSWER, answer);
        transactionDao.saveAndIndex(userId, Collections.singletonList(transaction), false);
        return transaction;
    }

    private void instrumentCategorizerPerformance(User user, Transaction transaction) {
        Context timer = instrumentCategorizerPerformanceTimer.time();
        try {
            Credentials credential = credentialsRepository.findOne(transaction.getCredentialsId());
            Provider provider = findProviderByName(credential.getProviderName());

            Categorizer activeCategorizer = categorizerFactory
                    .build(user, provider, Collections.singleton(transaction), labelIndexCache, citiesByMarket,
                            categorizationConfiguration
                    );
            Collection<Categorizer> categorizers = shadowCategorizersFactory.stream()
                    .map(factory -> factory
                            .build(user, provider, Collections.singleton(transaction), labelIndexCache, citiesByMarket,
                                    categorizationConfiguration))
                    .collect(Collectors.toList());
            categorizers.add(activeCategorizer);

            for (Categorizer categorizer : categorizers) {
                Category category = categorizer.categorize(transaction);
                final String outcome;
                if (category.getId().equals(transaction.getCategoryId())) {
                    outcome = "correct";
                } else if (sameParentCategory(transaction, category)) {
                    outcome = "partially_correct";
                } else {
                    outcome = "incorrect";
                }

                metricRegistry.meter(
                        CATEGORIZER_PERFORMANCE_METRIC
                                .label("categorizer", categorizer.getLabel())
                                .label("outcome", outcome)
                                .label("market", user.getProfile().getMarket()))
                        .inc();
            }
        } finally {
            timer.stop();
        }
    }

    private boolean sameParentCategory(Transaction transaction, Category predictedCategory) {
        return clusterCategories.get().stream().anyMatch(
                c -> c.getId().equals(transaction.getCategoryId()) && c.getPrimaryName()
                        .equals(predictedCategory.getPrimaryName()));
    }

    private Provider findProviderByName(String name) {
        if (isProvidersOnAggregation) {
            return aggregationControllerClient.getProviderByName(name);
        } else {
            return providerRepository.findByName(name);
        }
    }
}
