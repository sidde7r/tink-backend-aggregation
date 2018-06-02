package se.tink.backend.connector.seb;

import com.google.common.collect.Lists;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.ws.rs.WebApplicationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import se.tink.backend.common.mapper.CoreTransactionMapper;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.tasks.interfaces.TaskSubmitter;
import se.tink.backend.connector.api.SEBConnectorService;
import se.tink.backend.connector.resources.SEBConnectorServiceResource;
import se.tink.backend.connector.rpc.TransactionContainerType;
import se.tink.backend.connector.rpc.seb.TransactionAccountContainer;
import se.tink.backend.connector.rpc.seb.TransactionAccountEntity;
import se.tink.backend.connector.rpc.seb.TransactionEntity;
import se.tink.backend.connector.seb.SebConnectorTestBase.TestUser;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.core.TransactionTypes;
import se.tink.backend.core.User;
import se.tink.backend.seb.utils.SEBUtils;
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
import static se.tink.backend.connector.seb.SebConnectorTestBase.createTransaction;
import static se.tink.backend.connector.seb.SebConnectorTestBase.createTransactionAccount;
import static se.tink.backend.connector.seb.SebConnectorTestBase.transactionsEqual;

/**
 * TODO this is a unit test
 */
public class SebConnectorServiceTransactionsTest extends SebConnectorServiceIntegrationTest {

    private SEBConnectorService sebConnectorService;
    private AccountRepository accountRepository;

    @Before
    public void setUp() throws Exception {
        sebConnectorService = injector.getInstance(SEBConnectorServiceResource.class);

        Mockito.when(injector.getInstance(TaskSubmitter.class).submit(Mockito.any())).thenReturn(
                CompletableFuture.completedFuture(null));

        accountRepository = injector.getInstance(AccountRepository.class);
    }

    @Test
    @Ignore
    public void oneRealTimeContainer_oneAccount_oneTransaction_verifyCorrectTransaction() throws InterruptedException {
        User user = createUserAndSaveToDB();
        Credentials credentials = createCredentialsAndSaveToDB(user, SEBUtils.SEB_PROVIDER_NAME);

        TestUser testUser = new TestUser(user, credentials);
        testUser.addAccountAndSaveToDB(2000d, "account1", "accountNr1", accountRepository);

        TransactionEntity realTimeTransaction = createTransaction(-100d, TransactionTypes.CREDIT_CARD, "t1");

        testUser.addRealTimeTransactionAccountContainer(
                createTransactionAccount("account1", 1900d, realTimeTransaction, null));
        TransactionAccountContainer transactionAccountContainer = testUser.getTransactionAccountContainers().get(0);

        successfulPostStubFor("/update/credentials/update", "/update/accounts/update");

        sebConnectorService.transactions(user.getUsername(), transactionAccountContainer);

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
        Credentials credentials = createCredentialsAndSaveToDB(user, SEBUtils.SEB_PROVIDER_NAME);

        TestUser testUser = new TestUser(user, credentials);
        testUser.addAccountAndSaveToDB(2000d, "acc1", "accountNr1", accountRepository);

        TransactionEntity realTimeTransaction1 = createTransaction(-50d, TransactionTypes.DEFAULT, "t1");
        TransactionEntity realTimeTransaction2 = createTransaction(-100d, TransactionTypes.WITHDRAWAL, "t2");
        List<TransactionEntity> realTimeTransactions = Lists.newArrayList(realTimeTransaction1, realTimeTransaction2);

        testUser.addRealTimeTransactionAccountContainer(
                createTransactionAccount("acc1", 1850d, realTimeTransactions, null));

        successfulPostStubFor("/update/credentials/update", "/update/accounts/update");

        sebConnectorService.transactions(user.getUsername(), testUser.getTransactionAccountContainers().get(0));

        ArgumentCaptor<UpdateTransactionsTask> captor = ArgumentCaptor.forClass(UpdateTransactionsTask.class);
        Mockito.verify(injector.getInstance(TaskSubmitter.class), Mockito.atLeastOnce()).submit(captor.capture());
        UpdateTransactionsRequest updateTransactionsRequest = captor.getValue().getPayload();

        assertEquals(credentials.getId(), updateTransactionsRequest.getCredentials());
        assertEquals(user.getId(), updateTransactionsRequest.getUser());

        List<Transaction> transactions = CoreTransactionMapper.toCoreTransaction(updateTransactionsRequest.getTransactions());
        assertEquals(2, transactions.size());

        transactions.sort(Comparator
                .comparing(t -> t.getPayload().get(TransactionPayloadTypes.EXTERNAL_ID) + t.getAccountId()));
        realTimeTransactions.sort(Comparator.comparing(TransactionEntity::getExternalId));
        assertTrue(transactionsEqual(realTimeTransactions.get(0), transactions.get(0)));
        assertTrue(transactionsEqual(realTimeTransactions.get(1), transactions.get(1)));
    }

