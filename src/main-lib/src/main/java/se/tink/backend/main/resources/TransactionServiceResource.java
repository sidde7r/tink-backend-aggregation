package se.tink.backend.main.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import io.dropwizard.lifecycle.Managed;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import se.tink.backend.api.SearchService;
import se.tink.backend.api.TransactionService;
import se.tink.backend.categorization.factory.DefaultCategorizerFactoryCreator;
import se.tink.backend.categorization.factory.ShadowCategorizersFactoryCreator;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.dao.CategoryChangeRecordDao;
import se.tink.backend.common.dao.transactions.TransactionCleaner;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.providers.CurrenciesByCodeProvider;
import se.tink.backend.common.repository.cassandra.CategoryChangeRecordRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.CurrencyRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.common.search.SuggestTransactionsSearcher;
import se.tink.backend.common.search.client.ElasticSearchClient;
import se.tink.backend.common.statistics.StatisticsGeneratorAggregator;
import se.tink.backend.core.ClusterCategories;
import se.tink.backend.core.OrderTypes;
import se.tink.backend.core.SearchQuery;
import se.tink.backend.core.SearchResult;
import se.tink.backend.core.SearchSortTypes;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.StatisticMode;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionFraudStatus;
import se.tink.backend.core.TransactionLink;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.core.TransactionQuery;
import se.tink.backend.core.TransactionSortTypes;
import se.tink.backend.core.User;
import se.tink.backend.core.enums.FeatureFlags;
import se.tink.backend.core.exceptions.InvalidCandidateException;
import se.tink.backend.core.exceptions.InvalidSuggestLimitException;
import se.tink.backend.core.exceptions.TransactionNotFoundException;
import se.tink.backend.core.exceptions.TransactionPartNotFoundException;
import se.tink.backend.firehose.v1.queue.FirehoseQueueProducer;
import se.tink.backend.main.controllers.TransactionServiceController;
import se.tink.backend.main.controllers.exceptions.CategoryNotFoundException;
import se.tink.backend.main.controllers.exceptions.InvalidCategoryException;
import se.tink.backend.rpc.CategorizeTransactionPartRequest;
import se.tink.backend.rpc.CategorizeTransactionPartResponse;
import se.tink.backend.rpc.CategorizeTransactionsListRequest;
import se.tink.backend.rpc.CategorizeTransactionsRequest;
import se.tink.backend.rpc.DeleteTransactionPartResponse;
import se.tink.backend.rpc.LinkTransactionsResponse;
import se.tink.backend.rpc.SearchResponse;
import se.tink.backend.rpc.SimilarTransactionsResponse;
import se.tink.backend.rpc.SuggestTransactionsResponse;
import se.tink.backend.rpc.TransactionFraudulentRequest;
import se.tink.backend.rpc.TransactionLinkPromptRequest;
import se.tink.backend.rpc.TransactionLinkPromptResponse;
import se.tink.backend.rpc.TransactionLinkSuggestionResponse;
import se.tink.backend.rpc.TransactionQueryResponse;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.http.utils.HttpResponseHelper;
import se.tink.libraries.metrics.MetricRegistry;

@Path("/api/v1/transactions")
public class TransactionServiceResource implements TransactionService, Managed {
    private static final boolean DEBUG = false;

    private final ServiceContext serviceContext;
    private final SystemServiceFactory systemServiceFactory;

    private final AccountRepository accountRepository;
    private final CredentialsRepository credentialsRepository;
    private final ProviderRepository providerRepository;
    private final TransactionDao transactionDao;

    private final TransactionServiceController transactionServiceController;

