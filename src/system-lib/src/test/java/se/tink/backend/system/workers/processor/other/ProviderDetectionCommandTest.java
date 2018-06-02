package se.tink.backend.system.workers.processor.other;

import com.google.api.client.util.Lists;
import com.google.api.client.util.Maps;
import com.google.inject.Inject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.core.User;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;
import se.tink.backend.system.workers.processor.TransactionProcessorUserData;
import se.tink.backend.util.GuiceRunner;
import se.tink.backend.util.TestUtil;

import java.util.List;
import java.util.Map;

@RunWith(GuiceRunner.class)
public class ProviderDetectionCommandTest {

    /**
     * Testing setting provider if no payload.
     */
    private List<User> users;

    @Inject
    private TestUtil testUtil;

    @Before
    public void setUp(){
        users =  testUtil.getTestUsers("ProviderDetectionCommandTest");
    }

    @Test
    public void testProviderDetectionWithNoPayload() {
        users.forEach(user -> {
            // TODO Fix this test after fixes todo in ProviderDetectionCommand.class

            final String credentialId = "cr1";
            final String accountId = "acc1";

            final TransactionProcessorUserData userData = new TransactionProcessorUserData();

            final Credentials cred1 = new Credentials();
            cred1.setId(credentialId);
            cred1.setProviderName("swedbank-bankid");

            final List<Credentials> credentials = Lists.newArrayList();
            credentials.add(cred1);
            userData.setCredentials(credentials);

            final Account acc1 = new Account();
            acc1.setId(accountId);

            final List<Account> accounts = Lists.newArrayList();
            accounts.add(acc1);
            userData.setAccounts(accounts);

            List<Transaction> transactions = Lists.newArrayList();

            Transaction transaction = testUtil.getNewTransaction(user.getId(), -6000, "swedbank.kort", "02", "26");
            transaction.setUserId(user.getId());
            transaction.setAccountId(accountId);
            transaction.setCredentialsId(credentialId);
            transaction.setUserModifiedLocation(false);
            transaction.setUserModifiedAmount(false);
            transaction.setUserModifiedCategory(false);
            transaction.setUserModifiedDate(false);
            transaction.setUserModifiedDescription(false);

            transactions.add(transaction);

            userData.setInStoreTransactions(transactions);

            final TransactionProcessorContext context = new TransactionProcessorContext(
                    user,
                    testUtil.getProvidersByName(),
                    transactions
            );
            context.setUserData(userData);

            ProviderDetectionCommand command = new ProviderDetectionCommand();
            command.initialize();
            command.execute(transaction);

            String payloadToTest = transaction.getPayloadValue(TransactionPayloadTypes.TRANSFER_PROVIDER);

            Assert.assertEquals("swedbank", payloadToTest);
        });
    }

    /**
     * Test not setting provider if transaction have payload.
     */
    @Test
    public void testProviderDetectionWithPayload() {
        users.forEach(user -> {
            // TODO Fix this test after fixes todo in ProviderDetectionCommand.class

            final String credentialId = "cr1";
            final String accountId = "acc1";

            final TransactionProcessorUserData userData = new TransactionProcessorUserData();

            final Credentials cred1 = new Credentials();
            cred1.setId(credentialId);
            cred1.setProviderName("swedbank-bankid");

            final List<Credentials> credentials = Lists.newArrayList();
            credentials.add(cred1);
            userData.setCredentials(credentials);

            final Account acc1 = new Account();
            acc1.setId(accountId);

            final List<Account> accounts = Lists.newArrayList();
            accounts.add(acc1);
            userData.setAccounts(accounts);

            List<Transaction> transactions = Lists.newArrayList();

            Transaction transaction = testUtil.getNewTransaction(user.getId(), -6000, "swedbank.kort", "02", "26");
            transaction.setUserId(user.getId());
            transaction.setAccountId(accountId);
            transaction.setCredentialsId(credentialId);
            transaction.setUserModifiedLocation(false);
            transaction.setUserModifiedAmount(false);
            transaction.setUserModifiedCategory(false);
            transaction.setUserModifiedDate(false);
            transaction.setUserModifiedDescription(false);

            Map<TransactionPayloadTypes, String> payloads = Maps.newHashMap();
            payloads.put(TransactionPayloadTypes.TRANSFER_ACCOUNT, "7645138ee3ef42dea9e5cc3a9d0ceb52");
            payloads.put(TransactionPayloadTypes.TRANSFER_TWIN, "59a5d2644e6743b0bfa41da15d1e115f");
            transaction.setPayload(payloads);

            transactions.add(transaction);

            userData.setInStoreTransactions(transactions);

            // Make sure we have correct payload before running provider detection command.
            String payload = transaction.getPayloadValue(TransactionPayloadTypes.TRANSFER_ACCOUNT);
            boolean hasPayload = (payload != null && !payload.isEmpty());
            Assert.assertEquals(true, hasPayload);

            final TransactionProcessorContext context = new TransactionProcessorContext(
                    user,
                    testUtil.getProvidersByName(),
                    transactions
            );
            context.setUserData(userData);

            ProviderDetectionCommand command = new ProviderDetectionCommand();
            command.initialize();
            command.execute(transaction);

            String payloadToTest = transaction.getPayloadValue(TransactionPayloadTypes.TRANSFER_PROVIDER);
            Assert.assertEquals(null, payloadToTest);
        });
    }

}
