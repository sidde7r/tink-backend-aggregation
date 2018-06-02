package se.tink.backend.connector.kirkby;

import com.google.common.collect.Lists;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import se.tink.backend.common.mapper.CoreTransactionMapper;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.tasks.interfaces.TaskSubmitter;
import se.tink.backend.connector.TestBase;
import se.tink.backend.connector.TestUser;
import se.tink.backend.connector.exception.RequestException;
import se.tink.backend.connector.exception.error.RequestError;
import se.tink.backend.connector.rpc.CreateTransactionAccountContainer;
import se.tink.backend.connector.rpc.CreateTransactionAccountEntity;
import se.tink.backend.connector.rpc.CreateTransactionEntity;
import se.tink.backend.connector.transport.ConnectorTransactionServiceJerseyTransport;
import se.tink.backend.connector.util.handler.CredentialsHandler;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.core.TransactionTypes;
import se.tink.backend.core.User;
import se.tink.backend.system.rpc.UpdateAccountRequest;
import se.tink.backend.system.rpc.UpdateTransactionsRequest;
import se.tink.backend.system.tasks.UpdateTransactionsTask;
import se.tink.libraries.serialization.utils.SerializationUtils;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static se.tink.backend.connector.TestBase.createTransaction;
import static se.tink.backend.connector.TestBase.createTransactionAccount;
import static se.tink.backend.connector.TestBase.transactionsEqual;

/**
 * TODO this is a unit test
 */
public class KirkbyConnectorServiceTransactionsTest extends KirkbyConnectorServiceIntegrationTest {

    private ConnectorTransactionServiceJerseyTransport transactionServiceTransport;
    private AccountRepository accountRepository;
    private TestBase testBase;
    private String defaultProviderName;

    @Before
    public void setUp() {
        transactionServiceTransport = injector.getInstance(ConnectorTransactionServiceJerseyTransport.class);

        Mockito.when(injector.getInstance(TaskSubmitter.class).submit(Mockito.any())).thenReturn(
                CompletableFuture.completedFuture(null));
        
        accountRepository = injector.getInstance(AccountRepository.class);
        testBase = injector.getInstance(TestBase.class);

        defaultProviderName = configuration.getConnector().getDefaultProviderName();
    }

    @Test
    public void oneRealTimeContainer_oneAccount_oneTransaction_verifyCorrectTransaction()
            throws InterruptedException, RequestException {

        User user = createUserAndSaveToDB();
        Credentials credentials = createCredentialsAndSaveToDB(user, defaultProviderName);

        TestUser testUser = new TestUser(user, credentials, testBase);
        testUser.addAccountAndSaveToDB(2000d, "account1", "accountNr1", accountRepository);

        CreateTransactionEntity realTimeTransaction = createTransaction(-100d, TransactionTypes.CREDIT_CARD, "t1");

        testUser.addRealTimeAccountTransactionEntity(
                createTransactionAccount("account1", 1900d, realTimeTransaction, null));
        CreateTransactionAccountContainer transactionAccountContainer = testUser.getAccountTransactionEntities().get(0);

        successfulPostStubFor("/update/credentials/update", "/update/accounts/update");

        transactionServiceTransport.ingestTransactions(user.getUsername(), transactionAccountContainer);

        ArgumentCaptor<UpdateTransactionsTask> captor = ArgumentCaptor.forClass(UpdateTransactionsTask.class);
        Mockito.verify(injector.getInstance(TaskSubmitter.class), Mockito.atLeastOnce()).submit(captor.capture());
        UpdateTransactionsRequest updateTransactionsRequest = captor.getValue().getPayload();

        assertEquals(credentials.getId(), updateTransactionsRequest.getCredentials());
        assertEquals(user.getId(), updateTransactionsRequest.getUser());

        List<Transaction> transactions = CoreTransactionMapper.toCoreTransaction(updateTransactionsRequest.getTransactions());
        assertEquals(1, transactions.size());
        assertTrue(transactionsEqual(realTimeTransaction, transactions.get(0)));
    }

