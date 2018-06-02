package se.tink.backend.system.workers.processor.other;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.categorization.api.SECategories;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.system.workers.processor.TransactionProcessorCommandResult;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;
import se.tink.backend.system.workers.processor.TransactionProcessorUserData;
import se.tink.backend.util.GuiceRunner;
import se.tink.backend.util.TestProcessor;
import se.tink.backend.util.TestUtil;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(GuiceRunner.class)
public class FilterOrphansCommandTest {

    private final String credentialsId = "credentialsId1";
    private final String accountId = "accountId1";

    @Inject
    private TestUtil testUtil;

    @Test
    public void orphanTransactions_areFilteredOut_fromInBatchTransactions() {

        List<User> users = testUtil.getTestUsers("FilterOrphansCommandTest");
        users.forEach(user -> {
            final TransactionProcessorContext context = new TransactionProcessorContext(user,
                    testUtil.getProvidersByName(), Lists.newArrayList());

            final TransactionProcessorUserData userData = new TransactionProcessorUserData();

            final Credentials cred1 = new Credentials();
            cred1.setId(credentialsId);
            userData.setCredentials(Lists.newArrayList(cred1));

            final Account acc1 = new Account();
            acc1.setId(accountId);
            userData.setAccounts(Lists.newArrayList(acc1));

            context.setUserData(userData);

            final FilterOrphansCommand command = new FilterOrphansCommand(context, context.getUser().getId());

            final Transaction transactionWithUnknownAccount = testUtil.getNewTransaction("", -334, "Random description");
            transactionWithUnknownAccount.setAccountId("unknown");
            transactionWithUnknownAccount.setCredentialsId(credentialsId);
            context.getInBatchTransactions().add(transactionWithUnknownAccount);

            final Transaction transactionWithUnknownCredentials = testUtil.getNewTransaction("", -334, "Random description");
            transactionWithUnknownCredentials.setCredentialsId("unknown");
            transactionWithUnknownCredentials.setAccountId(accountId);
            context.getInBatchTransactions().add(transactionWithUnknownCredentials);

            final Transaction normalTransaction = testUtil.getNewTransaction("", -334, "Random description");
            normalTransaction.setCredentialsId(credentialsId);
            normalTransaction.setAccountId(accountId);
            context.getInBatchTransactions().add(normalTransaction);

            assertEquals(context.getInBatchTransactions().size(), 3);
            command.initialize();
            assertFalse(context.getInBatchTransactions().contains(transactionWithUnknownAccount));
            assertFalse(context.getInBatchTransactions().contains(transactionWithUnknownCredentials));
            assertTrue(context.getInBatchTransactions().contains(normalTransaction));

            assertEquals(command.execute(transactionWithUnknownAccount), TransactionProcessorCommandResult.CONTINUE);
            assertFalse(context.getInBatchTransactions().contains(transactionWithUnknownAccount));

            assertEquals(command.execute(transactionWithUnknownCredentials), TransactionProcessorCommandResult.CONTINUE);
            assertFalse(context.getInBatchTransactions().contains(transactionWithUnknownCredentials));

            assertEquals(command.execute(normalTransaction), TransactionProcessorCommandResult.CONTINUE);
            assertTrue(context.getInBatchTransactions().contains(normalTransaction));

        });
    }

    @Test
    public void nullAccountsOnUserData_doesNotImplyOrphanTransactions() {
        List<User> users = testUtil.getTestUsers("Random name");
        users.forEach(user -> {
            final TransactionProcessorContext context = new TransactionProcessorContext(user,
                    testUtil.getProvidersByName(), Lists.newArrayList());

            final TransactionProcessorUserData userData = new TransactionProcessorUserData();

            final Credentials cred1 = new Credentials();
            cred1.setId(credentialsId);
            userData.setCredentials(Lists.newArrayList(cred1));

            // Setting account list to null.
            userData.setAccounts(null);

            context.setUserData(userData);

            final FilterOrphansCommand command = new FilterOrphansCommand(context, context.getUser().getId());

            final Transaction normalTransaction = testUtil.getNewTransaction("testId", -334, "Random description");
            normalTransaction.setCredentialsId(credentialsId);
            normalTransaction.setAccountId(accountId);
            context.getInBatchTransactions().add(normalTransaction);

            // The non-orphan transaction is not filtered out since accounts == null.
            assertEquals(context.getInBatchTransactions().size(), 1);
            command.initialize();
            assertEquals(context.getInBatchTransactions().size(), 1);

            // Nothing happens in execute().
            command.execute(normalTransaction);
            assertEquals(context.getInBatchTransactions().size(), 1);
        });
    }