    private final ClusterCategories categories;
    private static final LogUtils log = new LogUtils(TransactionServiceResource.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private final ShadowCategorizersFactoryCreator shadowCategorizersFactoryCreator;

    public TransactionServiceResource(ServiceContext context, FirehoseQueueProducer firehoseQueueProducer,
            MetricRegistry metricRegistry, ClusterCategories categories,
            ShadowCategorizersFactoryCreator shadowCategorizersFactoryCreator,
            ElasticSearchClient elasticSearchClient) {
        this.serviceContext = context;
        this.systemServiceFactory = serviceContext.getSystemServiceFactory();

        this.accountRepository = context.getRepository(AccountRepository.class);
        this.credentialsRepository = context.getRepository(CredentialsRepository.class);
        this.providerRepository = context.getRepository(ProviderRepository.class);
        this.transactionDao = context.getDao(TransactionDao.class);

        CategoryRepository categoryRepository = context.getRepository(CategoryRepository.class);
        this.categories = categories;

        this.shadowCategorizersFactoryCreator = shadowCategorizersFactoryCreator;
        this.transactionServiceController = new TransactionServiceController(
                new CategoryChangeRecordDao(context.getRepository(CategoryChangeRecordRepository.class),
                        metricRegistry),
                categoryRepository, transactionDao, systemServiceFactory,
                firehoseQueueProducer,
                new SuggestTransactionsSearcher(
                        context, new CurrenciesByCodeProvider(context.getRepository(CurrencyRepository.class)).get(),
                        metricRegistry), elasticSearchClient,
                new TransactionCleaner(categoryRepository),
                context.getCacheClient(), metricRegistry, serviceContext.getExecutorService(),
                categoryRepository.findByCode(serviceContext.getCategoryConfiguration().getExcludeCode()),
                credentialsRepository, providerRepository,
                context.getAggregationControllerCommonClient(),
                DefaultCategorizerFactoryCreator
                        .fromServiceContext(serviceContext, metricRegistry, categories, elasticSearchClient),
                shadowCategorizersFactoryCreator, context.getConfiguration().getCluster(), categories,
                serviceContext.getConfiguration().getCategorization(),
                serviceContext.isProvidersOnAggregation());
    }

    @Override
    @Timed
    public void categorize(User user, CategorizeTransactionsListRequest categorizeTransactionsListRequest) {
        categorize(user, categorizeTransactionsListRequest.getCategorizationList());
    }

    @Override
    @Timed
    public void categorize(User user, List<CategorizeTransactionsRequest> categorizeTransactionsRequests) {
        transactionServiceController.categorize(user, categorizeTransactionsRequests);
    }

    @Override
    @Timed
    public Transaction getTransaction(User user, String id) {
        try {
            return transactionServiceController.getTransaction(user, id);
        } catch (NoSuchElementException e) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
    }

    /**
     * List transactions. Deprecated. Use {@link SearchService#searchQuery(User, String, int, int, String, String)} instead.
     */
    @Override
    @Timed
    @Deprecated
    public List<Transaction> list(User user, List<String> categories, List<String> accounts, List<String> periods,
            int offset, int limit, String sort, String order) {
        TransactionQuery query = new TransactionQuery();

        if (categories != null && !categories.isEmpty()) {
            query.setCategories(categories);
        }

        if (accounts != null && !accounts.isEmpty()) {
            query.setAccounts(accounts);
        }

        if (periods != null && !periods.isEmpty()) {
            query.setPeriods(periods);
        }

        query.setOffset(offset);
        query.setLimit(limit);
        query.setSort(TransactionSortTypes.DATE);
        query.setOrder(OrderTypes.DESC);
        query.setResolution(user.getProfile().getPeriodMode());

        if (sort != null) {
            try {
                query.setSort(TransactionSortTypes.valueOf(sort.toUpperCase()));
            } catch (Exception e) {
                log.warn("Could not parse sort: " + sort);
            }
        }

        if (order != null) {
            try {
                query.setOrder(OrderTypes.valueOf(order.toUpperCase()));
            } catch (Exception e) {
                log.warn("Could not parse order: " + order);
            }
        }

        return query(user, query).getTransactions();
    }

    private SearchQuery convertQuery(User user, TransactionQuery tq) {
        SearchQuery sq = new SearchQuery();

        sq.setQueryString("");
        sq.setLimit(tq.getLimit());
        sq.setOffset(tq.getOffset());
        sq.setOrder(tq.getOrder());
        sq.setAccounts(tq.getAccounts());
        sq.setCredentials(tq.getCredentials());
        sq.setCategories(tq.getCategories());
        sq.setTransactionId(tq.getId());
        sq.setIncludeUpcoming(tq.isIncludeUpcoming());

        if (tq.getSort() != null) {
            switch (tq.getSort()) {
            case DATE:
                sq.setSort(SearchSortTypes.DATE);
                break;
            case ACCOUNT:
                sq.setSort(SearchSortTypes.ACCOUNT);
                break;
            case DESCRIPTION:
                sq.setSort(SearchSortTypes.DESCRIPTION);
                break;
            case AMOUNT:
                sq.setSort(SearchSortTypes.AMOUNT);
                break;
            case CATEGORY:
                sq.setSort(SearchSortTypes.CATEGORY);
                break;
            }
        }

        if (tq.getLimit() == 0) {
            sq.setLimit(Integer.MAX_VALUE);
        }

        if (tq.getPeriods() != null && tq.getPeriods().size() > 0) {
            Date startDate = DateUtils.getFirstDateFromPeriods(tq.getPeriods(), tq.getResolution(),
                    user.getProfile().getPeriodAdjustedDay());

            Date endDate = DateUtils.getLastDateFromPeriods(tq.getPeriods(), tq.getResolution(),
                    user.getProfile().getPeriodAdjustedDay());

            sq.setStartDate(startDate);
            sq.setEndDate(endDate);
        }

        if (tq.getEndDate() != null) {
            sq.setEndDate(tq.getEndDate());

            if (sq.getStartDate() == null) {
                sq.setStartDate(new Date(0));
            }
        }

        return sq;
    }

    @Override
    @Timed
    public TransactionQueryResponse query(User user, TransactionQuery transactionQuery) {
        if (DEBUG) {
            try {
                log.info(user.getId(), "Transaction query: " + mapper.writeValueAsString(transactionQuery));
            } catch (Exception e) {
            }
        }
        TransactionQueryResponse response = new TransactionQueryResponse();

        if (transactionQuery == null) {
            return response;
        }

        if (transactionQuery.getSort() == null) {
            transactionQuery.setSort(TransactionSortTypes.DATE);
        }

        if (transactionQuery.getOrder() == null) {
            transactionQuery.setOrder(OrderTypes.DESC);
        }

        if (transactionQuery.getResolution() == null) {
            transactionQuery.setResolution(user.getProfile().getPeriodMode());
        }

        SearchQuery searchQuery = convertQuery(user, transactionQuery);

        SearchResponse searchResponse = transactionServiceController.searchTransactions(user, searchQuery);

        response.setQuery(transactionQuery);
        response.setCount(searchResponse.getCount());

        response.setTransactions(Lists.newArrayList(Iterables.transform(searchResponse.getResults(),
                SearchResult::getTransaction)));

        if (DEBUG) {
            log.info(user.getId(), "\tReturning " + response.getTransactions().size() + " of " + response.getCount()
                    + " transactions");
        }

        return response;
    }

    @Override
    @Timed
    public SimilarTransactionsResponse similar(User user, String id, String categoryId, boolean includeSelf) {
        List<Transaction> transactions;
        try {
            transactions = transactionServiceController.findSimilarTransactions(user, id, categoryId, includeSelf);
        } catch (NoSuchElementException e) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        List<Statistic> statistics = StatisticsGeneratorAggregator.calculateIncomeAndExpensesAndTransfers(user,
                credentialsRepository.findAllByUserId(user.getId()), accountRepository.findByUserId(user.getId()),
                transactions, categories.get(), true, serviceContext.getCategoryConfiguration());

        SimilarTransactionsResponse response = new SimilarTransactionsResponse();

        response.setTransactions(transactions);
        response.setStatistics(statistics);

        return response;
    }

    @Override
    @Timed
    public SuggestTransactionsResponse suggest(User user, int numberOfClusters, final boolean evaluateEverything) {
        try {
            return transactionServiceController.suggest(user, numberOfClusters, evaluateEverything);
        } catch (Exception e) {
            log.error(user.getId(), "Caught exception while suggesting transactions", e);
            return new SuggestTransactionsResponse();
        }
    }

    @Override
    @Timed
    public void updateTransactions(User user, List<Transaction> transactions) {
        List<Transaction> updatedTransactions = Lists.newArrayList();

        for (Transaction transaction : transactions) {
            updatedTransactions.add(updateTransactions(user.getId(), transaction.getId(), transaction, false, false));
        }

        transactionDao.saveAndIndex(user, updatedTransactions, false);

        transactionServiceController.purgeSuggestFromCache(user, transactions);

        systemServiceFactory.getProcessService()
                .generateStatisticsAndActivitiesWithoutNotifications(user.getId(), StatisticMode.FULL);
    }

    @Override
    @Timed
    public Transaction updateTransaction(User user, String id, Transaction transaction) {
        return updateTransactions(user.getId(), id, transaction, true, true);
    }

    private Transaction updateTransactions(String userId, String id, Transaction transaction,
            boolean generateStatistics,
            boolean saveAndIndex) {
        try {
            return transactionServiceController
                    .updateTransactions(userId, id, transaction, generateStatistics, saveAndIndex);
        } catch (NoSuchElementException e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(Status.BAD_REQUEST);
        }
    }

    @Override
    @Timed
    public Transaction fraudulent(User user, String id, TransactionFraudulentRequest request) {
        Transaction existingTransaction = transactionDao.findOneByUserIdAndId(user.getId(), id, Optional.empty());

        if (existingTransaction == null) {
            HttpResponseHelper.error(Status.NOT_FOUND);
        }

        if (!existingTransaction.getUserId().equals(user.getId())) {
            HttpResponseHelper.error(Status.UNAUTHORIZED);
        }

        if (!id.equals(existingTransaction.getId())) {
            log.warn("Transaction id does not match request body id: " + id + " != " + existingTransaction.getId());
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        if (request.getStatus() == null) {
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        Map<TransactionPayloadTypes, String> payload = existingTransaction.getPayload();

        if (request.getStatus() == TransactionFraudStatus.FRAUDULENT) {
            payload.put(TransactionPayloadTypes.FRAUD_STATUS, TransactionFraudStatus.FRAUDULENT.name());
        } else if (request.getStatus() == TransactionFraudStatus.NOT_FRAUDULENT) {
            payload.remove(TransactionPayloadTypes.FRAUD_STATUS);
        }

        existingTransaction.setPayload(payload);
        transactionDao.saveAndIndex(user, Collections.singletonList(existingTransaction), false);

        return existingTransaction;
    }

    @Override
    @Timed(name = "link-transactions")
    public LinkTransactionsResponse link(User user, String id, String counterpartTransactionId) {

        validateFeatureFlagForSplitTransaction(user.getFlags());

        LinkTransactionsResponse response = new LinkTransactionsResponse();

        try {
            TransactionLink transactions = transactionServiceController
                    .link(user, id, counterpartTransactionId);
            response.setTransaction(transactions.getTransaction());
            response.setCounterpartTransaction(transactions.getCounterpartTransaction());
        } catch (TransactionNotFoundException e) {
            HttpResponseHelper.error(Status.NOT_FOUND);
        } catch (InvalidCandidateException e) {
            HttpResponseHelper.error(Status.PRECONDITION_FAILED);
        }

        return response;
    }

    @Override
    @Timed(name = "categorize-transaction-part")
    public CategorizeTransactionPartResponse categorizePart(User user, String transactionId, String partId,
            CategorizeTransactionPartRequest request) {

        validateFeatureFlagForSplitTransaction(user.getFlags());

        CategorizeTransactionPartResponse response = new CategorizeTransactionPartResponse();

        try {
            response.setTransaction(transactionServiceController
                    .categorizePart(user.getId(), transactionId, partId, request.getCategoryId()));

            systemServiceFactory.getProcessService()
                    .generateStatisticsAndActivitiesWithoutNotifications(user.getId(), StatisticMode.SIMPLE);
        } catch (TransactionNotFoundException e) {
            log.warn(user.getId(), "Unable to find transaction.", e);
            HttpResponseHelper.error(Status.NOT_FOUND);
        } catch (TransactionPartNotFoundException e) {
            log.warn(user.getId(), "Unable to find transaction part.", e);
            HttpResponseHelper.error(Status.NOT_FOUND);
        } catch (InvalidCandidateException e) {
            log.warn(user.getId(), "The transaction part cannot be categorized.", e);
            HttpResponseHelper.error(Status.PRECONDITION_FAILED);
        } catch (CategoryNotFoundException e) {
            log.warn(user.getId(), "Unable to find category.", e);
            HttpResponseHelper.error(Status.BAD_REQUEST);
        } catch (InvalidCategoryException e) {
            log.warn(user.getId(), "The category is not applicable to the transaction part.", e);
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        return response;
    }

    @Override
    @Timed(name = "delete-transaction-part")
    public DeleteTransactionPartResponse deletePart(User user, String transactionId, String partId) {
        DeleteTransactionPartResponse response = new DeleteTransactionPartResponse();

        validateFeatureFlagForSplitTransaction(user.getFlags());

        try {
            TransactionLink result = transactionServiceController.deletePart(user.getId(), transactionId, partId);

            response.setTransaction(result.getTransaction());
            response.setTransaction(result.getCounterpartTransaction());

        } catch (TransactionNotFoundException | TransactionPartNotFoundException e) {
            HttpResponseHelper.error(Status.NOT_FOUND);
        } catch (InvalidCandidateException e) {
            HttpResponseHelper.error(Status.PRECONDITION_FAILED);
        }

        return response;
    }

    @Override
    @Timed(name = "suggest-counterpart-transactions")
    public TransactionLinkSuggestionResponse linkSuggest(User user, String transactionId, int limit) {

        validateFeatureFlagForSplitTransaction(user.getFlags());

        if (limit == 0) {
            limit = 5;
        }

        TransactionLinkSuggestionResponse response = new TransactionLinkSuggestionResponse();
        response.setTransactionId(transactionId);
        response.setLimit(limit);

        try {
            response.setSuggestedCounterpartTransactions(
                    transactionServiceController.linkSuggest(user.getId(), transactionId, limit));
        } catch (TransactionNotFoundException e) {
            log.warn(user.getId(), "Unable to find transaction.", e);
            HttpResponseHelper.error(Status.NOT_FOUND);
        } catch (InvalidSuggestLimitException e) {
            log.warn(user.getId(), "Unable to suggest transactions.", e);
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        return response;
    }

    @Override
    public TransactionLinkPromptResponse linkPrompt(User user, String transactionId,
            TransactionLinkPromptRequest request) {

        validateFeatureFlagForSplitTransaction(user.getFlags());

        TransactionLinkPromptResponse response = new TransactionLinkPromptResponse();

        try {
            response.setTransaction(
                    transactionServiceController.setLinkPromptAnswer(user.getId(), transactionId, request.getAnswer()));
        } catch (TransactionNotFoundException e) {
            log.warn(user.getId(), String.format("[transactionId:%s] Unable to find transaction.", transactionId), e);
            HttpResponseHelper.error(Status.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            log.warn(user.getId(), String.format("[transactionId:%s] Invalid answer.", transactionId), e);
            HttpResponseHelper.error(Status.BAD_REQUEST);
        }

        return response;
    }

    private void validateFeatureFlagForSplitTransaction(List<String> flags) {
        if (!FeatureFlags.FeatureFlagGroup.SPLIT_TRANSACTIONS_FEATURE.isFlagInGroup(flags)) {
            HttpResponseHelper.error(Status.FORBIDDEN);
        }
    }

    @Override
    public void start() throws Exception {
        // Deliberately left empty.
    }

    @Override
    public void stop() throws Exception {
        shadowCategorizersFactoryCreator.close();
    }
}
