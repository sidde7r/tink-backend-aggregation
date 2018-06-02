package se.tink.backend.system.workers.processor.other;

import com.google.api.client.util.Lists;

import java.util.List;
import java.util.Optional;

import com.google.inject.Inject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;
import se.tink.backend.system.workers.processor.TransactionProcessorUserData;
import se.tink.backend.util.GuiceRunner;
import se.tink.backend.util.TestProcessor;
import se.tink.backend.util.TestUtil;

@RunWith(GuiceRunner.class)
public class ResetExistingCommandTest {
    @Inject
    private TestUtil testUtil;

    @Inject
    CategoryRepository categoryRepository;

    private List<User> users;

    @Before
    public void setUp() {
        users = testUtil.getTestUsers("ResetExistingCommandTest");
    }

    /**
     * Tests if transactions was reset correct.
     * Fields to reset:
     * - categoryType
     * - categoryId
     * - isUserModifiedCategory
     * - description
     */
    @Test
    public void testResetOfExistingTransactions() {
        users.forEach(user -> {
            final String credentialId = "cr1";
            final String accountId = "acc1";

            final TransactionProcessorUserData userData = new TransactionProcessorUserData();

            final Credentials cred1 = new Credentials();
            cred1.setId(credentialId);

            final List<Credentials> credentials = Lists.newArrayList();
            credentials.add(cred1);
            userData.setCredentials(credentials);

            final Account acc1 = new Account();
            acc1.setId(accountId);

            final List<Account> accounts = Lists.newArrayList();
            accounts.add(acc1);
            userData.setAccounts(accounts);

            List<Category> categories = categoryRepository.findLeafCategories();

            Assert.assertTrue(categories.size() > 0);

            Optional<Category> category = categories.stream().filter(c -> c.getType() == CategoryTypes.EXPENSES)
                    .findFirst();

            Assert.assertTrue(category.isPresent());

            List<Transaction> transactions = Lists.newArrayList();

            Transaction transaction = testUtil.getNewTransaction(user.getId(), -95, "Pontus", "02", "30");
            transaction.setUserId(user.getId());
            transaction.setAccountId(accountId);
            transaction.setUserModifiedLocation(false);
            transaction.setUserModifiedAmount(false);
            transaction.setUserModifiedCategory(false);
            transaction.setUserModifiedDate(false);
            transaction.setUserModifiedDescription(false);
            transaction.setCategory(category.get());

            transactions.add(transaction);

            userData.setInStoreTransactions(transactions);

            // Make sure we have correct data before running reset command.
            Assert.assertEquals(transaction.getCategoryId(), category.get().getId());
            Assert.assertEquals(transaction.getCategoryType(), category.get().getType());
            Assert.assertEquals(transaction.isUserModifiedCategory(), false);
            Assert.assertEquals(transaction.getDescription(), "Pontus");

            final TransactionProcessorContext context = new TransactionProcessorContext(
                    user,
                    testUtil.getProvidersByName(),
                    transactions
            );
            context.setUserData(userData);

            ResetExistingCommand command = new ResetExistingCommand(
                    context,
                    false
            );
            command.initialize();

            List<Transaction> finalTransactions = context.getInBatchTransactions();

            for (Transaction t : finalTransactions) {

                if (t != null) {

                    Assert.assertEquals(t.getCategoryId(), null);
                    Assert.assertEquals(t.getCategoryType(), null);
                    Assert.assertEquals(t.isUserModifiedCategory(), false);
                    Assert.assertEquals(t.getDescription(), null);
                }
            }
        });
    }