    @Test
    public void emptyAccountListOnUserData_meansOrphanTransactions() {
        List<User> users = testUtil.getTestUsers("Random name");
        users.forEach(user -> {
            final TransactionProcessorContext context = new TransactionProcessorContext(user,
                    testUtil.getProvidersByName(), Lists.newArrayList());

            final TransactionProcessorUserData userData = new TransactionProcessorUserData();

            final Credentials cred1 = new Credentials();
            cred1.setId(credentialsId);
            userData.setCredentials(Lists.newArrayList(cred1));

            // Setting accounts to an empty list.
            userData.setAccounts(Lists.newArrayList());

            context.setUserData(userData);

            final FilterOrphansCommand command = new FilterOrphansCommand(context, context.getUser().getId());

            final Transaction normalTransaction = testUtil.getNewTransaction("testId", -334, "Random description");
            normalTransaction.setCredentialsId(credentialsId);
            normalTransaction.setAccountId(accountId);
            context.getInBatchTransactions().add(normalTransaction);

            // The non-orphan transaction is filtered out since the account list is empty.
            assertEquals(context.getInBatchTransactions().size(), 1);
            command.initialize();
            assertEquals(context.getInBatchTransactions().size(), 0);

            // Nothing happens in execute().
            command.execute(normalTransaction);
            assertEquals(context.getInBatchTransactions().size(), 0);
        });
    }

    @Test
    public void nullCredentialsOnUserData_doesNotImplyOrphanTransactions() {
        List<User> users = testUtil.getTestUsers("Random name");
        users.forEach(user -> {
            final TransactionProcessorContext context = new TransactionProcessorContext(user,
                    testUtil.getProvidersByName(), Lists.newArrayList());

            final TransactionProcessorUserData userData = new TransactionProcessorUserData();

            final Account acc1 = new Account();
            acc1.setId(accountId);
            userData.setAccounts(Lists.newArrayList(acc1));

            // Setting credentials list to null.
            userData.setCredentials(null);

            context.setUserData(userData);

            final FilterOrphansCommand command = new FilterOrphansCommand(context, context.getUser().getId());

            final Transaction normalTransaction = testUtil.getNewTransaction("testId", -334, "Random description");
            normalTransaction.setCredentialsId(credentialsId);
            normalTransaction.setAccountId(accountId);
            context.getInBatchTransactions().add(normalTransaction);

            // The non-orphan transaction is not filtered out since credentials == null.
            assertEquals(context.getInBatchTransactions().size(), 1);
            command.initialize();
            assertEquals(context.getInBatchTransactions().size(), 1);

            // Nothing happens in execute().
            command.execute(normalTransaction);
            assertEquals(context.getInBatchTransactions().size(), 1);
        });
    }

    @Test
    public void emptyCredentialsListOnUserData_meansOrphanTransactions() {
        List<User> users = testUtil.getTestUsers("Random name");
        users.forEach(user -> {
            final TransactionProcessorContext context = new TransactionProcessorContext(user,
                    testUtil.getProvidersByName(), Lists.newArrayList());

            final TransactionProcessorUserData userData = new TransactionProcessorUserData();

            final Account acc1 = new Account();
            acc1.setId(accountId);
            userData.setAccounts(Lists.newArrayList(acc1));

            // Setting credentials to an empty list.
            userData.setCredentials(Lists.newArrayList());

            context.setUserData(userData);

            final FilterOrphansCommand command = new FilterOrphansCommand(context, context.getUser().getId());

            final Transaction normalTransaction = testUtil.getNewTransaction("testId", -334, "Random description");
            normalTransaction.setCredentialsId(credentialsId);
            normalTransaction.setAccountId(accountId);
            context.getInBatchTransactions().add(normalTransaction);

            // The non-orphan transaction is filtered out since the credentials list is empty.
            assertEquals(context.getInBatchTransactions().size(), 1);
            command.initialize();
            assertEquals(context.getInBatchTransactions().size(), 0);

            // Nothing happens in execute().
            command.execute(normalTransaction);
            assertEquals(context.getInBatchTransactions().size(), 0);
        });
    }
}