    @Test
    public void oneRealTimeContainer_twoAccounts_twoTransactions_verifyCorrectTransactions() throws Exception {
        User user = createUserAndSaveToDB();
        Credentials credentials = createCredentialsAndSaveToDB(user, SEBUtils.SEB_PROVIDER_NAME);

        TestUser testUser = new TestUser(user, credentials);
        testUser.addAccountAndSaveToDB(4000d, "acc1", "accountNr1", accountRepository);
        testUser.addAccountAndSaveToDB(5000d, "acc2", "accountNr2", accountRepository);

        TransactionEntity realTimeTransaction1 = createTransaction(-100d, TransactionTypes.DEFAULT, "t1");
        TransactionEntity realTimeTransaction2 = createTransaction(-200d, TransactionTypes.WITHDRAWAL, "t2");
        List<TransactionEntity> realTimeTransactions = Lists.newArrayList(realTimeTransaction1, realTimeTransaction2);

        testUser.addRealTimeTransactionAccountContainer(
                createTransactionAccount("acc1", 3900d, realTimeTransaction1, null),
                createTransactionAccount("acc2", 4800d, realTimeTransaction2, null));

        successfulPostStubFor("/update/credentials/update", "/update/accounts/update");

        sebConnectorService.transactions(user.getUsername(), testUser.getTransactionAccountContainers().get(0));

        ArgumentCaptor<UpdateTransactionsTask> captor = ArgumentCaptor.forClass(UpdateTransactionsTask.class);
        Mockito.verify(injector.getInstance(TaskSubmitter.class), Mockito.atLeastOnce()).submit(captor.capture());
        UpdateTransactionsRequest updateTransactionsRequest = captor.getValue().getPayload();

        assertEquals(credentials.getId(), updateTransactionsRequest.getCredentials());
        assertEquals(user.getId(), updateTransactionsRequest.getUser());

        List<Transaction> transactions = CoreTransactionMapper.toCoreTransaction(updateTransactionsRequest.getTransactions());
        assertEquals(2, transactions.size());

        transactions.sort(Comparator
                .comparing(t -> t.getPayload().get(TransactionPayloadTypes.EXTERNAL_ID) + t.getAccountId()));
        realTimeTransactions.sort(Comparator.comparing(TransactionEntity::getExternalId));
        assertTrue(transactionsEqual(realTimeTransactions.get(0), transactions.get(0)));
        assertTrue(transactionsEqual(realTimeTransactions.get(1), transactions.get(1)));
    }

    @Test
    public void sendingNullContainer_givesResponseError() {
        User user = createUserAndSaveToDB();
        createCredentialsAndSaveToDB(user, SEBUtils.SEB_PROVIDER_NAME);

        try {
            sebConnectorService.transactions(user.getUsername(), null);
            fail();
        } catch (WebApplicationException e) {
            Assert.assertEquals(400, e.getResponse().getStatus());
        }
    }