    /**
     * Tests if transactions was reset correct.
     * Fields to reset:
     * - description
     */
    @Test
    public void testResetOfExistingTransactions_userModifiedCategory() {
        users.forEach(user -> {

            final String credentialId = "cr1";
            final String accountId = "acc1";

            final TransactionProcessorUserData userData = new TransactionProcessorUserData();

            final Credentials cred1 = new Credentials();
            cred1.setId(credentialId);

            final List<Credentials> credentials = Lists.newArrayList();
            credentials.add(cred1);
            userData.setCredentials(credentials);

            final Account acc1 = new Account();
            acc1.setId(accountId);

            final List<Account> accounts = Lists.newArrayList();
            accounts.add(acc1);
            userData.setAccounts(accounts);

            List<Category> categories = categoryRepository.findLeafCategories();

            Assert.assertTrue(categories.size() > 0);

            Optional<Category> category = categories.stream().filter(c -> c.getType() == CategoryTypes.EXPENSES)
                    .findFirst();

            Assert.assertTrue(category.isPresent());

            List<Transaction> transactions = Lists.newArrayList();

            Transaction transaction = testUtil.getNewTransaction(user.getId(), -95, "Pontus", "02", "30");
            transaction.setUserId(user.getId());
            transaction.setAccountId(accountId);
            transaction.setUserModifiedLocation(false);
            transaction.setUserModifiedAmount(false);
            transaction.setUserModifiedCategory(true);
            transaction.setUserModifiedDate(false);
            transaction.setUserModifiedDescription(false);
            transaction.setCategory(category.get());

            transactions.add(transaction);

            userData.setInStoreTransactions(transactions);

            // Make sure we have correct data before running reset command.
            Assert.assertEquals(transaction.getCategoryId(), category.get().getId());
            Assert.assertEquals(transaction.getCategoryType(), CategoryTypes.EXPENSES);
            Assert.assertEquals(transaction.isUserModifiedCategory(), true);
            Assert.assertEquals(transaction.getDescription(), "Pontus");

            final TransactionProcessorContext context = new TransactionProcessorContext(
                    user,
                    testUtil.getProvidersByName(),
                    transactions
            );
            context.setUserData(userData);

            ResetExistingCommand command = new ResetExistingCommand(
                    context,
                    false
            );
            command.initialize();

            List<Transaction> finalTransactions = context.getInBatchTransactions();

            for (Transaction t : finalTransactions) {

                if (t != null) {

                    Assert.assertEquals(t.getCategoryId(), category.get().getId());
                    Assert.assertEquals(t.getCategoryType(), CategoryTypes.EXPENSES);
                    Assert.assertEquals(t.isUserModifiedCategory(), true);
                    Assert.assertEquals(t.getDescription(), null);
                }
            }
        });
    }

    /**
     * Tests if transactions was reset correct.
     * Fields to reset:
     * - categoryType
     * - categoryId
     * - isUserModifiedCategory
     */
    @Test
    public void testResetOfExistingTransactions_userModifiedDescription() {
        users.forEach(user -> {
            final String credentialId = "cr1";
            final String accountId = "acc1";

            final TransactionProcessorUserData userData = new TransactionProcessorUserData();

            final Credentials cred1 = new Credentials();
            cred1.setId(credentialId);

            final List<Credentials> credentials = Lists.newArrayList();
            credentials.add(cred1);
            userData.setCredentials(credentials);

            final Account acc1 = new Account();
            acc1.setId(accountId);

            final List<Account> accounts = Lists.newArrayList();
            accounts.add(acc1);
            userData.setAccounts(accounts);

            List<Category> categories = categoryRepository.findLeafCategories();

            Assert.assertTrue(categories.size() > 0);

            Optional<Category> category = categories.stream().filter(c -> c.getType() == CategoryTypes.EXPENSES)
                    .findFirst();

            Assert.assertTrue(category.isPresent());

            List<Transaction> transactions = Lists.newArrayList();

            Transaction transaction = testUtil.getNewTransaction(user.getId(), -95, "Pontus", "02", "30");
            transaction.setOriginalDescription("ICA");
            transaction.setUserId(user.getId());
            transaction.setAccountId(accountId);
            transaction.setUserModifiedLocation(false);
            transaction.setUserModifiedAmount(false);
            transaction.setUserModifiedCategory(false);
            transaction.setUserModifiedDate(false);
            transaction.setUserModifiedDescription(true);
            transaction.setCategory(category.get());

            transactions.add(transaction);

            userData.setInStoreTransactions(transactions);

            // Make sure we have correct data before running reset command.
            Assert.assertEquals(transaction.getCategoryId(), category.get().getId());
            Assert.assertEquals(transaction.getCategoryType(), CategoryTypes.EXPENSES);
            Assert.assertEquals(transaction.isUserModifiedDescription(), true);
            Assert.assertEquals(transaction.getDescription(), "Pontus");
            Assert.assertEquals(transaction.getOriginalDescription(), "ICA");

            final TransactionProcessorContext context = new TransactionProcessorContext(
                    user,
                    testUtil.getProvidersByName(),
                    transactions
            );
            context.setUserData(userData);

            ResetExistingCommand command = new ResetExistingCommand(
                    context,
                    false
            );
            command.initialize();

            List<Transaction> finalTransactions = context.getInBatchTransactions();

            for (Transaction t : finalTransactions) {

                if (t != null) {

                    Assert.assertEquals(t.getCategoryId(), null);
                    Assert.assertEquals(t.getCategoryType(), null);
                    Assert.assertEquals(t.isUserModifiedCategory(), false);
                    Assert.assertEquals(t.getDescription(), "Pontus");
                    Assert.assertEquals(t.getOriginalDescription(), "ICA");
                }
            }
        });
    }

}
