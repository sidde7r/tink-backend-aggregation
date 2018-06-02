package se.tink.backend.system.workers.processor.deduplication;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.categorization.api.SECategories;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.UserData;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;
import se.tink.backend.util.GuiceRunner;
import se.tink.backend.util.TestProcessor;
import se.tink.backend.util.TestUtil;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.core.Category;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionTypes;
import se.tink.backend.core.User;
import se.tink.backend.utils.guavaimpl.Orderings;

import static org.mockito.Mockito.when;

/**
 * TODO this is a unit test
 */
@RunWith(GuiceRunner.class)
public class DuplicateTransactionsTest {
    private static final String ACCOUNT_ID = UUIDUtils.toTinkUUID(UUID.randomUUID());
    private static final String CREDENTIALS_ID = UUIDUtils.toTinkUUID(UUID.randomUUID());

    @Inject
    private TestUtil testUtil;

    @Inject
    private TestProcessor testProcessor;

    @Inject
    private CategoryRepository categoryRepository;

    List<Category> categories;

    @Before
    public void setUp () {
        categories = categoryRepository.findAll();
    }

    @Test
    public void ensureDuplicateTransactions_areRemoved_And_updatedPendingTransactions_areUpdated() {
        List<User> users = testUtil.getTestUsers("DuplicateTransactionsTest");
        users.forEach(user -> {
            Credentials credentials = testUtil.getCredentials(user, "swedbank-bankid");

            List<Transaction> inStoreTransactions = createInStoreTransactions(user.getId());

            Account account = new Account();
            account.setId(ACCOUNT_ID);
            account.setUserId(user.getId());

            UserData userData = testUtil.getUserData(user, credentials, Lists.newArrayList(account), Lists.newArrayList());

            TransactionProcessorContext context = new TransactionProcessorContext(
                    user,
                    testUtil.getProvidersByName(),
                    inStoreTransactions,
                    userData,
                    credentials.getId()
            );


            TransactionProcessorContext processContextFirst = process(context, userData);

            List<Transaction> inBatchTransactions = createInBatchTransactions(user.getId(), inStoreTransactions, testUtil.stringToDate("02", "16"));
            processContextFirst.updateInBatchTransactions(inBatchTransactions);

            TransactionProcessorContext processContextSecond = process(processContextFirst, userData);
            List<Transaction> transactionsToDelete = processContextSecond.getTransactionsToDelete();
            List<Transaction> allTransactions = processContextSecond.getTransactionsToSave().values().stream()
                    .filter(t -> !transactionsToDelete.contains(t))
                    .collect(Collectors.toList());
            List<Transaction> pendingTransactions = processContextSecond.getTransactionsToSave().values().stream().filter(Transaction::isPending)
                    .collect(Collectors.toList());
            Assert.assertEquals(1, pendingTransactions.size());
            Assert.assertEquals(8, allTransactions.size());

            for (Transaction transaction : allTransactions) {
                if (transaction.getDescription().contains("duplicate")) {
                    throw new AssertionError("Duplicate transaction was not removed: " + transaction);
                }
            }
        });
    }

    private TransactionProcessorContext process(TransactionProcessorContext context, UserData userData) {
        testProcessor.process(context, userData);
        return context;
    }


    private List<Transaction> createInStoreTransactions(String userId) {
        List<Transaction> transactions = Lists.newArrayList();

        String description = "Store non pending";
        transactions.add(getTransaction(userId,1, description, 894, "02", "13", false));
        transactions.add(getTransaction(userId,2, description, 629, "02", "16", false));
        transactions.add(getTransaction(userId,3, description, 725, "02", "25", false));

        description = "Store non pending duplicate";
        transactions.add(getTransaction(userId,4, description, 725, "02", "25", false));

        description = "Store non pending";
        transactions.add(getTransaction(userId,5, description, 312, "02", "29", false));

        // PENDING TRANSACTIONS
        description = "Store pending";
        transactions.add(getTransaction(userId,6, description, 57, "01", "03", true));
        transactions.add(getTransaction(userId,7, description, 529, "01", "05", true));

        return updateInsertedAndTimestamp(transactions, "02", "29");
    }

    private List<Transaction> createInBatchTransactions(String userId, List<Transaction> inStoreTransactions, Date cutOfDate) {
        List<Transaction> transactions = getOverlappedTransactions(inStoreTransactions, cutOfDate);

        transactions.add(getTransaction(userId, 1, "New non pending", 372, "01", "04", false));
        transactions.add(getTransaction(userId, 2, "New pending", 926, "01", "08", true));

        return updateInsertedAndTimestamp(transactions, "01", "05");
    }

    private List<Transaction> getOverlappedTransactions(List<Transaction> inStoreTransactions, Date cutOfDate) {
        List<Transaction> overlappedTransactions = Lists.newArrayList();

        for (int i = inStoreTransactions.size() - 1; i > 0; i--) {
            Transaction storeTransaction = inStoreTransactions.get(i);
            if (!storeTransaction.isPending() && storeTransaction.getDate().before(cutOfDate)) {
                break;
            }

            Transaction transaction = storeTransaction.clone();
            transaction.setId(UUIDUtils.toTinkUUID(UUID.randomUUID()));

            if (transaction.isPending()) {
                transaction.setPending(false);
                transaction.setDescription(transaction.getDescription().replace("pending", "previous pending"));
                transaction.setOriginalDescription(transaction.getDescription());
            }

            if (!transaction.getDescription().contains("duplicate")) {
                overlappedTransactions.add(transaction);
            }
        }

        return overlappedTransactions.stream().sorted(Orderings.TRANSACTION_DATE_ORDERING).collect(Collectors.toList());
    }

    private List<Transaction> updateInsertedAndTimestamp(List<Transaction> transactions, String monthsAgo, String day) {
        long inserted = testUtil.stringToDate(monthsAgo, day).getTime();
        long timestamp = inserted + 1;

        for (Transaction transaction : transactions) {
            transaction.setId(UUIDUtils.toTinkUUID(UUID.randomUUID()));
            transaction.setInserted(inserted);
            transaction.setTimestamp(timestamp);

            timestamp++;
        }

        return transactions;
    }

    private Transaction getTransaction(String userId, int transactionNum, String description, double amount,
                                       String monthsAgo, String day, boolean pending) {
        Transaction transaction = new Transaction();
        Date dueDate = testUtil.stringToDate(monthsAgo, day);

        description = String.format("%s %s", description, transactionNum);

        transaction.setUserId(userId);
        transaction.setCredentialsId(CREDENTIALS_ID);
        transaction.setAccountId(ACCOUNT_ID);
        transaction.setDate(dueDate);
        transaction.setOriginalDate(transaction.getDate());
        transaction.setAmount(amount);
        transaction.setOriginalAmount(amount);
        transaction.setDescription(description);
        transaction.setOriginalDescription(description);
        transaction.setPending(pending);
        transaction.setType(TransactionTypes.DEFAULT);
        Category bars = categories.stream().filter(c -> c.getCode().equals(SECategories.Codes.EXPENSES_FOOD_BARS))
                .findFirst().get();
        when(categoryRepository.findByCode("bars:mocked")).thenReturn(bars);
        Category barCategory = categoryRepository.findByCode("bars:mocked");

        transaction.setCategory(barCategory);

        return transaction;
    }
}
