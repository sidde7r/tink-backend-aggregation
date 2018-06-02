package se.tink.backend.system.workers.processor.deduplication;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.categorization.api.SECategories;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.core.Account;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.core.User;
import se.tink.backend.core.UserData;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;
import se.tink.backend.util.GuiceRunner;
import se.tink.backend.util.TestProcessor;
import se.tink.backend.util.TestUtil;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RunWith(GuiceRunner.class)
public class DeduplicationCommandTest {
    private List<User> users;
    private Map<String, Credentials> credentials;

    private Map<String, UserData> userDatas;
    @Inject
    private TestProcessor transactionProcessor;
    @Inject
    private TestUtil testUtil;
    @Inject
    private TransactionDao transactionDao;

    @Before
    public void setup() {
        users = testUtil.getTestUsers("DeduplicationCommandTest");
        this.credentials = users.stream().map(user ->{
            return testUtil.getCredentials(user, "swedbank-bankid");
        }).collect(Collectors.toMap(c -> c.getUserId(), c -> c));

        this.userDatas = users.stream().map((user -> {
            UserData userData = new UserData();
            userData.setUser(user);
            userData.setCredentials(com.google.common.collect.Lists.newArrayList(credentials.get(user.getId())));
            return userData;
        })).collect(Collectors.toMap(ud -> ud.getUser().getId(), ud -> ud));
    }

    private TransactionProcessorContext process(User user, List<Transaction> transactions) {
        userDatas.get(user.getId()).setTransactions(transactionDao.findAllByUserId(user.getId()));

        TransactionProcessorContext context = new TransactionProcessorContext(
                user,
                testUtil.getProvidersByName(),
                transactions,
                userDatas.get(user.getId()),
                credentials.get(user.getId()).getId()
        );

        transactionProcessor.process(context, userDatas.get(user.getId()));
        return context;
    }

    /**
     * Test will add two "identical" transactions in the same batch to verify that
     * they aren't treated as duplicates
     */
    @Test
    public void twoSimilarTransactionInSameBatchShouldNotBeDuplicates() {
        users.forEach(user -> {
            Account account = new Account();
            account.setUserId(user.getId());

            // Process transactions
            List<Transaction> transactions = ImmutableList.of(getNewTransaction(account), getNewTransaction(account));

            TransactionProcessorContext contextResult = process(user, transactions);

            Assert.assertEquals(2, contextResult.getTransactionsToSave().size());

            // Re-process transactions
            transactions = ImmutableList.of(getNewTransaction(account), getNewTransaction(account));

            TransactionProcessorContext reContextResult = process(user, transactions);

            Assert.assertEquals(2, reContextResult.getTransactionsToSave().size());
        });
    }

    /**
     * Test to add a transaction and then another identical but with external id
     */
    @Test
    public void testExternalIdAndNoExternalIdMatching() {
        users.forEach(user -> {
            Account account = new Account();
            account.setUserId(user.getId());

            // Process transactions
            List<Transaction> transactions = ImmutableList.of(getNewTransaction(account));

            TransactionProcessorContext processorContext = process(user, transactions);

            Assert.assertEquals(1, processorContext.getTransactionsToSave().size());

            // Re-process transactions
            transactions = ImmutableList.of(getNewTransaction(account, "123"));

            TransactionProcessorContext reProcessorContext = process(user, transactions);

            Assert.assertEquals(1, reProcessorContext.getTransactionsToSave().size());
        });
    }

    /**
     * Test to add a transaction with external id and then another identical but without external id
     */
    @Test
    public void testNoExternalIdAndExternalIdMatching() {
        users.forEach(user -> {
            Account account = new Account();
            account.setUserId(user.getId());

            // Process transactions
            List<Transaction> transactions = ImmutableList.of(getNewTransaction(account, "123"));

            TransactionProcessorContext processorContext = process(user, transactions);

            Assert.assertEquals(1, processorContext.getTransactionsToSave().size());

            // Re-process transactions

            transactions = ImmutableList.of(getNewTransaction(account));

            TransactionProcessorContext reProcessorContext = process(user, transactions);

            Assert.assertEquals(1, reProcessorContext.getTransactionsToSave().size());
        });
    }

    private Transaction getNewTransaction(Account account, String externalId) {
        Transaction transaction = getNewTransaction(account);
        transaction.setPayload(TransactionPayloadTypes.EXTERNAL_ID, externalId);
        return transaction;
    }

    private Transaction getNewTransaction(Account account) {
        Category unkownExpenseCategory= new Category();
        unkownExpenseCategory.setCode(SECategories.Codes.EXPENSES_MISC_UNCATEGORIZED);
        unkownExpenseCategory.setType(CategoryTypes.EXPENSES);
        Transaction t = testUtil.getNewTransaction(account.getUserId(), -80, "Thai-mat");
        t.setCategory(unkownExpenseCategory);
        t.setInserted(System.currentTimeMillis());
        t.setAccountId(account.getId());
        t.setPending(true);

        return t;
    }
} 