    @Test
    public void oneRealTimeContainer_nonExistingAccount_oneTransaction_verifyError() {
        User user = createUserAndSaveToDB();
        Credentials credentials = createCredentialsAndSaveToDB(user, SEBUtils.SEB_PROVIDER_NAME);

        TestUser testUser = new TestUser(user, credentials);

        // This does not save the account to the database.
        testUser.addAccount(2000d, "acc1", "accountNr1");

        TransactionEntity realTimeTransaction = createTransaction(-200d, TransactionTypes.DEFAULT, "t1");

        testUser.addRealTimeTransactionAccountContainer(
                createTransactionAccount("acc1", 1850d, realTimeTransaction, null));

        try {
            sebConnectorService.transactions(user.getUsername(), testUser.getTransactionAccountContainers().get(0));
            fail();
        } catch (WebApplicationException e) {
            Assert.assertEquals(400, e.getResponse().getStatus());
        }
    }

    @Test
    public void oneContainer_nullType_verifyError() {
        User user = createUserAndSaveToDB();
        createCredentialsAndSaveToDB(user, SEBUtils.SEB_PROVIDER_NAME);

        TransactionAccountContainer transactionAccountContainer = new TransactionAccountContainer();
        transactionAccountContainer.setTransactionAccounts(Lists.newArrayList(new TransactionAccountEntity()));
        transactionAccountContainer.setType(null);

        try {
            sebConnectorService.transactions(user.getUsername(), transactionAccountContainer);
            fail();
        } catch (WebApplicationException e) {
            Assert.assertEquals(400, e.getResponse().getStatus());
        }
    }

    @Test
    public void oneRealTimeContainer_accountListWithNullElement_verifyError() {
        User user = createUserAndSaveToDB();
        createCredentialsAndSaveToDB(user, SEBUtils.SEB_PROVIDER_NAME);

        TransactionAccountContainer transactionAccountContainer = new TransactionAccountContainer();
        List<TransactionAccountEntity> accounts = Lists.newArrayList();
        accounts.add(null);
        transactionAccountContainer.setTransactionAccounts(accounts);
        transactionAccountContainer.setType(TransactionContainerType.REAL_TIME);

        try {
            sebConnectorService.transactions(user.getUsername(), transactionAccountContainer);
            fail();
        } catch (WebApplicationException e) {
            Assert.assertEquals(400, e.getResponse().getStatus());
        }
    }

    @Test
    public void oneRealTimeContainer_oneAccount_nullTransaction_verifyError() {
        User user = createUserAndSaveToDB();
        Credentials credentials = createCredentialsAndSaveToDB(user, SEBUtils.SEB_PROVIDER_NAME);

        TestUser testUser = new TestUser(user, credentials);

        testUser.addAccountAndSaveToDB(2000d, "acc1", "accountNr1", accountRepository);

        List<TransactionEntity> transactions = Lists.newArrayList();
        transactions.add(null);
        testUser.addRealTimeTransactionAccountContainer(createTransactionAccount("acc1", 1850d, transactions, null));

        try {
            sebConnectorService.transactions(user.getUsername(), testUser.getTransactionAccountContainers().get(0));
            fail();
        } catch (WebApplicationException e) {
            Assert.assertEquals(400, e.getResponse().getStatus());
        }
    }

    @Test
    public void oneRealTimeContainer_oneAccount_oneTransaction_nonExistingCredentials_verifyError() {
        User user = createUserAndSaveToDB();

        // This does not save the credentials to the database.
        Credentials credentials = SEBUtils.createCredentials(user);

        TestUser testUser = new TestUser(user, credentials);
        testUser.addAccountAndSaveToDB(2000d, "acc1", "accountNr1", accountRepository);

        TransactionEntity realTimeTransaction1 = createTransaction(-50d, TransactionTypes.DEFAULT, "t1");
        TransactionEntity realTimeTransaction2 = createTransaction(-100d, TransactionTypes.WITHDRAWAL, "t2");
        List<TransactionEntity> realTimeTransactions = Lists.newArrayList(realTimeTransaction1, realTimeTransaction2);

        testUser.addRealTimeTransactionAccountContainer(
                createTransactionAccount("acc1", 1850d, realTimeTransactions, null));

        try {
            sebConnectorService.transactions(user.getUsername(), testUser.getTransactionAccountContainers().get(0));
            fail();
        } catch (WebApplicationException e) {
            Assert.assertEquals(400, e.getResponse().getStatus());
        }
    }

