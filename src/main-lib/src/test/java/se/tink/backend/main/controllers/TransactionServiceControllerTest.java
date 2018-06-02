package se.tink.backend.main.controllers;

import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import org.elasticsearch.client.Client;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.tink.backend.categorization.factory.DefaultCategorizerFactoryCreator;
import se.tink.backend.categorization.factory.ShadowCategorizersFactoryCreator;
import se.tink.backend.categorization.interfaces.CategorizerFactory;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.backend.common.concurrency.ListenableThreadPoolExecutor;
import se.tink.backend.common.config.CategorizationConfiguration;
import se.tink.backend.common.dao.CategoryChangeRecordDao;
import se.tink.backend.common.dao.transactions.TransactionCleaner;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.common.search.SuggestTransactionsSearcher;
import se.tink.backend.common.search.client.ElasticSearchClient;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.ClusterCategories;
import se.tink.backend.core.Provider;
import se.tink.backend.core.StatisticMode;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.core.TransactionTypes;
import se.tink.backend.firehose.v1.queue.FirehoseQueueProducer;
import se.tink.backend.rpc.UpdateTransactionRequest;
import se.tink.backend.system.api.ProcessService;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.metrics.MetricRegistry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(JUnitParamsRunner.class)
public class TransactionServiceControllerTest {
    private TransactionServiceController transactionServiceController;
    @Mock private CategoryChangeRecordDao categoryChangeRecordsDao;
    @Mock private TransactionDao transactionDao;
    @Mock private SystemServiceFactory systemServiceFactory;
    @Mock private TransactionCleaner transactionCleaner;
    @Mock private CacheClient cacheClient;
    @Mock private ProcessService processService;
    @Mock private FirehoseQueueProducer firehoseQueueProducer;
    @Mock private Client searchClient;
    @Mock private Category excludedCategory;
    @Mock private DefaultCategorizerFactoryCreator categorizerFactoryCreator;
    @Mock private ShadowCategorizersFactoryCreator shadowCategorizersFactoryCreator;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        CategoryRepository categoryRepository = mock(CategoryRepository.class);
        SuggestTransactionsSearcher suggestSearcher = mock(SuggestTransactionsSearcher.class);
        MetricRegistry metricRegistry = new MetricRegistry();
        ListenableThreadPoolExecutor<Runnable> executor = mock(ListenableThreadPoolExecutor.class);
        CredentialsRepository credentialsRepository = mock(CredentialsRepository.class);
        ProviderRepository providerRepository = mock(ProviderRepository.class);
        AggregationControllerCommonClient aggregationControllerClient = mock(AggregationControllerCommonClient.class);
        Provider provider = mock(Provider.class);
        ClusterCategories clusterCategories = mock(ClusterCategories.class);
        ElasticSearchClient elasticSearchClient = mock(ElasticSearchClient.class);
        CategorizationConfiguration categorizationConfiguration = new CategorizationConfiguration();
        Cluster cluster = Cluster.TINK;

        when(categorizerFactoryCreator.build()).thenReturn(mock(CategorizerFactory.class));
        when(shadowCategorizersFactoryCreator.build()).thenReturn(Collections.EMPTY_LIST);
        when(provider.getMarket()).thenReturn("");
        when(aggregationControllerClient.listProviders()).thenReturn(Collections.singletonList(provider));
        when(providerRepository.findAll()).thenReturn(Collections.singletonList(provider));

