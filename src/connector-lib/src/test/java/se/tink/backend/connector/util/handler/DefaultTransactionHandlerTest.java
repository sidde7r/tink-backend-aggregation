package se.tink.backend.connector.util.handler;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.common.config.TasksQueueConfiguration;
import se.tink.backend.common.tasks.interfaces.TaskSubmitter;
import se.tink.backend.connector.rpc.CreateTransactionEntity;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionTypes;
import se.tink.backend.core.User;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.libraries.metrics.MetricRegistry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultTransactionHandlerTest {
    private DefaultTransactionHandler transactionHandler;
    private User user;
    private Credentials credentials;
    private Account account;

    private static final double SAMPLE_AMOUNT_A = -300.5;
    private static final double SAMPLE_AMOUNT_B = 50;
    private static final double DELTA_TOLERANCE = 0.01;

    @Before
    public void setUp() {
        TasksQueueConfiguration tasksQueueConfiguration = new TasksQueueConfiguration();
        TaskSubmitter taskSubmitter = mock(TaskSubmitter.class);
        SystemServiceFactory systemServiceFactory = mock(SystemServiceFactory.class);
        MetricRegistry metricRegistry = mock(MetricRegistry.class);

        transactionHandler = new DefaultTransactionHandler(tasksQueueConfiguration, taskSubmitter, systemServiceFactory, metricRegistry);
        user = mock(User.class);
        credentials = mock(Credentials.class);
        account = mock(Account.class);
    }

    private CreateTransactionEntity dummyTransactionEntity() {
        CreateTransactionEntity transactionEntity = mock(CreateTransactionEntity.class);

        when(transactionEntity.getAmount()).thenReturn(SAMPLE_AMOUNT_A);
        when(transactionEntity.getDate()).thenReturn(new Date(1000000000));
        when(transactionEntity.getEntityCreated()).thenReturn(new Date(1000000000));
        when(transactionEntity.getType()).thenReturn(TransactionTypes.TRAINING);
        when(transactionEntity.isPending()).thenReturn(false);
        when(transactionEntity.getPayload()).thenCallRealMethod();
        when(transactionEntity.getExternalId()).thenReturn("ExampleOrg");

        return transactionEntity;
    }

    @Test
    public void mapToTinkModel() throws Exception {
        CreateTransactionEntity transactionEntity = dummyTransactionEntity();
        Transaction transaction = transactionHandler.mapToTinkModel(user, credentials, account, transactionEntity);

        Assert.assertEquals("Transaction amount not equal", transactionEntity.getAmount(), transaction.getAmount(),
        DELTA_TOLERANCE);
    }

    @Test
    public void mapUpdateToTinkModel_updateTransactionAmount() throws Exception {
        CreateTransactionEntity oldTransaction = dummyTransactionEntity();
        CreateTransactionEntity newTransaction = dummyTransactionEntity();
        when(newTransaction.getAmount()).thenReturn(-300.0);

        Transaction transaction = transactionHandler.mapToTinkModel(user, credentials, account, oldTransaction);
        transaction = transactionHandler.mapUpdateToTinkModel(transaction, newTransaction);
        Assert.assertEquals("Transaction amount not updated", newTransaction.getAmount(), transaction.getAmount(),
                DELTA_TOLERANCE);
    }

    @Test
    public void mapUpdateToTinkModel_userModifiedValues() throws Exception {
        Transaction transaction = new Transaction();
        transaction.setAmount(SAMPLE_AMOUNT_B);
        transaction.setOriginalAmount(SAMPLE_AMOUNT_B);
        transaction.setUserModifiedAmount(true);
        CreateTransactionEntity update = dummyTransactionEntity();

        transaction = transactionHandler.mapUpdateToTinkModel(transaction, update);
        // Expected: User modified value not changed and "original" value changed to new value
        Assert.assertEquals(SAMPLE_AMOUNT_B, transaction.getAmount(), DELTA_TOLERANCE);
        Assert.assertNotEquals(transaction.getAmount(), transaction.getOriginalAmount(), DELTA_TOLERANCE);
        Assert.assertEquals(update.getAmount(), transaction.getOriginalAmount(), DELTA_TOLERANCE);
    }

    @Test
    public void mapUpdateToTinkModel_nonUserModifiedValues() throws Exception {
        Transaction transaction = new Transaction();
        transaction.setAmount(SAMPLE_AMOUNT_B);
        transaction.setOriginalAmount(SAMPLE_AMOUNT_B);
        CreateTransactionEntity update = dummyTransactionEntity();

        transaction = transactionHandler.mapUpdateToTinkModel(transaction, update);
        Assert.assertEquals(transaction.getAmount(), transaction.getOriginalAmount(), DELTA_TOLERANCE);
        Assert.assertNotEquals(SAMPLE_AMOUNT_B, transaction.getAmount(), DELTA_TOLERANCE);
    }

    @Test
    public void tagsPayloadToNotes() throws Exception {
        CreateTransactionEntity transactionEntity = dummyTransactionEntity();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("TAGS", Lists.newArrayList("#foo"));
        when(transactionEntity.getPayload()).thenReturn(map);

        Transaction transaction = transactionHandler.mapToTinkModel(user, credentials, account, transactionEntity);

        Assert.assertEquals("Tags are not transferred from payload", "#foo", transaction.getNotes());
    }
}
