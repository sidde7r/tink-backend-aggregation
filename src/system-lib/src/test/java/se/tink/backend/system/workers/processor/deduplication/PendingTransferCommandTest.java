package se.tink.backend.system.workers.processor.deduplication;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.inject.Inject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.categorization.api.SECategories;
import se.tink.backend.common.dao.CategoryChangeRecordDao;
import se.tink.backend.common.dao.ProviderDao;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.ClusterCategories;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.PendingStringTypes;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.UserData;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;
import se.tink.backend.system.workers.processor.chaining.SimpleChainFactory;
import se.tink.backend.system.workers.processor.storage.LoadUserDataCommand;
import se.tink.backend.system.workers.processor.storage.PrepareTransactionsToSaveAndDeleteCommand;
import se.tink.backend.system.workers.processor.storage.SaveTransactionCommand;
import se.tink.backend.system.workers.processor.transfers.TransferDetectionCommand;
import se.tink.backend.system.workers.processor.transfers.scoring.TransferDetectionScorerFactory;
import se.tink.backend.util.GuiceRunner;
import se.tink.backend.util.TestProcessor;
import se.tink.backend.util.TestUtil;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.metrics.MetricRegistry;

/**
 * Tests verifies that Swedbank transfers are matched correctly when the description is changed and account number
 * is added
 */
@RunWith(GuiceRunner.class)
public class PendingTransferCommandTest {
    private List<User> users;
    private Map<String, Credentials> credentials;

    private Map<String, UserData> userDatas;

    private String accountId1 = StringUtils.generateUUID();
    private String accountId2 = StringUtils.generateUUID();
    private Category unCategorizedCategory ;

    @Inject
    TestUtil testUtil;

    @Inject
    TestProcessor transactionProcessor;

    @Inject
    private CredentialsRepository credentialsRepository;
    @Inject
    private LoanDataRepository loanDataRepository;
    @Inject
    private TransactionDao transactionDao;
    @Inject
    private AccountRepository accountRepository;
    @Inject
    private CategoryRepository categoryRepository;
    @Inject
    private CategoryConfiguration categoryConfiguration;

    @Inject
    private Cluster cluster;
    @Inject
    private CategoryChangeRecordDao categoryChangeRecordDao;
    @Inject
    private MetricRegistry metricRegistry;

    @Before
    public void setup() {
        unCategorizedCategory = new Category();
        unCategorizedCategory.setCode(SECategories.Codes.EXPENSES_MISC_UNCATEGORIZED);
        unCategorizedCategory.setType(CategoryTypes.EXPENSES);
        users = testUtil.getTestUsers("PendingTransferCommandTest");
        credentials = users.stream().map(user -> {
            return testUtil.getCredentials(user, "swedbank-bankid");
        }).collect(Collectors.toMap(c -> c.getUserId(), c -> c));

        this.userDatas = users.stream().map((user -> {
            UserData userData = new UserData();
            userData.setUser(user);
            userData.setCredentials(com.google.common.collect.Lists.newArrayList(credentials.get(user.getId())));
            return userData;
        })).collect(Collectors.toMap(ud -> ud.getUser().getId(), ud -> ud));
    }

    private TransactionProcessorContext process(User user, List<Transaction> transactions, List<Transaction> inStore) {
        userDatas.get(user.getId()).setTransactions(inStore);

        TransactionProcessorContext context = new TransactionProcessorContext(
                user,
                testUtil.getProvidersByName(),
                transactions,
                userDatas.get(user.getId()),
                credentials.get(user.getId()).getId()
        );

        return transactionProcessor.process(context,userDatas.get(user.getId()),() -> new SimpleChainFactory(
                        ctx -> ImmutableList.of(
                                new LoadUserDataCommand(
                                        ctx,
                                        credentialsRepository,
                                        loanDataRepository,
                                        transactionDao,
                                        accountRepository
                                ),
                                new PendingTransactionCommand(ctx),
                                new TransferDetectionCommand(
                                        ctx,
                                        categoryConfiguration,
                                        TransferDetectionScorerFactory
                                                .byCluster(cluster),
                                        new ClusterCategories(
                                                categoryRepository.findAll()),
                                        categoryChangeRecordDao
                                ),
                                new PrepareTransactionsToSaveAndDeleteCommand(ctx, metricRegistry),
                                new SaveTransactionCommand(ctx, transactionDao, metricRegistry)
                        )
                ),
                true);
    }