    @Test
    public void oneRealTimeContainer_oneAccount_twoTransactions_verifyCorrectTransactions() throws Exception {
        User user = createUserAndSaveToDB();
        Credentials credentials = createCredentialsAndSaveToDB(user, defaultProviderName);

        TestUser testUser = new TestUser(user, credentials, testBase);
        testUser.addAccountAndSaveToDB(2000d, "acc1", "accountNr1", accountRepository);

        CreateTransactionEntity realTimeTransaction1 = createTransaction(-50d, TransactionTypes.DEFAULT, "t1");
        CreateTransactionEntity realTimeTransaction2 = createTransaction(-100d, TransactionTypes.WITHDRAWAL, "t2");
        List<CreateTransactionEntity> realTimeTransactions = Lists.newArrayList(realTimeTransaction1, realTimeTransaction2);

        testUser.addRealTimeAccountTransactionEntity(
                createTransactionAccount("acc1", 1850d, realTimeTransactions, null));

        successfulPostStubFor("/update/credentials/update", "/update/accounts/update");

        transactionServiceTransport
                .ingestTransactions(user.getUsername(), testUser.getAccountTransactionEntities().get(0));

        ArgumentCaptor<UpdateTransactionsTask> captor = ArgumentCaptor.forClass(UpdateTransactionsTask.class);
        Mockito.verify(injector.getInstance(TaskSubmitter.class), Mockito.atLeastOnce()).submit(captor.capture());
        UpdateTransactionsRequest updateTransactionsRequest = captor.getValue().getPayload();

        assertEquals(credentials.getId(), updateTransactionsRequest.getCredentials());
        assertEquals(user.getId(), updateTransactionsRequest.getUser());

        List<Transaction> transactions = CoreTransactionMapper.toCoreTransaction(updateTransactionsRequest.getTransactions());
        assertEquals(2, transactions.size());

        transactions.sort(Comparator
                .comparing(t -> t.getPayload().get(TransactionPayloadTypes.EXTERNAL_ID) + t.getAccountId()));
        realTimeTransactions.sort(Comparator.comparing(CreateTransactionEntity::getExternalId));
        assertTrue(transactionsEqual(realTimeTransactions.get(0), transactions.get(0)));
        assertTrue(transactionsEqual(realTimeTransactions.get(1), transactions.get(1)));
    }

    @Test
    public void oneRealTimeContainer_twoAccounts_twoTransactions_verifyCorrectTransactions() throws Exception {
        User user = createUserAndSaveToDB();
        Credentials credentials = createCredentialsAndSaveToDB(user, defaultProviderName);

        TestUser testUser = new TestUser(user, credentials, testBase);
        testUser.addAccountAndSaveToDB(4000d, "acc1", "accountNr1", accountRepository);
        testUser.addAccountAndSaveToDB(5000d, "acc2", "accountNr2", accountRepository);

        CreateTransactionEntity realTimeTransaction1 = createTransaction(-100d, TransactionTypes.DEFAULT, "t1");
        CreateTransactionEntity realTimeTransaction2 = createTransaction(-200d, TransactionTypes.WITHDRAWAL, "t2");
        List<CreateTransactionEntity> realTimeTransactions = Lists.newArrayList(realTimeTransaction1, realTimeTransaction2);

        testUser.addRealTimeAccountTransactionEntity(
                createTransactionAccount("acc1", 3900d, realTimeTransaction1, null),
                createTransactionAccount("acc2", 4800d, realTimeTransaction2, null));

        successfulPostStubFor("/update/credentials/update", "/update/accounts/update");

        transactionServiceTransport
                .ingestTransactions(user.getUsername(), testUser.getAccountTransactionEntities().get(0));

        ArgumentCaptor<UpdateTransactionsTask> captor = ArgumentCaptor.forClass(UpdateTransactionsTask.class);
        Mockito.verify(injector.getInstance(TaskSubmitter.class), Mockito.atLeastOnce()).submit(captor.capture());
        UpdateTransactionsRequest updateTransactionsRequest = captor.getValue().getPayload();

        assertEquals(credentials.getId(), updateTransactionsRequest.getCredentials());
        assertEquals(user.getId(), updateTransactionsRequest.getUser());

        List<Transaction> transactions = CoreTransactionMapper.toCoreTransaction(updateTransactionsRequest.getTransactions());
        assertEquals(2, transactions.size());

        transactions.sort(Comparator
                .comparing(t -> t.getPayload().get(TransactionPayloadTypes.EXTERNAL_ID) + t.getAccountId()));
        realTimeTransactions.sort(Comparator.comparing(CreateTransactionEntity::getExternalId));
        assertTrue(transactionsEqual(realTimeTransactions.get(0), transactions.get(0)));
        assertTrue(transactionsEqual(realTimeTransactions.get(1), transactions.get(1)));
    }