        transactionServiceController = new TransactionServiceController(categoryChangeRecordsDao, categoryRepository,
                transactionDao, systemServiceFactory, firehoseQueueProducer, suggestSearcher,
                elasticSearchClient, transactionCleaner, cacheClient, metricRegistry, executor,
                excludedCategory, credentialsRepository, providerRepository, aggregationControllerClient,
                categorizerFactoryCreator, shadowCategorizersFactoryCreator, cluster, clusterCategories,
                categorizationConfiguration, false);
    }

    @Test(expected = NoSuchElementException.class)
    public void throwExceptionOnNonExistedTransaction() {
        transactionServiceController
                .updateTransactions("userId", "unexistedTransactionId", new Transaction(), false, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwExceptionOnUnequalTransactionId() {
        Transaction incorrectTransaction = new Transaction();
        incorrectTransaction.setUserId("userId");
        incorrectTransaction.setId("anotherTransactionId");
        when(transactionDao.findOneByUserIdAndId(eq("userId"), eq("transactionId"), eq(Optional.empty())))
                .thenReturn(incorrectTransaction);

        transactionServiceController
                .updateTransactions("userId", "transactionId", new Transaction(), false, false);
    }

    @Test
    public void updateOnlyModifiableFields() {
        Date now = DateUtils.flattenTime(new Date());
        Map<TransactionPayloadTypes, String> payload = Maps.newHashMap();
        payload.put(TransactionPayloadTypes.MESSAGE, "message");

        Transaction oldTransaction = createFullTransaction(StringUtils.generateUUID(), StringUtils.generateUUID(),
                TransactionTypes.PAYMENT, "accountId", CategoryTypes.EXPENSES, -1000, -1001, now,
                DateUtils.addDays(now, -1), "description", "originalDescription", "notes", payload, true, true);

        when(transactionDao.findOneByUserIdAndId(eq(oldTransaction.getUserId()), eq(oldTransaction.getId()),
                any(Optional.class)))
                .thenReturn(oldTransaction.clone());
        when(systemServiceFactory.getProcessService()).thenReturn(processService);

        Map<TransactionPayloadTypes, String> newPayload = Maps.newHashMap();
        newPayload.put(TransactionPayloadTypes.EDITABLE_TRANSACTION_TRANSFER_ID, "transferId");

        Transaction newTransaction = createFullTransaction(StringUtils.generateUUID(), StringUtils.generateUUID(),
                TransactionTypes.CREDIT_CARD, "newAccountId", CategoryTypes.TRANSFERS, -2000, -2001,
                DateUtils.addDays(now, -2), DateUtils.addDays(now, -3), "newDescription", "newOriginalDescription",
                "newNotes", newPayload, false, false);

        Transaction updatedTransaction = transactionServiceController
                .updateTransactions(oldTransaction.getUserId(), oldTransaction.getId(), newTransaction.clone(), true,
                        true);

        // Change only @Modifiable fields
        Transaction expectedTransaction = oldTransaction.clone();
        expectedTransaction.setAmount(newTransaction.getAmount());
        expectedTransaction.setUserModifiedAmount(true);
        expectedTransaction.setCategory(newTransaction.getCategoryId(), oldTransaction.getCategoryType());
        expectedTransaction.setUserModifiedCategory(true);
        expectedTransaction.setDate(newTransaction.getDate());
        expectedTransaction.setUserModifiedDate(true);
        expectedTransaction.setDescription(newTransaction.getDescription());
        expectedTransaction.setUserModifiedDescription(true);
        expectedTransaction.setNotes(newTransaction.getNotes());

        assertThat(oldTransaction.getAmount()).isNotEqualTo(expectedTransaction.getAmount());
        assertThat(updatedTransaction).isEqualToComparingFieldByField(expectedTransaction);
        verify(transactionDao)
                .saveAndIndex(eq(updatedTransaction.getUserId()), eq(Collections.singletonList(updatedTransaction)),
                        eq(true));
        verify(processService)
                .generateStatisticsAndActivitiesWithoutNotifications(oldTransaction.getUserId(), StatisticMode.FULL);
    }

    @Test
    public void updateOnlyRequestedFields() {
        Date now = DateUtils.flattenTime(new Date());
        Map<TransactionPayloadTypes, String> payload = Maps.newHashMap();
        payload.put(TransactionPayloadTypes.MESSAGE, "message");

        Transaction oldTransaction = createFullTransaction(StringUtils.generateUUID(), StringUtils.generateUUID(),
                TransactionTypes.PAYMENT, "accountId", CategoryTypes.EXPENSES, -1000, -1001, now,
                DateUtils.addDays(now, -1), "description", "originalDescription", "notes", payload, true, true);

        when(transactionDao.findOneByUserIdAndId(eq(oldTransaction.getUserId()), eq(oldTransaction.getId()),
                any(Optional.class)))
                .thenReturn(oldTransaction.clone());
        when(systemServiceFactory.getProcessService()).thenReturn(processService);

        UpdateTransactionRequest updateRequest = new UpdateTransactionRequest();
        updateRequest.setAmount(-2000D);
        updateRequest.setNotes("newNotes");
        updateRequest.setDate(now);

        Transaction updatedTransaction = transactionServiceController
                .updateTransactions(oldTransaction.getUserId(), oldTransaction.getId(), updateRequest, true,
                        true);

        Transaction expectedTransaction = oldTransaction.clone();
        expectedTransaction.setAmount(-2000.);
        expectedTransaction.setUserModifiedAmount(true);
        expectedTransaction.setNotes("newNotes");
        expectedTransaction.setDate(now);
        expectedTransaction.setUserModifiedDate(false);

        assertThat(oldTransaction.getAmount()).isNotEqualTo(expectedTransaction.getAmount());
        assertThat(updatedTransaction).isEqualToComparingFieldByField(expectedTransaction);
        verify(transactionDao)
                .saveAndIndex(eq(updatedTransaction.getUserId()), eq(Collections.singletonList(updatedTransaction)),
                        eq(true));
        verify(processService)
                .generateStatisticsAndActivitiesWithoutNotifications(oldTransaction.getUserId(), StatisticMode.FULL);
    }

    @Test
    public void doNotChangeFuzzyEqualAmount() {
        Date now = DateUtils.flattenTime(new Date());
        Map<TransactionPayloadTypes, String> payload = Maps.newHashMap();
        payload.put(TransactionPayloadTypes.MESSAGE, "message");

        Transaction oldTransaction = createFullTransaction(StringUtils.generateUUID(), StringUtils.generateUUID(),
                TransactionTypes.PAYMENT, "accountId", CategoryTypes.EXPENSES, -1000, -1001, now,
                DateUtils.addDays(now, -1), "description", "originalDescription", "notes", payload, true, true);

        when(transactionDao.findOneByUserIdAndId(eq(oldTransaction.getUserId()), eq(oldTransaction.getId()),
                eq(Optional.empty())))
                .thenReturn(oldTransaction.clone());
        when(systemServiceFactory.getProcessService()).thenReturn(processService);

        UpdateTransactionRequest updateRequest = new UpdateTransactionRequest();
        updateRequest.setAmount(oldTransaction.getAmount() + 0.00001);

        Transaction updatedTransaction = transactionServiceController
                .updateTransactions(oldTransaction.getUserId(), oldTransaction.getId(), updateRequest, true,
                        true);

        Transaction expectedTransaction = oldTransaction.clone();
        expectedTransaction.setUserModifiedAmount(false);

        assertThat(updatedTransaction).isEqualToComparingFieldByField(expectedTransaction);
        verify(transactionDao)
                .saveAndIndex(eq(updatedTransaction.getUserId()), eq(Collections.singletonList(updatedTransaction)),
                        eq(true));
        verify(processService)
                .generateStatisticsAndActivitiesWithoutNotifications(oldTransaction.getUserId(), StatisticMode.FULL);
    }

    public static Transaction createFullTransaction(String id, String userId, TransactionTypes type, String accountId,
            CategoryTypes categoryType, double amount, double originalAmount, Date date,
            Date originalDate, String description,
            String originalDescription, String notes, Map<TransactionPayloadTypes, String> payload, boolean pending,
            boolean upcoming) {
        Transaction transaction = new Transaction();
        transaction.setId(id);
        transaction.setUserId(userId);
        transaction.setType(type);
        transaction.setAccountId(accountId);
        transaction.setAmount(amount);
        transaction.setOriginalAmount(originalAmount);
        transaction.setDate(date);
        transaction.setOriginalDate(originalDate);
        transaction.setDescription(description);
        transaction.setOriginalDescription(originalDescription);
        transaction.setNotes(notes);

        if (payload != null) {
            transaction.setPayload(payload);
        }

        transaction.setCategory(StringUtils.generateUUID(), categoryType);
        transaction.setPending(pending);
        transaction.setUpcoming(upcoming);

        return transaction;
    }

}