    /**
     * Verifies that pending transactions will be updated
     */
    @Test
    public void swedbankPendingTransactionsShouldBeUpdated() {
        users.forEach(user -> {
            // First batch of transactions
            List<Transaction> transactions = Lists.newArrayList();
            Transaction t1 = getNewPendingSwedbankTransaction(user.getId(), accountId1, -2000);
            Transaction t2 = getNewPendingSwedbankTransaction(user.getId(), accountId2, 2000);
            transactions.add(t1);
            transactions.add(t2);

            TransactionProcessorContext firstContext = process(user, transactions, Lists.newArrayList());
            List<Transaction> inStore = firstContext.getTransactionsToSave().values().stream().collect(Collectors.toList());

            Assert.assertEquals(2, inStore.size());

            // Second batch of transactions with updated transactions
            transactions = Lists.newArrayList();
            Transaction t3 = getNewNonPendingSwedbankTransaction(user.getId(), accountId1, -2000, "1111111111111");
            Transaction t4 = getNewNonPendingSwedbankTransaction(user.getId(), accountId2, 2000, "999999999999");
            transactions.add(t3);
            transactions.add(t4);

            TransactionProcessorContext secondContext = process(user, transactions, inStore);
            inStore = secondContext.getTransactionsToSave().values()
                    .stream().collect(Collectors.toList());
            // Get transactions by old id since they have been updated
            Transaction result1 = inStore.stream().filter(t -> t.getUserId().equals(user.getId()) && t.getId().equals(t1.getId()))
                    .findFirst().get();
            Transaction result2 = inStore.stream().filter(t -> t.getUserId().equals(user.getId()) && t.getId().equals(t2.getId()))
                    .findFirst().get();

            Assert.assertEquals(2, inStore.size());
            Assert.assertNotNull(result1);
            Assert.assertNotNull(result2);
            Assert.assertEquals(false, result1.isPending());
            Assert.assertEquals(false, result2.isPending());
            Assert.assertEquals(t3.getOriginalDescription(), result1.getOriginalDescription());
            Assert.assertEquals(t4.getOriginalDescription(), result2.getOriginalDescription());
        });
    }

    /**
     * Verifies that two pending transactions in the same batch with different amounts will be updated
     */
    @Test
    public void multipleSwedbankPendingTransactionsShouldBeUpdated() {
        users.forEach(user -> {
            // First batch of transactions
            List<Transaction> transactions = Lists.newArrayList();
            Transaction t1 = getNewPendingSwedbankTransaction(user.getId(), accountId1, -1000);
            Transaction t2 = getNewPendingSwedbankTransaction(user.getId(), accountId2, 1000);
            Transaction t3 = getNewPendingSwedbankTransaction(user.getId(), accountId1, -2000);
            Transaction t4 = getNewPendingSwedbankTransaction(user.getId(), accountId2, 2000);

            transactions.add(t1);
            transactions.add(t2);
            transactions.add(t3);
            transactions.add(t4);

            TransactionProcessorContext firstContext = process(user, transactions, Lists.newArrayList());
            List<Transaction> inStore = firstContext.getTransactionsToSave().values().stream().collect(Collectors.toList());
            Assert.assertEquals(4, inStore.size());

            // Second batch of transactions with updated transactions
            transactions = Lists.newArrayList();
            Transaction t5 = getNewNonPendingSwedbankTransaction(user.getId(), accountId1, -1000, "12345678912343124987");
            Transaction t6 = getNewNonPendingSwedbankTransaction(user.getId(), accountId2, 1000, "973946924749272176394");
            Transaction t7 = getNewNonPendingSwedbankTransaction(user.getId(), accountId1, -2000, "121212121212233423434");
            Transaction t8 = getNewNonPendingSwedbankTransaction(user.getId(), accountId2, 2000, "78345323242555");

            transactions.add(t5);
            transactions.add(t6);
            transactions.add(t7);
            transactions.add(t8);

            TransactionProcessorContext secondContext = process(user, transactions, inStore);
            List<Transaction> secondInStore = secondContext.getTransactionsToSave().values().stream().collect(Collectors.toList());
            // Get transactions by old id since they have been updated
            Transaction result1 = secondInStore.stream()
                    .filter(t -> t.getUserId().equals(user.getId())&& t.getId().equals(t1.getId())).findFirst().get();
            Transaction result2 = secondInStore.stream()
                    .filter(t -> t.getUserId().equals(user.getId())&& t.getId().equals(t2.getId())).findFirst().get();
            Transaction result3 = secondInStore.stream()
                    .filter(t -> t.getUserId().equals(user.getId())&& t.getId().equals(t3.getId())).findFirst().get();
            Transaction result4 = secondInStore.stream()
                    .filter(t -> t.getUserId().equals(user.getId())&& t.getId().equals(t4.getId())).findFirst().get();

            Assert.assertEquals(4, secondInStore.size());
            Assert.assertNotNull(result1);
            Assert.assertNotNull(result2);
            Assert.assertNotNull(result3);
            Assert.assertNotNull(result4);
            Assert.assertEquals(false, result1.isPending());
            Assert.assertEquals(false, result2.isPending());
            Assert.assertEquals(false, result3.isPending());
            Assert.assertEquals(false, result4.isPending());
            Assert.assertEquals(t5.getOriginalDescription(), result1.getOriginalDescription());
            Assert.assertEquals(t6.getOriginalDescription(), result2.getOriginalDescription());
            Assert.assertEquals(t7.getOriginalDescription(), result3.getOriginalDescription());
            Assert.assertEquals(t8.getOriginalDescription(), result4.getOriginalDescription());
        });
    }