    @Test
    public void oneRealTimeContainer_nonExistingAccount_oneTransaction_verifyError() throws RequestException {
        User user = createUserAndSaveToDB();
        Credentials credentials = createCredentialsAndSaveToDB(user, defaultProviderName);

        TestUser testUser = new TestUser(user, credentials, testBase);

        // This does not save the account to the database.
        testUser.addAccount(2000d, "acc1", "accountNr1");

        CreateTransactionEntity realTimeTransaction = createTransaction(-200d, TransactionTypes.DEFAULT, "t1");

        testUser.addRealTimeAccountTransactionEntity(
                createTransactionAccount("acc1", 1850d, realTimeTransaction, null));

        try {
            transactionServiceTransport
                    .ingestTransactions(user.getUsername(), testUser.getAccountTransactionEntities().get(0));
            fail();
        } catch (RequestException e) {
            Assert.assertEquals(RequestError.NO_ACCOUNTS_FOUND, e.getError());
        }
    }

    @Test
    public void oneRealTimeContainer_oneAccount_oneTransaction_nonExistingCredentials_verifyError()
            throws RequestException {
        User user = createUserAndSaveToDB();

        // This does not save the credentials to the database.
        Credentials credentials = injector.getInstance(CredentialsHandler.class).createCredentials(user);

        TestUser testUser = new TestUser(user, credentials, testBase);
        testUser.addAccountAndSaveToDB(2000d, "acc1", "accountNr1", accountRepository);

        CreateTransactionEntity realTimeTransaction1 = createTransaction(-50d, TransactionTypes.DEFAULT, "t1");
        CreateTransactionEntity realTimeTransaction2 = createTransaction(-100d, TransactionTypes.WITHDRAWAL, "t2");
        List<CreateTransactionEntity> realTimeTransactions = Lists.newArrayList(realTimeTransaction1, realTimeTransaction2);

        testUser.addRealTimeAccountTransactionEntity(
                createTransactionAccount("acc1", 1850d, realTimeTransactions, null));

        try {
            transactionServiceTransport
                    .ingestTransactions(user.getUsername(), testUser.getAccountTransactionEntities().get(0));
            fail();
        } catch (RequestException e) {
            Assert.assertEquals(RequestError.CREDENTIALS_NOT_FOUND, e.getError());
        }
    }

    @Test
    public void oneRealTimeContainer_oneAccount_oneTransaction_nonExistingUser_verifyError() throws RequestException {

        // This does not save the user or the credentials to the database.
        User user = getTestUser("test@tink.se");
        Credentials credentials = injector.getInstance(CredentialsHandler.class).createCredentials(user);

        TestUser testUser = new TestUser(user, credentials, testBase);
        testUser.addAccountAndSaveToDB(2000d, "acc1", "accountNr1", accountRepository);

        CreateTransactionEntity realTimeTransaction1 = createTransaction(-50d, TransactionTypes.DEFAULT, "t1");
        CreateTransactionEntity realTimeTransaction2 = createTransaction(-100d, TransactionTypes.WITHDRAWAL, "t2");
        List<CreateTransactionEntity> realTimeTransactions = Lists.newArrayList(realTimeTransaction1, realTimeTransaction2);

        testUser.addRealTimeAccountTransactionEntity(
                createTransactionAccount("acc1", 1850d, realTimeTransactions, null));

        try {
            transactionServiceTransport
                    .ingestTransactions(user.getUsername(), testUser.getAccountTransactionEntities().get(0));
            fail();
        } catch (RequestException e) {
            Assert.assertEquals(RequestError.USER_NOT_FOUND, e.getError());
        }
    }