    @Test
    public void oneRealTimeContainer_oneAccount_oneTransaction_nonExistingUser_verifyError() {

        // This does not save the user or the credentials to the database.
        User user = getTestUser("test@tink.se");
        Credentials credentials = SEBUtils.createCredentials(user);

        TestUser testUser = new TestUser(user, credentials);
        testUser.addAccountAndSaveToDB(2000d, "acc1", "accountNr1", accountRepository);

        TransactionEntity realTimeTransaction1 = createTransaction(-50d, TransactionTypes.DEFAULT, "t1");
        TransactionEntity realTimeTransaction2 = createTransaction(-100d, TransactionTypes.WITHDRAWAL, "t2");
        List<TransactionEntity> realTimeTransactions = Lists.newArrayList(realTimeTransaction1, realTimeTransaction2);

        testUser.addRealTimeTransactionAccountContainer(
                createTransactionAccount("acc1", 1850d, realTimeTransactions, null));

        try {
            sebConnectorService.transactions(user.getUsername(), testUser.getTransactionAccountContainers().get(0));
            fail();
        } catch (WebApplicationException e) {
            Assert.assertEquals(401, e.getResponse().getStatus());
        }
    }

    @Test
    public void oneRealTimeContainer_twoAccounts_twoTransactions_sameExternalId_verifyNoError() throws Exception {
        User user = createUserAndSaveToDB();
        Credentials credentials = createCredentialsAndSaveToDB(user, SEBUtils.SEB_PROVIDER_NAME);

        TestUser testUser = new TestUser(user, credentials);
        testUser.addAccountAndSaveToDB(4000d, "acc1", "accountNr1", accountRepository);
        testUser.addAccountAndSaveToDB(5000d, "acc2", "accountNr2", accountRepository);

        TransactionEntity realTimeTransaction = createTransaction(-100d, TransactionTypes.DEFAULT, "t1");

        // Two identical transactions (even with same externalId) is OK if they're in different accounts. The
        // externalIds are only unique within accounts.
        testUser.addRealTimeTransactionAccountContainer(
                createTransactionAccount("acc1", 3900d, realTimeTransaction, null),
                createTransactionAccount("acc2", 4800d, realTimeTransaction, null));

        successfulPostStubFor("/update/credentials/update", "/update/accounts/update");

        sebConnectorService.transactions(user.getUsername(), testUser.getTransactionAccountContainers().get(0));

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
    public void oneRealTimeContainer_oneAccount_twoIdenticalTransactionsWithSameExternalId_verifyError() {
        User user = createUserAndSaveToDB();
        Credentials credentials = createCredentialsAndSaveToDB(user, SEBUtils.SEB_PROVIDER_NAME);

        TestUser testUser = new TestUser(user, credentials);
        testUser.addAccountAndSaveToDB(5000d, "acc1", "accountNr1", accountRepository);

        // Two identical transactions (with same externalId) is OK if they're in different accounts, since the
        // externalIds are only unique within accounts. If they're in the same account, we should give an error.
        TransactionEntity realTimeTransaction = createTransaction(-1000d, TransactionTypes.DEFAULT, "t1");
        List<TransactionEntity> realTimeTransactions = Lists.newArrayList(realTimeTransaction, realTimeTransaction);

        testUser.addRealTimeTransactionAccountContainer(
                createTransactionAccount("acc1", 4000d, realTimeTransactions, null));

        successfulPostStubFor("/update/credentials/update", "/update/accounts/update");

        try {
            sebConnectorService.transactions(user.getUsername(), testUser.getTransactionAccountContainers().get(0));
            fail();
        } catch (WebApplicationException e) {
            assertEquals(400, e.getResponse().getStatus());
        }
    }

    @Test
    public void oneRealTimeContainer_twoAccountsWithDisposableAmountAndBalance_verifyAllBalancesCorrect() {
        User user = createUserAndSaveToDB();
        Credentials credentials = createCredentialsAndSaveToDB(user, SEBUtils.SEB_PROVIDER_NAME);

        TestUser testUser = new TestUser(user, credentials);
        testUser.addAccountAndSaveToDB(4000d, "acc1", "accountNr1", accountRepository);
        testUser.addAccountAndSaveToDB(5000d, "acc2", "accountNr2", accountRepository);

        TransactionEntity realTimeTransaction = createTransaction(-100d, TransactionTypes.DEFAULT, "t1");

        List<TransactionAccountEntity> transactionAccountEntities = Lists.newArrayList(
                createTransactionAccount("acc1", 3900d, realTimeTransaction, 3800d),
                createTransactionAccount("acc2", 4900d, realTimeTransaction, null));
        testUser.addRealTimeTransactionAccountContainer(transactionAccountEntities);

        successfulPostStubFor("/update/credentials/update", "/update/accounts/update");

        sebConnectorService.transactions(user.getUsername(), testUser.getTransactionAccountContainers().get(0));

        ArgumentCaptor<UpdateTransactionsTask> captor = ArgumentCaptor.forClass(UpdateTransactionsTask.class);
        Mockito.verify(injector.getInstance(TaskSubmitter.class), Mockito.atLeastOnce()).submit(captor.capture());
        verify(2, postRequestedFor(urlEqualTo("/update/accounts/update")));

        List<UpdateAccountRequest> requests = findAll(postRequestedFor(urlEqualTo("/update/accounts/update"))).stream()
                .map(r -> SerializationUtils.deserializeFromString(r.getBodyAsString(), UpdateAccountRequest.class))
                .collect(Collectors.toList());

        requests.sort(Comparator.comparing(r -> r.getAccount().getBankId()));
        transactionAccountEntities.sort(Comparator.comparing(TransactionAccountEntity::getExternalId));

        for (int i = 0; i < requests.size(); i++) {
            assertEquals(credentials.getId(), requests.get(i).getCredentialsId());
            assertEquals(user.getId(), requests.get(i).getUser());

            Account account = requests.get(i).getAccount();

            // When disposable amount is set, we should always use that instead of the balance.
            if (transactionAccountEntities.get(i).getDisposableAmount() != null) {
                assertEquals(transactionAccountEntities.get(i).getDisposableAmount(), account.getBalance(), 0);
            } else {
                assertEquals(transactionAccountEntities.get(i).getBalance(), account.getBalance(), 0);
            }
        }
    }

    @Test
    public void nullBalanceOnAccount_doesNotSilentlyGenerateZeroBalanceButGeneratesError() {
        User user = createUserAndSaveToDB();
        Credentials credentials = createCredentialsAndSaveToDB(user, SEBUtils.SEB_PROVIDER_NAME);

        TestUser testUser = new TestUser(user, credentials);
        testUser.addAccountAndSaveToDB(4000d, "acc1", "accountNr1", accountRepository);
        TransactionEntity realTimeTransaction = createTransaction(-500d, TransactionTypes.PAYMENT, "t1");
        testUser.addRealTimeTransactionAccountContainer(
                createTransactionAccount("acc1", null, realTimeTransaction, null));

        try {
            sebConnectorService.transactions(user.getId(), testUser.getTransactionAccountContainers().get(0));
            fail();
        } catch (WebApplicationException e) {
            Assert.assertEquals(400, e.getResponse().getStatus());
        }
    }

    @Test
    public void nullAmountOnTransaction_doesNotSilentlyGenerateZeroAmountButGeneratesError() {
        User user = createUserAndSaveToDB();
        Credentials credentials = createCredentialsAndSaveToDB(user, SEBUtils.SEB_PROVIDER_NAME);

        TestUser testUser = new TestUser(user, credentials);
        testUser.addAccountAndSaveToDB(4000d, "acc1", "accountNr1", accountRepository);
        TransactionEntity realTimeTransaction = createTransaction(null, TransactionTypes.PAYMENT, "t1");
        testUser.addRealTimeTransactionAccountContainer(
                createTransactionAccount("acc1", 4000d, realTimeTransaction, 3000d));

        try {
            sebConnectorService.transactions(user.getId(), testUser.getTransactionAccountContainers().get(0));
            fail();
        } catch (WebApplicationException e) {
            Assert.assertEquals(400, e.getResponse().getStatus());
        }
    }
}