    /**
     * Verifies that two pending transactions in the same batch with the same amounts will be updated
     */
    @Test
    public void multipleSwedbankPendingTransactionsWithSameAmountShouldBeUpdated() {
        users.forEach(user -> {
            // First batch of transactions
            List<Transaction> transactions = Lists.newArrayList();
            Transaction t1 = getNewPendingSwedbankTransaction(user.getId(), accountId1, -2000);
            Transaction t2 = getNewPendingSwedbankTransaction(user.getId(), accountId2, 2000);
            Transaction t3 = getNewPendingSwedbankTransaction(user.getId(), accountId1, -2000);
            Transaction t4 = getNewPendingSwedbankTransaction(user.getId(), accountId2, 2000);

            transactions.add(t1);
            transactions.add(t2);
            transactions.add(t3);
            transactions.add(t4);

            TransactionProcessorContext firstContext = process(user, transactions, Lists.newArrayList());
            List<Transaction> inStore = firstContext.getTransactionsToSave().values().stream().collect(Collectors.toList());
            Assert.assertEquals(4, inStore.size());

            // Second batch of transactions with updated transactions
            transactions = Lists.newArrayList();
            Transaction t5 = getNewNonPendingSwedbankTransaction(user.getId(), accountId1, -2000, "12345678912343124987");
            Transaction t6 = getNewNonPendingSwedbankTransaction(user.getId(), accountId2, 2000, "973946924749272176394");
            Transaction t7 = getNewNonPendingSwedbankTransaction(user.getId(), accountId1, -2000, "12345678912343124987");
            Transaction t8 = getNewNonPendingSwedbankTransaction(user.getId(), accountId2, 2000, "973946924749272176394");

            transactions.add(t5);
            transactions.add(t6);
            transactions.add(t7);
            transactions.add(t8);

            TransactionProcessorContext secondContext = process(user, transactions, inStore);
            List<Transaction> secondInStore = secondContext.getTransactionsToSave().values().stream().collect(Collectors.toList());
            // Get transactions by old id since they have been updated
            Transaction result1 = secondInStore.stream()
                    .filter(t -> t.getUserId().equals(user.getId())&& t.getId().equals(t1.getId())).findFirst().get();
            Transaction result2 = secondInStore.stream()
                    .filter(t -> t.getUserId().equals(user.getId())&& t.getId().equals(t2.getId())).findFirst().get();
            Transaction result3 = secondInStore.stream()
                    .filter(t -> t.getUserId().equals(user.getId())&& t.getId().equals(t3.getId())).findFirst().get();
            Transaction result4 = secondInStore.stream()
                    .filter(t -> t.getUserId().equals(user.getId())&& t.getId().equals(t4.getId())).findFirst().get();

            Assert.assertEquals(4, secondInStore.size());

            Assert.assertNotNull(result1);
            Assert.assertNotNull(result2);
            Assert.assertNotNull(result3);
            Assert.assertNotNull(result4);
            Assert.assertEquals(false, result1.isPending());
            Assert.assertEquals(false, result2.isPending());
            Assert.assertEquals(false, result3.isPending());
            Assert.assertEquals(false, result4.isPending());
            Assert.assertEquals(t5.getOriginalDescription(), result1.getOriginalDescription());
            Assert.assertEquals(t6.getOriginalDescription(), result2.getOriginalDescription());
            Assert.assertEquals(t7.getOriginalDescription(), result3.getOriginalDescription());
            Assert.assertEquals(t8.getOriginalDescription(), result4.getOriginalDescription());
        });
    }

    private Transaction getNewPendingSwedbankTransaction(String userId, String accountId, double amount) {
        Transaction t = testUtil.getNewTransaction(userId, amount, PendingStringTypes.SWEDBANK_PENDING_TRANSFER.getValue());
        t.setAccountId(accountId);
        t.setCategory(unCategorizedCategory);
        t.setPending(true);
        return t;
    }

    private Transaction getNewNonPendingSwedbankTransaction(String userId, String accountId, double amount,
                                                            String accountNumber) {

        String description = PendingStringTypes.SWEDBANK_PENDING_TRANSFER.getValue() + " " + accountNumber;

        Transaction t = testUtil.getNewTransaction(userId, amount, description);
        t.setAccountId(accountId);
        t.setCategory(unCategorizedCategory);
        t.setPending(false);
        return t;
    }

}