    @Test
    public void oneRealTimeContainer_twoAccounts_twoTransactions_sameExternalId_verifyNoError() throws Exception {
        User user = createUserAndSaveToDB();
        Credentials credentials = createCredentialsAndSaveToDB(user, defaultProviderName);

        TestUser testUser = new TestUser(user, credentials, testBase);
        testUser.addAccountAndSaveToDB(4000d, "acc1", "accountNr1", accountRepository);
        testUser.addAccountAndSaveToDB(5000d, "acc2", "accountNr2", accountRepository);

        CreateTransactionEntity realTimeTransaction = createTransaction(-100d, TransactionTypes.DEFAULT, "t1");

        // Two identical transactions (even with same externalId) is OK if they're in different accounts. The
        // externalIds are only unique within accounts.
        testUser.addRealTimeAccountTransactionEntity(
                createTransactionAccount("acc1", 3900d, realTimeTransaction, null),
                createTransactionAccount("acc2", 4800d, realTimeTransaction, null));

        successfulPostStubFor("/update/credentials/update", "/update/accounts/update");

        transactionServiceTransport
                .ingestTransactions(user.getUsername(), testUser.getAccountTransactionEntities().get(0));

        ArgumentCaptor<UpdateTransactionsTask> captor = ArgumentCaptor.forClass(UpdateTransactionsTask.class);
        Mockito.verify(injector.getInstance(TaskSubmitter.class), Mockito.atLeastOnce()).submit(captor.capture());
        UpdateTransactionsRequest updateTransactionsRequest = captor.getValue().getPayload();

        assertEquals(credentials.getId(), updateTransactionsRequest.getCredentials());
        assertEquals(user.getId(), updateTransactionsRequest.getUser());

        List<Transaction> transactions = CoreTransactionMapper.toCoreTransaction(updateTransactionsRequest.getTransactions());
        assertEquals(2, transactions.size());

        assertTrue(transactionsEqual(realTimeTransaction, transactions.get(0)));
        assertTrue(transactionsEqual(realTimeTransaction, transactions.get(1)));
    }

    @Test
    public void oneRealTimeContainer_twoAccountsWithDisposableAmountAndBalance_verifyAllBalancesCorrect()
            throws RequestException {
        User user = createUserAndSaveToDB();
        Credentials credentials = createCredentialsAndSaveToDB(user, defaultProviderName);

        TestUser testUser = new TestUser(user, credentials, testBase);
        testUser.addAccountAndSaveToDB(4000d, "acc1", "accountNr1", accountRepository);
        testUser.addAccountAndSaveToDB(5000d, "acc2", "accountNr2", accountRepository);

        CreateTransactionEntity realTimeTransaction = createTransaction(-100d, TransactionTypes.DEFAULT, "t1");

        List<CreateTransactionAccountEntity> transactionAccountEntities = Lists.newArrayList(
                createTransactionAccount("acc1", 3900d, realTimeTransaction, 3800d),
                createTransactionAccount("acc2", 4900d, realTimeTransaction, null));
        testUser.addRealTimeAccountTransactionEntity(transactionAccountEntities);

        successfulPostStubFor("/update/credentials/update", "/update/accounts/update");

        transactionServiceTransport
                .ingestTransactions(user.getUsername(), testUser.getAccountTransactionEntities().get(0));

        ArgumentCaptor<UpdateTransactionsTask> captor = ArgumentCaptor.forClass(UpdateTransactionsTask.class);
        Mockito.verify(injector.getInstance(TaskSubmitter.class), Mockito.atLeastOnce()).submit(captor.capture());
        verify(2, postRequestedFor(urlEqualTo("/update/accounts/update")));

        List<UpdateAccountRequest> requests = findAll(postRequestedFor(urlEqualTo("/update/accounts/update"))).stream()
                .map(r -> SerializationUtils.deserializeFromString(r.getBodyAsString(), UpdateAccountRequest.class))
                .collect(Collectors.toList());

        requests.sort(Comparator.comparing(r -> r.getAccount().getBankId()));
        transactionAccountEntities.sort(Comparator.comparing(CreateTransactionAccountEntity::getExternalId));

        for (int i = 0; i < requests.size(); i++) {
            assertEquals(credentials.getId(), requests.get(i).getCredentialsId());
            assertEquals(user.getId(), requests.get(i).getUser());

            Account account = requests.get(i).getAccount();

            // When reserved amount is set, we should calculate the balance.
            if (transactionAccountEntities.get(i).getReservedAmount() != null) {
                assertEquals(transactionAccountEntities.get(i).getBalance() - transactionAccountEntities.get(i).getReservedAmount(), account.getBalance(), 0);
            } else {
                assertEquals(transactionAccountEntities.get(i).getBalance(), account.getBalance(), 0);
            }
        }
    }
}
