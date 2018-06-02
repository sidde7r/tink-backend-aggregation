package se.tink.backend.system.workers.processor.categorization;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.categorization.api.CategoryConfiguration;
import se.tink.backend.categorization.api.SECategories;
import se.tink.backend.common.dao.CategoryChangeRecordDao;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.ClusterCategories;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.core.User;
import se.tink.backend.core.UserData;
import se.tink.backend.util.GuiceRunner;
import se.tink.backend.util.TestProcessor;
import se.tink.backend.util.TestUtil;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.system.workers.processor.TransactionProcessorCommand;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;
import se.tink.backend.system.workers.processor.chaining.SimpleChainFactory;
import se.tink.backend.system.workers.processor.storage.LoadUserDataCommand;
import se.tink.backend.system.workers.processor.storage.PrepareTransactionsToSaveAndDeleteCommand;
import se.tink.backend.system.workers.processor.storage.SaveTransactionCommand;
import se.tink.backend.system.workers.processor.transfers.TransferDetectionCommand;
import se.tink.backend.system.workers.processor.transfers.scoring.TransferDetectionScorerFactory;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.abnamro.utils.AbnAmroUtils;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.metrics.MetricRegistry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(GuiceRunner.class)
public class TransferDetectionCommandTest {
    private Category unCategorizedExpenseCategory;
    private Category unCategorizedIncomeCategory;
    int amount = 1000;

    private Map<String , UserData> userDatas ;
    private List<User> users;
    private Map<String, Credentials> swedbankCredential;
    private Map<String, Credentials> handelsbankenCredential;
    private Map<String, Credentials> nordeaCredential;
    private AtomicInteger userNo = new AtomicInteger(0);

    @Inject
    private TestUtil testUtil;

    @Inject
    private TestProcessor transactionProcessor;


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
    private CategoryChangeRecordDao categoryChangeRecordDao;
    @Inject
    private MetricRegistry metricRegistry;


    @Before
    public void setup() {
        List<Category> categories = categoryRepository.findAll();
        unCategorizedExpenseCategory = categories.stream()
                .filter(c -> c.getCode().equals(SECategories.Codes.EXPENSES_MISC_UNCATEGORIZED))
                .findFirst().get();

        unCategorizedIncomeCategory =
                categories.stream()
                        .filter(c -> c.getCode().equals(SECategories.Codes.INCOME_OTHER_OTHER))
                        .findFirst().get();

        users = testUtil.getTestUsers(String.format("TransferDetectionCommandTestUser-%s", userNo.incrementAndGet()));
        swedbankCredential = users.stream()
                .map( user ->testUtil.getCredentials(user, "swedbank-bankid")).collect(Collectors.toMap(c-> c.getUserId(), c -> c));
        handelsbankenCredential = users.stream()
        .map( user ->testUtil.getCredentials(user, "handelsbanken-bankid")).collect(Collectors.toMap(c-> c.getUserId(), c -> c));
        nordeaCredential = users.stream()
        .map( user ->testUtil.getCredentials(user, "nordea-bankid")).collect(Collectors.toMap(c-> c.getUserId(), c -> c));
        userDatas = users.stream().map(user -> {
            UserData userData = new UserData();
            userData.setUser(user);
            userData.setCredentials(Lists.newArrayList(swedbankCredential.get(user.getId()), handelsbankenCredential.get(user.getId()), nordeaCredential.get(user.getId())));
            return userData;
        }).collect(Collectors.toMap(ud -> ud.getUser().getId(), ud -> ud));
    }

    private TransactionProcessorContext process(List<Transaction> transactions, User user, List<Transaction> inStore, Cluster cluster, List<Account> accounts) {
        if (accounts != null && !accounts.isEmpty()) {
            UserData userData = userDatas.get(user.getId());
            userData.setAccounts(accounts);
        }

        return process(transactions, user, inStore, cluster);
    }
    private TransactionProcessorContext process(List<Transaction> transactions, User user, List<Transaction> inStore, Cluster cluster) {
        UserData userData = userDatas.get(user.getId());
        userData.setTransactions(inStore);

        TransactionProcessorContext context = new TransactionProcessorContext(
                user,
                testUtil.getProvidersByName(),
                transactions,
                userData,
                swedbankCredential.get(user.getId()).getId()
        );
        Function<TransactionProcessorContext, ImmutableList<TransactionProcessorCommand>> simpleChain = (TransactionProcessorContext tcon) -> transferCommands(tcon,cluster);

        transactionProcessor.process(context, userData, () -> new SimpleChainFactory(simpleChain),
                    true);
        return context;
    }

    @Test
    public void findTransferSameDay() {

        List<Transaction> transactions = Lists.newArrayList();
        for (int i = 1; i < 4; i++) {
            Transaction t = new Transaction();
            t.setDescription("transfer" + i);
            t.setOriginalDate(new Date());
            if (i % 2 == 0) {
                t.setOriginalAmount(amount * -1);
                t.setAccountId("myAccount");
            } else {
                t.setOriginalAmount(amount);
                t.setAccountId("otherAccount");
            }
            transactions.add(t);
        }

        assertTrue("Transfers should be corresponding",
                transactions.get(0).canBeTwinTransfer(transactions.get(1)));
        assertFalse("Transfers should not be corresponding",
                transactions.get(0).canBeTwinTransfer(transactions.get(2)));
    }

    @Test
    public void findTransferTodayMinusOne() {
        // trans1
        Transaction today = new Transaction();
        today.setDescription("today");
        today.setOriginalDate(new Date());
        today.setOriginalAmount(amount);
        today.setAccountId("myAccount");

        // trans2
        Transaction todayMinus1 = new Transaction();
        todayMinus1.setDescription("today-1");
        todayMinus1.setOriginalDate(DateUtils.addDays(new Date(), -1));
        todayMinus1.setOriginalAmount(amount * -1);
        todayMinus1.setAccountId("otherAccount");

        assertTrue("Transactions should be corresponding", today.canBeTwinTransfer(todayMinus1));

        today.setOriginalAmount(amount * -1);
        todayMinus1.setOriginalAmount(amount);
        assertFalse("Transactions should not be corresponding", today.canBeTwinTransfer(todayMinus1));
    }

    @Test
    public void findTransferTodayMinusTwo() {
        // trans1
        Transaction today = new Transaction();
        today.setDescription("today");
        today.setOriginalDate(new Date());
        today.setOriginalAmount(amount);
        today.setAccountId("myAccount");

        // trans2
        Transaction todayMinus2 = new Transaction();
        todayMinus2.setDescription("today-2");
        todayMinus2.setOriginalDate(DateUtils.addDays(new Date(), -2));
        todayMinus2.setOriginalAmount(amount * -1);
        todayMinus2.setAccountId("otherAccount");

        assertTrue("Transactions should be corresponding", today.canBeTwinTransfer(todayMinus2));

        todayMinus2.setOriginalAmount(amount);
        today.setOriginalAmount(amount * -1);
        assertFalse("Transactions should not be corresponding", today.canBeTwinTransfer(todayMinus2));
    }

    @Test
    public void findTransferTodayMinusThree() {
        // trans1
        Transaction today = new Transaction();
        today.setDescription("today");
        today.setOriginalDate(new Date());
        today.setOriginalAmount(amount);
        today.setAccountId("myAccount");

        // trans2
        Transaction todayMinus3 = new Transaction();
        todayMinus3.setDescription("today-3");
        todayMinus3.setOriginalDate(DateUtils.addDays(new Date(), -3));
        todayMinus3.setOriginalAmount(amount * -1);
        todayMinus3.setAccountId("otherAccount");

        assertTrue("Transactions should be corresponding", today.canBeTwinTransfer(todayMinus3));

        today.setOriginalAmount(amount * -1);
        todayMinus3.setOriginalAmount(amount);
        assertFalse("Transactions should not be corresponding", today.canBeTwinTransfer(todayMinus3));

    }

    @Test
    public void findTransferTodayPlusOne() {
        // trans1
        Transaction today = new Transaction();
        today.setDescription("today");
        today.setOriginalDate(new Date());
        today.setOriginalAmount(amount * -1);
        today.setAccountId("myAccount");

        // trans2
        Transaction todayPlusOne = new Transaction();
        todayPlusOne.setDescription("yesterday");
        todayPlusOne.setOriginalDate(DateUtils.addDays(new Date(), 1));
        todayPlusOne.setOriginalAmount(amount);
        todayPlusOne.setAccountId("otherAccount");

        assertTrue("Transactions should be corresponding", today.canBeTwinTransfer(todayPlusOne));

        today.setOriginalAmount(amount);
        todayPlusOne.setOriginalAmount(amount * -1);
        assertFalse("Transactions should not be corresponding", today.canBeTwinTransfer(todayPlusOne));
    }

    @Test
    public void findTransferTodayPlusTwo() {
        // trans1
        Transaction today = new Transaction();
        today.setDescription("today");
        today.setOriginalDate(new Date());
        today.setOriginalAmount(amount * -1);
        today.setAccountId("myAccount");

        // trans2
        Transaction todayPlusTwo = new Transaction();
        todayPlusTwo.setDescription("yesterday");
        todayPlusTwo.setOriginalDate(DateUtils.addDays(new Date(), 2));
        todayPlusTwo.setOriginalAmount(amount);
        todayPlusTwo.setAccountId("otherAccount");

        assertTrue("Transactions should be corresponding", today.canBeTwinTransfer(todayPlusTwo));

        today.setOriginalAmount(amount);
        todayPlusTwo.setOriginalAmount(amount * -1);
        assertFalse("Transactions should not be corresponding", today.canBeTwinTransfer(todayPlusTwo));
    }

    @Test
    public void findTransferTodayPlusThree() {
        // trans1
        Transaction today = new Transaction();
        today.setDescription("today");
        today.setOriginalDate(new Date());
        today.setOriginalAmount(amount * -1);
        today.setAccountId("myAccount");

        // trans2
        Transaction todayPlusThree = new Transaction();
        todayPlusThree.setDescription("yesterday");
        todayPlusThree.setOriginalDate(DateUtils.addDays(new Date(), 1));
        todayPlusThree.setOriginalAmount(amount);
        todayPlusThree.setAccountId("otherAccount");

        assertTrue("Transactions should be corresponding", today.canBeTwinTransfer(todayPlusThree));

        today.setOriginalAmount(amount);
        todayPlusThree.setOriginalAmount(amount * -1);
        assertFalse("Transactions should not be corresponding", today.canBeTwinTransfer(todayPlusThree));
    }

    @Test
    public void dontFindTransferBigAmountMoreThanLevel() {
        // trans1
        Transaction t1 = new Transaction();
        t1.setDescription("tran1");
        t1.setOriginalDate(new Date());
        t1.setOriginalAmount(10000);
        t1.setAccountId("myAccount");

        // trans2
        Transaction t2 = new Transaction();
        t2.setDescription("tran2");
        t2.setOriginalDate(new Date());
        t2.setOriginalAmount(-11000);
        t2.setAccountId("myAccount");

        assertFalse("Transactions should not be corresponding", t1.canBeTwinTransfer(t2));
        assertFalse("Transactions should not be corresponding", t2.canBeTwinTransfer(t1));

        t2.setOriginalAmount(-10600);
        assertFalse("Transactions should not be corresponding", t1.canBeTwinTransfer(t2));
        assertFalse("Transactions should not be corresponding", t2.canBeTwinTransfer(t1));
    }

    @Test
    public void findTransferBigAmountOnLevel() {
        // trans1
        Transaction t1 = new Transaction();
        t1.setDescription("tran1");
        t1.setOriginalDate(new Date());
        t1.setOriginalAmount(50001);
        t1.setAccountId("myAccount");

        // trans2
        Transaction t2 = new Transaction();
        t2.setDescription("tran2");
        t2.setOriginalDate(new Date());
        t2.setOriginalAmount(-50500);
        t2.setAccountId("myAccount");

        assertTrue("Transactions should be corresponding", t1.canBeTwinTransfer(t2));
    }

    @Test
    public void dontFindTransferBigAmountOnLevel() {
        // trans1
        Transaction t1 = new Transaction();
        t1.setDescription("tran1");
        t1.setOriginalDate(new Date());
        t1.setOriginalAmount(10000);
        t1.setAccountId("myAccount");

        // trans2
        Transaction t2 = new Transaction();
        t2.setDescription("tran2");
        t2.setOriginalDate(new Date());
        t2.setOriginalAmount(10500);
        t2.setAccountId("myAccount");

        assertFalse("Transactions should not be corresponding", t1.canBeTwinTransfer(t2));
    }

    @Test
    public void dontFindTransferOnLevel() {
        // trans1
        Transaction t1 = new Transaction();
        t1.setDescription("tran1");
        t1.setOriginalDate(new Date());
        t1.setOriginalAmount(1000);
        t1.setAccountId("otherAccount");

        // trans2
        Transaction t2 = new Transaction();
        t2.setDescription("tran2");
        t2.setOriginalDate(new Date());
        t2.setOriginalAmount(1050);
        t2.setAccountId("myAccount");

        assertFalse("Transactions should not be corresponding", t1.canBeTwinTransfer(t2));
    }

    @Test
    public void dontFindTransferOnLevel2() {
        // trans1
        Transaction t1 = new Transaction();
        t1.setDescription("tran1");
        t1.setOriginalDate(new Date());
        t1.setOriginalAmount(1000);
        t1.setAccountId("otherAccount");

        // trans2
        Transaction t2 = new Transaction();
        t2.setDescription("tran2");
        t2.setOriginalDate(new Date());
        t2.setOriginalAmount(-1050);
        t2.setAccountId("otherAccount");

        assertFalse("Transactions should not be corresponding", t1.canBeTwinTransfer(t2));
    }

    @Test
    public void findTwinMatchTest() {

        users.forEach(user ->{
        String accountId1 = StringUtils.generateUUID();
        String accountId2 = StringUtils.generateUUID();

        List<Transaction> transactions = Lists.newArrayList();
        Transaction transaction1 = testUtil.getNewTransaction(user.getId(), -3000, "Till e-konto");
        transaction1.setAccountId(accountId1);
        transaction1.setCategory(unCategorizedExpenseCategory);
        transactions.add(transaction1);

        Transaction transaction2 = testUtil.getNewTransaction(user.getId(), 3000, "Från mitt konto");
        transaction2.setAccountId(accountId2);
        transaction2.setCategory(unCategorizedIncomeCategory);
        transactions.add(transaction2);

        TransactionProcessorContext context = process(transactions, user, Lists.newArrayList(), Cluster.TINK);

        List<Transaction> transaction1Reslut = context.getTransactionsToSave().values().stream().filter(t -> t.getUserId().equals(user.getId()) &&
                t.getAccountId().equals(transaction1.getAccountId())).collect(Collectors.toList());
        List<Transaction> transaction2Reslut = context.getTransactionsToSave().values().stream().filter(t -> t.getUserId().equals(user.getId()) &&
                t.getAccountId().equals(transaction2.getAccountId())).collect(Collectors.toList());

        assertEquals(CategoryTypes.TRANSFERS, transaction1Reslut.get(0).getCategoryType());
        assertEquals(CategoryTypes.TRANSFERS, transaction2Reslut.get(0).getCategoryType());
        assertEquals(transaction1.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN),
                transaction2Reslut.get(0).getId());
        assertEquals(transaction2.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN),
                transaction1Reslut.get(0).getId());

        });
    }

    private ImmutableList<TransactionProcessorCommand> transferCommands(
            TransactionProcessorContext context, Cluster cluster) {
        return ImmutableList.of(
                new LoadUserDataCommand(
                        context, credentialsRepository, loanDataRepository, transactionDao, accountRepository),
                new TransferDetectionCommand(
                        context, categoryConfiguration,
                        TransferDetectionScorerFactory.byCluster(cluster),
                        new ClusterCategories(categoryRepository.findAll()),
                        categoryChangeRecordDao
                ),
                new PrepareTransactionsToSaveAndDeleteCommand(context, metricRegistry),
                new SaveTransactionCommand(context, transactionDao, metricRegistry)
        );
    }


    /**
     * Verifies that we can handle several tranfers in the same batch
     */
    @Test
    public void findDoubleTwinMatchTest() {

        users.forEach(user -> {
            String accountId1 = StringUtils.generateUUID();
            String accountId2 = StringUtils.generateUUID();

            List<Transaction> transactions = Lists.newArrayList();
            Transaction transaction1 = testUtil.getNewTransaction(user.getId(), -3000, "Till e-konto");
            transaction1.setAccountId(accountId1);
            transaction1.setCategory(unCategorizedExpenseCategory);
            transactions.add(transaction1);

            Transaction transaction2 = testUtil.getNewTransaction(user.getId(), 3000, "Från mitt konto");
            transaction2.setAccountId(accountId2);
            transaction2.setCategory(unCategorizedIncomeCategory);
            transactions.add(transaction2);

            Transaction transaction3 = testUtil.getNewTransaction(user.getId(), -2000, "Till e-konto ABC");
            transaction3.setAccountId(accountId1);
            transaction3.setCategory(unCategorizedExpenseCategory);
            transactions.add(transaction3);

            Transaction transaction4 = testUtil.getNewTransaction(user.getId(), 2000, "Från mitt konto ABC");
            transaction4.setAccountId(accountId2);
            transaction4.setCategory(unCategorizedIncomeCategory);
            transactions.add(transaction4);

            TransactionProcessorContext context = process(transactions, user, Lists.newArrayList(), Cluster.TINK);

            Transaction result1 = context.getTransactionsToSave().values().stream().filter(t -> t.getUserId().equals(user.getId()) &&
                    t.getId().equals(transaction1.getId())).findFirst().get();
            Transaction result2 = context.getTransactionsToSave().values().stream().filter(t -> t.getUserId().equals(user.getId()) &&
                    t.getId().equals(transaction2.getId())).findFirst().get();
            Transaction result3 = context.getTransactionsToSave().values().stream().filter(t -> t.getUserId().equals(user.getId()) &&
                    t.getId().equals(transaction3.getId())).findFirst().get();
            Transaction result4 = context.getTransactionsToSave().values().stream().filter(t -> t.getUserId().equals(user.getId()) &&
                    t.getId().equals(transaction4.getId())).findFirst().get();

            assertNotNull(result1);
            assertNotNull(result2);
            assertNotNull(result3);
            assertNotNull(result4);

            assertEquals(CategoryTypes.TRANSFERS, result1.getCategoryType());
            assertEquals(CategoryTypes.TRANSFERS, result2.getCategoryType());
            assertEquals(CategoryTypes.TRANSFERS, result3.getCategoryType());
            assertEquals(CategoryTypes.TRANSFERS, result4.getCategoryType());

            assertEquals(result1.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN), result2.getId());
            assertEquals(result2.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN), result1.getId());

            assertEquals(result3.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN), result4.getId());
            assertEquals(result4.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN), result3.getId());

        });

    }

    @Test
    public void findBestTwinMatchTestOfTwo() {

        users.forEach(user -> {
            String accountId1 = StringUtils.generateUUID();
            String accountId2 = StringUtils.generateUUID();

            List<Transaction> transactions = Lists.newArrayList();
            Transaction transaction1 = testUtil.getNewTransaction(user.getId(), -3000, "Gemensamt");
            transaction1.setAccountId(accountId1);
            transaction1.setCategory(unCategorizedExpenseCategory);
            transactions.add(transaction1);

            Transaction transaction2 = testUtil.getNewTransaction(user.getId(), 3000, "Vårt konto");
            transaction2.setAccountId(accountId2);
            transaction2.setCategory(unCategorizedIncomeCategory);
            transactions.add(transaction2);

            Transaction transaction3 = testUtil.getNewTransaction(user.getId(), 3000, "Gemensamt");
            transaction3.setAccountId(accountId2);
            transaction3.setCategory(unCategorizedIncomeCategory);
            transactions.add(transaction3);

            TransactionProcessorContext context = process(transactions, user, Lists.newArrayList(), Cluster.TINK);

            List<Transaction> account1Reslut = context.getTransactionsToSave().values().stream().filter(t -> t.getUserId().equals(user.getId()) &&
                    t.getAccountId().equals(transaction1.getAccountId())).collect(Collectors.toList());
            List<Transaction> account2Reslut = context.getTransactionsToSave().values().stream().filter(t -> t.getUserId().equals(user.getId()) &&
                    t.getAccountId().equals(transaction2.getAccountId())).collect(Collectors.toList());;

            Transaction transaction2Result = null;
            Transaction transaction3Result = null;
            for (Transaction t : account2Reslut) {
                if (t.getOriginalDescription().equals("Vårt konto")) {
                    transaction2Result = t;
                } else if (t.getOriginalDescription().equals("Gemensamt")) {
                    transaction3Result = t;
                }
            }
            assertNotNull(transaction2Result);
            assertNotNull(transaction3Result);

            assertEquals(transaction1.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN),
                    transaction3Result.getId());
            assertEquals(transaction3.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN),
                    account1Reslut.get(0).getId());
            assertNull(transaction2Result.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN));
            assertEquals(CategoryTypes.TRANSFERS, transaction3Result.getCategoryType());

        });
    }

    @Test
    public void findBestTwinMatchOfTwoWithBetterDate() {

        users.forEach(user -> {
            String accountId1 = StringUtils.generateUUID();
            String accountId2 = StringUtils.generateUUID();

            List<Transaction> transactions = Lists.newArrayList();
            Transaction transaction1 = testUtil.getNewTransaction(user.getId(), -3000, "Gemensamt");
            transaction1.setAccountId(accountId1);
            transaction1.setCategory(unCategorizedExpenseCategory);
            transaction1.setOriginalDate(new Date());
            transactions.add(transaction1);

            Transaction transaction2 = testUtil.getNewTransaction(user.getId(), 3000, "Vårt konto");
            transaction2.setAccountId(accountId2);
            transaction2.setCategory(unCategorizedIncomeCategory);
            transaction2.setOriginalDate(DateUtils.addDays(new Date(), 1));
            transactions.add(transaction2);

            Transaction transaction3 = testUtil.getNewTransaction(user.getId(), 3000, "Gemensamt");
            transaction3.setAccountId(accountId2);
            transaction3.setCategory(unCategorizedIncomeCategory);
            transaction3.setOriginalDate(DateUtils.addDays(new Date(), 2));
            transactions.add(transaction3);

            TransactionProcessorContext context = process(transactions, user, Lists.newArrayList(), Cluster.TINK);

            List<Transaction> account1Reslut = context.getTransactionsToSave().values().stream().filter(t -> t.getUserId().equals(user.getId()) &&
                    t.getAccountId().equals(transaction1.getAccountId())).collect(Collectors.toList());
            List<Transaction> account2Reslut = context.getTransactionsToSave().values().stream().filter(t -> t.getUserId().equals(user.getId()) &&
                    t.getAccountId().equals(transaction2.getAccountId())).collect(Collectors.toList());

            Transaction transaction2Result = null;
            Transaction transaction3Result = null;
            for (Transaction t : account2Reslut) {
                if (t.getOriginalDescription().equals("Vårt konto")) {
                    transaction2Result = t;
                } else if (t.getOriginalDescription().equals("Gemensamt")) {
                    transaction3Result = t;
                }
            }
            assertNotNull(transaction2Result);
            assertNotNull(transaction3Result);

            assertEquals(transaction1.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN),
                    transaction2Result.getId());
            assertEquals(transaction2.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN),
                    account1Reslut.get(0).getId());
            assertNull(transaction3Result.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN));
            assertEquals(CategoryTypes.TRANSFERS, transaction2Result.getCategoryType());

        });
    }

    @Test
    public void findBestTwinMatchOfTwoWithBetterDate2() {
        users.forEach(user -> {
            String accountId1 = StringUtils.generateUUID();
            String accountId2 = StringUtils.generateUUID();

            List<Transaction> transactions = Lists.newArrayList();
            Transaction transaction1 = testUtil.getNewTransaction(user.getId(), -3000, "Gemensamt");
            transaction1.setAccountId(accountId1);
            transaction1.setCategory(unCategorizedExpenseCategory);
            transaction1.setOriginalDate(new Date());
            transactions.add(transaction1);

            Transaction transaction2 = testUtil.getNewTransaction(user.getId(), 3000, "Vårt konto");
            transaction2.setAccountId(accountId2);
            transaction2.setCategory(unCategorizedIncomeCategory);
            transaction2.setOriginalDate(DateUtils.addDays(new Date(), 2));
            transactions.add(transaction2);

            Transaction transaction3 = testUtil.getNewTransaction(user.getId(), 3000, "Gemensamt");
            transaction3.setAccountId(accountId2);
            transaction3.setCategory(unCategorizedIncomeCategory);
            transaction3.setOriginalDate(DateUtils.addDays(new Date(), 1));
            transactions.add(transaction3);

            TransactionProcessorContext context = process(transactions, user, Lists.newArrayList(), Cluster.TINK);

            List<Transaction> account1Reslut = context.getTransactionsToSave().values().stream().filter(t -> t.getUserId().equals(user.getId()) &&
                    t.getAccountId().equals(transaction1.getAccountId())).collect(Collectors.toList());
            List<Transaction> account2Reslut = context.getTransactionsToSave().values().stream().filter(t -> t.getUserId().equals(user.getId()) &&
                    t.getAccountId().equals(transaction2.getAccountId())).collect(Collectors.toList());

            Transaction transaction2Result = null;
            Transaction transaction3Result = null;
            for (Transaction t : account2Reslut) {
                assertNotNull(t);
                if (t.getOriginalDescription().equals("Vårt konto")) {
                    transaction2Result = t;
                } else if (t.getOriginalDescription().equals("Gemensamt")) {
                    transaction3Result = t;
                }
            }
            assertNotNull(transaction2Result);
            assertNotNull(transaction3Result);

            assertEquals(transaction1.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN),
                    transaction3Result.getId());
            assertEquals(transaction3.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN),
                    account1Reslut.get(0).getId());
            assertNull(transaction2Result.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN));
            assertEquals(CategoryTypes.TRANSFERS, transaction3Result.getCategoryType());

        });
    }

    @Test
    public void findTransferTwoCredentials() {

        users.forEach(user -> {
            String accountId1 = StringUtils.generateUUID();
            String accountId2 = StringUtils.generateUUID();

            List<Transaction> transactions = Lists.newArrayList();
            Transaction transaction1 = testUtil.getNewTransaction(user.getId(), -3000, "Betalning");
            transaction1.setAccountId(accountId1);
            transaction1.setCredentialsId(swedbankCredential.get(user.getId()).getId());
            transaction1.setCategory(unCategorizedExpenseCategory);
            transactions.add(transaction1);

            TransactionProcessorContext firstContext = process(transactions, user, Lists.newArrayList(), Cluster.TINK);
            transactions.clear();

            Transaction transaction2 = testUtil.getNewTransaction(user.getId(), 3000, "AmEx");
            transaction2.setAccountId(accountId2);
            transaction2.setCredentialsId(handelsbankenCredential.get(user.getId()).getId());
            transaction2.setCategory(unCategorizedIncomeCategory);
            transactions.add(transaction2);
            List<Transaction> inStore = firstContext.getTransactionsToSave().values().stream().collect(Collectors.toList());
            TransactionProcessorContext secondContext = process(transactions, user, inStore, Cluster.TINK);

            List<Transaction> account1Reslut = secondContext.getTransactionsToSave().values().stream().filter(t -> t.getUserId().equals(user.getId()) &&
                    t.getAccountId().equals(transaction1.getAccountId())).collect(Collectors.toList());
            List<Transaction> account2Reslut = secondContext.getTransactionsToSave().values().stream().filter(t -> t.getUserId().equals(user.getId()) &&
                    t.getAccountId().equals(transaction2.getAccountId())).collect(Collectors.toList());

            Transaction transaction1Result = null;
            Transaction transaction2Result = null;
            for (Transaction t : account1Reslut) {
                if (t.getOriginalDescription().equals("Betalning")) {
                    transaction1Result = t;
                }
            }
            for (Transaction t : account2Reslut) {
                if (t.getOriginalDescription().equals("AmEx")) {
                    transaction2Result = t;
                }
            }
            assertNotNull(transaction1Result);
            assertNotNull(transaction2Result);

            assertEquals(transaction2.getId(),
                    transaction1Result.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN));
            assertEquals(CategoryTypes.TRANSFERS, transaction1Result.getCategoryType());

            assertEquals(transaction1.getId(),
                    transaction2Result.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN));
            assertEquals(CategoryTypes.TRANSFERS, transaction2Result.getCategoryType());

        });
    }

    @Test
    public void findTransferInSameDayIncomeEarlierDate() {
        Category restaurant = categoryRepository.findAll().stream().filter(c -> c.getCode().equals(SECategories.Codes.EXPENSES_FOOD_RESTAURANTS))
                .findFirst().get();
        when(categoryRepository.findByCode(SECategories.Codes.EXPENSES_FOOD_RESTAURANTS)).thenReturn(restaurant);

        users.forEach(user -> {
            String accountId1 = StringUtils.generateUUID();
            String accountId2 = StringUtils.generateUUID();

            List<Transaction> transactions = Lists.newArrayList();
            Transaction transferIncome = testUtil.getNewTransaction(user.getId(), 1000, "Enkla Sparko");
            transferIncome.setAccountId(accountId1);
            transferIncome.setCredentialsId(swedbankCredential.get(user.getId()).getId());
            transferIncome.setCategory(unCategorizedIncomeCategory);
            transactions.add(transferIncome);

            TransactionProcessorContext firstContext = process(transactions, user, Lists.newArrayList(), Cluster.TINK);
            transactions.clear();

            Transaction noTransferExpense = testUtil.getNewTransaction(user.getId(), -90, "Tashi Sylten");
            noTransferExpense.setAccountId(accountId2);
            noTransferExpense.setCredentialsId(handelsbankenCredential.get(user.getId()).getId());
            noTransferExpense.setCategory(
                    categoryRepository.findByCode(categoryConfiguration.getRestaurantsCode()));
            transactions.add(noTransferExpense);

            Transaction transferExpense = testUtil.getNewTransaction(user.getId(), -1000, "Sbab-konto");
            transferExpense.setAccountId(accountId2);
            transferExpense.setCredentialsId(handelsbankenCredential.get(user.getId()).getId());
            transferExpense.setCategory(unCategorizedExpenseCategory);
            transferExpense.setOriginalDate(
                    se.tink.libraries.date.DateUtils.setInclusiveEndTime(transferExpense.getOriginalDate()));
            transactions.add(transferExpense);
            List<Transaction> inStore = firstContext.getTransactionsToSave().values().stream().collect(Collectors.toList());
            TransactionProcessorContext secondContext = process(transactions, user, inStore, Cluster.TINK);

            Transaction transferIncomeResult = secondContext.getTransactionsToSave().values().stream().filter(t -> t.getUserId().equals(user.getId()) &&
                    t.getId().equals(transferIncome.getId())).findFirst().get();
            Transaction transferExpenseResult =secondContext.getTransactionsToSave().values().stream().filter(t -> t.getUserId().equals(user.getId()) &&
                    t.getId().equals(transferExpense.getId())).findFirst().get();

            assertNotNull(transferIncomeResult);
            assertNotNull(transferExpenseResult);

            assertEquals(transferExpense.getId(),
                    transferIncomeResult.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN));
            assertEquals(CategoryTypes.TRANSFERS, transferIncomeResult.getCategoryType());

            assertEquals(transferIncome.getId(),
                    transferExpenseResult.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN));
            assertEquals(CategoryTypes.TRANSFERS, transferExpenseResult.getCategoryType());

        });
    }

    @Test
    public void findTransferTwoBestDateCredentials() {

        users.forEach(user -> {
            String accountId1 = StringUtils.generateUUID();
            String accountId2 = StringUtils.generateUUID();
            String accountId3 = StringUtils.generateUUID();

            List<Transaction> transactions = Lists.newArrayList();
            Transaction transaction1 = testUtil.getNewTransaction(user.getId(), -3000, "Betalning");
            transaction1.setAccountId(accountId1);
            transaction1.setCredentialsId(swedbankCredential.get(user.getId()).getId());
            transaction1.setOriginalDate(new Date());
            transaction1.setCategory(unCategorizedExpenseCategory);
            transactions.add(transaction1);

            TransactionProcessorContext firstContext = process(transactions, user, Lists.newArrayList(), Cluster.TINK);
            transactions.clear();

            Transaction transaction2 = testUtil.getNewTransaction(user.getId(), 3000, "AmEx");
            transaction2.setAccountId(accountId2);
            transaction2.setCredentialsId(handelsbankenCredential.get(user.getId()).getId());
            transaction2.setCategory(unCategorizedIncomeCategory);
            transaction2.setOriginalDate(DateUtils.addDays(new Date(), 3));
            transactions.add(transaction2);
            List<Transaction> inStore = firstContext.getTransactionsToSave().values().stream().collect(Collectors.toList());
            TransactionProcessorContext secondContext = process(transactions, user, inStore, Cluster.TINK);

            transactions.clear();

            Transaction transaction3 = testUtil.getNewTransaction(user.getId(), 3000, "AmEx");
            transaction3.setAccountId(accountId3);
            transaction3.setCredentialsId(nordeaCredential.get(user.getId()).getId());
            transaction3.setCategory(unCategorizedIncomeCategory);
            transaction3.setOriginalDate(DateUtils.addDays(new Date(), 2));
            transactions.add(transaction3);
            List<Transaction> secondInStore = secondContext.getTransactionsToSave().values().stream().collect(Collectors.toList());
            TransactionProcessorContext thirdContext = process(transactions, user, secondInStore, Cluster.TINK);

            List<Transaction> account1Reslut = thirdContext.getTransactionsToSave().values().stream().filter(t -> t.getUserId().equals(user.getId()) &&
                    t.getAccountId().equals(transaction1.getAccountId())).collect(Collectors.toList());
            List<Transaction> account2Reslut = thirdContext.getTransactionsToSave().values().stream().filter(t -> t.getUserId().equals(user.getId()) &&
                    t.getAccountId().equals(transaction2.getAccountId())).collect(Collectors.toList());
            List<Transaction> account3Reslut = thirdContext.getTransactionsToSave().values().stream().filter(t -> t.getUserId().equals(user.getId()) &&
                    t.getAccountId().equals(transaction3.getAccountId())).collect(Collectors.toList());

            Transaction transaction1Result = null;
            Transaction transaction2Result = null;
            Transaction transaction3Result = null;
            for (Transaction t : account1Reslut) {
                if (t.getOriginalDescription().equals("Betalning")) {
                    transaction1Result = t;
                }
            }
            for (Transaction t : account2Reslut) {
                if (t.getOriginalDescription().equals("AmEx")) {
                    transaction2Result = t;
                }
            }
            for (Transaction t : account3Reslut) {
                if (t.getOriginalDescription().equals("AmEx")) {
                    transaction3Result = t;
                }
            }
            assertNotNull(transaction1Result);
            assertNotNull(transaction2Result);
            assertNotNull(transaction3Result);

            assertEquals(transaction3.getId(),
                    transaction1Result.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN));
            assertEquals(CategoryTypes.TRANSFERS, transaction1Result.getCategoryType());

            assertEquals(transaction1.getId(),
                    transaction3Result.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN));
            assertEquals(CategoryTypes.TRANSFERS, transaction3Result.getCategoryType());

            assertNull(transaction2Result.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN));
            assertEquals(CategoryTypes.INCOME, transaction2Result.getCategoryType());

        });

    }

    @Test
    public void findBestTwinMatchTestOfThree() {

        users.forEach(user -> {
            String accountId1 = StringUtils.generateUUID();
            String accountId2 = StringUtils.generateUUID();

            List<Transaction> transactions = Lists.newArrayList();
            Transaction transaction1 = testUtil.getNewTransaction(user.getId(), -3000, "Gemensamt");
            transaction1.setAccountId(accountId1);
            transaction1.setCategory(unCategorizedExpenseCategory);
            transactions.add(transaction1);

            Transaction transaction2 = testUtil.getNewTransaction(user.getId(), 3000, "Vårt konto");
            transaction2.setAccountId(accountId2);
            transaction2.setCategory(unCategorizedIncomeCategory);
            transactions.add(transaction2);

            Transaction transaction3 = testUtil.getNewTransaction(user.getId(), 3000, "Gemensamt");
            transaction3.setAccountId(accountId2);
            transaction3.setCategory(unCategorizedIncomeCategory);
            transactions.add(transaction3);

            Transaction transaction4 = testUtil.getNewTransaction(user.getId(), 3000, "Gemensamt konto");
            transaction4.setAccountId(accountId2);
            transaction4.setCategory(unCategorizedIncomeCategory);
            transactions.add(transaction4);

            TransactionProcessorContext context = process(transactions, user, Lists.newArrayList(), Cluster.TINK);

            List<Transaction> account1Reslut = context.getTransactionsToSave().values().stream().filter(t -> t.getUserId().equals(user.getId()) &&
                    t.getAccountId().equals(transaction1.getAccountId())).collect(Collectors.toList());
            List<Transaction> account2Reslut = context.getTransactionsToSave().values().stream().filter(t -> t.getUserId().equals(user.getId()) &&
                    t.getAccountId().equals(transaction2.getAccountId())).collect(Collectors.toList());

            Transaction transaction2Result = null;
            Transaction transaction3Result = null;
            Transaction transaction4Result = null;

            for (Transaction t : account2Reslut) {
                if (t.getOriginalDescription().equals("Vårt konto")) {
                    transaction2Result = t;
                }
                if (t.getOriginalDescription().equals("Gemensamt")) {
                    transaction3Result = t;
                }
                if (t.getOriginalDescription().equals("Gemensamt konto")) {
                    transaction4Result = t;
                }
            }
            assertNotNull(transaction2Result);
            assertNotNull(transaction3Result);
            assertNotNull(transaction4Result);

            assertEquals(CategoryTypes.TRANSFERS, transaction3Result.getCategoryType());

            assertEquals(transaction1.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN),
                    transaction3Result.getId());
            assertEquals(transaction3.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN),
                    account1Reslut.get(0).getId());
            assertNull(transaction2Result.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN));

        });
    }

    @Test
    public void findBigTransactionsSameAccount() {
        users.forEach(user -> {
            String accountId = StringUtils.generateUUID();

            List<Transaction> transactions = Lists.newArrayList();
            Transaction transaction1 = testUtil.getNewTransaction(user.getId(), 300000, "Swedbank Lån");
            transaction1.setAccountId(accountId);
            transaction1.setCategory(unCategorizedIncomeCategory);
            transactions.add(transaction1);

            Transaction transaction2 = testUtil.getNewTransaction(user.getId(), -300000, "Lägenhetsbetalning");
            transaction2.setAccountId(accountId);
            transaction2.setCategory(unCategorizedExpenseCategory);
            transactions.add(transaction2);

            TransactionProcessorContext context = process(transactions, user, Lists.newArrayList(), Cluster.TINK);

            List<Transaction> transaction1Reslut = context.getTransactionsToSave().values().stream().filter(t -> t.getUserId().equals(user.getId()) &&
                    t.getAccountId().equals(transaction1.getAccountId())).collect(Collectors.toList());
            boolean foundT1 = false;
            boolean foundT2 = false;

            for (Transaction t : transaction1Reslut) {
                if (t.getDescription().equals("Swedbank Lån")) {
                    assertEquals("Wrong category type", CategoryTypes.TRANSFERS, t.getCategoryType());
                    foundT1 = true;
                }
                if (t.getDescription().equals("Lägenhetsbetalning")) {
                    assertEquals("Wrong category type", CategoryTypes.TRANSFERS, t.getCategoryType());
                    foundT2 = true;
                }
            }
            assertTrue("Did not find transactions", foundT1 && foundT2);

        });
    }

    @Test
    public void dontFindBigTransactionsSameAccount() {

        users.forEach(user -> {
            String accountId = StringUtils.generateUUID();

            List<Transaction> transactions = Lists.newArrayList();
            Transaction transaction1 = testUtil.getNewTransaction(user.getId(), 300000, "Swedbank Lån");
            transaction1.setAccountId(accountId);
            transaction1.setCategory(unCategorizedIncomeCategory);
            transactions.add(transaction1);

            Transaction transaction2 = testUtil.getNewTransaction(user.getId(), 300000, "Lägenhetsbetalning");
            transaction2.setAccountId(accountId);
            transaction2.setCategory(unCategorizedIncomeCategory);
            transactions.add(transaction2);

            TransactionProcessorContext context = process(transactions, user, Lists.newArrayList(), Cluster.TINK);

            List<Transaction> transaction1Reslut = context.getTransactionsToSave().values().stream().filter(t -> t.getUserId().equals(user.getId()) &&
                    t.getAccountId().equals(transaction1.getAccountId())).collect(Collectors.toList());
            boolean foundT1 = false;
            boolean foundT2 = false;

            for (Transaction t : transaction1Reslut) {
                if (t.getDescription().equals("Swedbank Lån")) {
                    assertEquals("Wrong category type", CategoryTypes.INCOME, t.getCategoryType());
                    foundT1 = true;
                }
                if (t.getDescription().equals("Lägenhetsbetalning")) {
                    assertEquals("Wrong category type", CategoryTypes.INCOME, t.getCategoryType());
                    foundT2 = true;
                }
            }
            assertTrue("Did not find transactions", foundT1 && foundT2);

        });
    }

    @Test
    public void shouldUpdateWithBetterMatchIfFoundInASecondImport() {
        users.forEach(user -> {
            String accountId1 = StringUtils.generateUUID();
            String accountId2 = StringUtils.generateUUID();

            List<Transaction> transactions = Lists.newArrayList();
            Transaction transaction1 = testUtil.getNewTransaction(user.getId(), 10000,
                    "A weird description that shouldn't get high score in Jaro Winkler");
            transaction1.setAccountId(accountId2);
            transaction1.setCategory(unCategorizedIncomeCategory);

            transactions.add(transaction1);

            Transaction transaction2 = testUtil.getNewTransaction(user.getId(), -10000, "Buy a car: 123-456");
            transaction2.setAccountId(accountId1);
            transaction2.setCategory(unCategorizedExpenseCategory);
            transactions.add(transaction2);

            TransactionProcessorContext context = process(transactions, user, Lists.newArrayList(), Cluster.TINK);

            // Verify that the transactions has been matched
            assertEquals(transaction1.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN), transaction2.getId());
            assertEquals(transaction2.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN), transaction1.getId());

            Transaction transaction3 = testUtil.getNewTransaction(user.getId(), 10000, "Buy a car: 987-244");
            transaction3.setAccountId(accountId2);
            transaction3.setCategory(unCategorizedIncomeCategory);
            transactions.add(transaction3);
            List<Transaction> inStore = context.getTransactionsToSave().values().stream().collect(Collectors.toList());
            process(transactions, user, inStore, Cluster.TINK);

            // Verify that the transaction has been replaced
            assertEquals(transaction3.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN), transaction2.getId());
            assertEquals(transaction2.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN), transaction3.getId());
            assertNull(transaction1.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN));

        });

    }

    @Test
    public void shouldKeepBestMatchDuringSecondImport() {
        users.forEach(user -> {
            String accountId1 = StringUtils.generateUUID();
            String accountId2 = StringUtils.generateUUID();

            List<Transaction> transactions = Lists.newArrayList();
            Transaction transaction1 = testUtil.getNewTransaction(user.getId(), 10000, "Buy a car: 987-244");
            transaction1.setAccountId(accountId2);
            transaction1.setCategory(unCategorizedIncomeCategory);

            transactions.add(transaction1);

            Transaction transaction2 = testUtil.getNewTransaction(user.getId(), -10000, "Buy a car: 123-456");
            transaction2.setAccountId(accountId1);
            transaction2.setCategory(unCategorizedExpenseCategory);
            transactions.add(transaction2);

            TransactionProcessorContext context = process(transactions, user, Lists.newArrayList(), Cluster.TINK);

            // Verify that the transactions has been matched
            assertEquals(transaction1.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN), transaction2.getId());
            assertEquals(transaction2.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN), transaction1.getId());

            Transaction transaction3 = testUtil.getNewTransaction(user.getId(), 10000,
                    "A weird description that shouldn't get high score in Jaro Winkler");
            transaction3.setAccountId(accountId2);
            transaction3.setCategory(unCategorizedIncomeCategory);
            transactions.add(transaction3);
            List<Transaction> inStore = context.getTransactionsToSave().values().stream().collect(Collectors.toList());
            process(transactions, user, inStore, Cluster.TINK);

            // Verify that the transaction has been replaced
            assertEquals(transaction1.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN), transaction2.getId());
            assertEquals(transaction2.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN), transaction1.getId());
            assertNull(transaction3.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN));

        });

    }

    @Test
    public void shouldMatchTransfersForManyTransferOnTheSameDay() {
        users.forEach(user -> {
            String accountId1 = StringUtils.generateUUID();
            String accountId2 = StringUtils.generateUUID();

            List<Transaction> transactions = Lists.newArrayList();

            String description = "Tink Transfer";

            final int numberOfTransfers = 50;

            for (int i = 0; i < numberOfTransfers; i++) {

                Transaction transaction1 = testUtil.getNewTransaction(user.getId(), -10, description);
                transaction1.setAccountId(accountId1);
                transaction1.setCredentialsId(swedbankCredential.get(user.getId()).getId());
                transaction1.setCategory(unCategorizedExpenseCategory);
                transactions.add(transaction1);

                Transaction transaction2 = testUtil.getNewTransaction(user.getId(), 10, description);
                transaction2.setAccountId(accountId2);
                transaction2.setCredentialsId(swedbankCredential.get(user.getId()).getId());
                transaction2.setCategory(unCategorizedIncomeCategory);
                transactions.add(transaction2);
            }

            TransactionProcessorContext context = process(transactions, user, Lists.newArrayList(), Cluster.TINK);

            List<Transaction> result = context.getTransactionsToSave().values().stream().collect(Collectors.toList());

            assertNotNull(result);

            // All transactions should be transfers
            for (Transaction transaction : result) {
                if (transaction.getDescription().equalsIgnoreCase(description)) {
                    assertEquals(CategoryTypes.TRANSFERS, transaction.getCategoryType());
                }
            }

        });
    }

    @Test
    public void abnAmroTransferTest() {
        users.forEach(user -> {
            Account accountWithoutIban1 = new Account();
            Account accountWithoutIban2 = new Account();
            Account accountWithIban = new Account();

            accountWithoutIban1.setUserId(user.getId());
            accountWithoutIban2.setUserId(user.getId());
            accountWithIban.setUserId(user.getId());

            accountWithIban.setPayload("{\"iban\":\"dummy-iban\"}");

            ArrayList<Account> accounts = Lists.newArrayList(accountWithoutIban1, accountWithoutIban2, accountWithIban);

            List<Transaction> transactions = Lists.newArrayList();

            Transaction sourceTransaction = testUtil.getNewTransaction(user.getId(), -3000, "Beskrivning");
            sourceTransaction.setAccountId(accountWithoutIban1.getId());
            sourceTransaction.setCategory(unCategorizedExpenseCategory);
            sourceTransaction
                    .setInternalPayload(AbnAmroUtils.InternalPayloadKeys.DESCRIPTION_LINES, "[\"iban: dummy-iban\"]");
            transactions.add(sourceTransaction);

            Transaction destinationTransactionWithoutIban = testUtil.getNewTransaction(user.getId(), 3000, "Beskriving");
            destinationTransactionWithoutIban.setAccountId(accountWithoutIban2.getId());
            destinationTransactionWithoutIban.setCategory(unCategorizedIncomeCategory);
            transactions.add(destinationTransactionWithoutIban);

            Transaction destinationTransactionWithIban = testUtil.getNewTransaction(user.getId(), 3000, "Text text text text");
            destinationTransactionWithIban.setAccountId(accountWithIban.getId());
            destinationTransactionWithIban.setCategory(unCategorizedIncomeCategory);
            transactions.add(destinationTransactionWithIban);

            TransactionProcessorContext context = process(transactions, user, Lists.newArrayList(), Cluster.ABNAMRO, accounts);

            // Transaction with iban should be picked before transaction without iban since it is to this account
            // the transfer is made

            List<Transaction> sourceResult = context.getTransactionsToSave().values().stream().filter(t -> t.getUserId().equals(user.getId()) &&
                    t.getAccountId().equals(sourceTransaction.getAccountId())).collect(Collectors.toList());
            List<Transaction> destinationResult = context.getTransactionsToSave().values().stream().filter(t -> t.getUserId().equals(user.getId()) &&
                    t.getAccountId().equals(destinationTransactionWithIban.getAccountId())).collect(Collectors.toList());

            assertEquals(sourceTransaction.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN),
                    destinationResult.get(0).getId());
            assertEquals(destinationTransactionWithIban.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN),
                    sourceResult.get(0).getId());
        });
    }

    /**
     * Test case for https://trello.com/c/ZPas9aFf/53-transfer-detection-command-matches-wrong-transfers
     */
    @Test
    public void transfersShouldBeMatchedInPairs() {
        users.forEach(user -> {
            Transaction toSparUrban = testUtil.getNewTransaction(user.getId(), 1500, "Sparande");
            toSparUrban.setAccountId(StringUtils.generateUUID());
            toSparUrban.setCredentialsId(swedbankCredential.get(user.getId()).getId());
            toSparUrban.setCategory(unCategorizedIncomeCategory);

            Transaction toSparJeanette = testUtil.getNewTransaction(user.getId(), 1500, "Sparande");
            toSparJeanette.setAccountId(StringUtils.generateUUID());
            toSparJeanette.setCredentialsId(handelsbankenCredential.get(user.getId()).getId());
            toSparJeanette.setCategory(unCategorizedIncomeCategory);

            Transaction toOmbyggnad = testUtil.getNewTransaction(user.getId(), 1500, "Insättning från annan bank");
            toOmbyggnad.setAccountId(StringUtils.generateUUID());
            toOmbyggnad.setCredentialsId(nordeaCredential.get(user.getId()).getId());
            toOmbyggnad.setCategory(unCategorizedIncomeCategory);

            Transaction fromUrbanPrivat = testUtil.getNewTransaction(user.getId(), -1500, "Sparande");
            fromUrbanPrivat.setAccountId(StringUtils.generateUUID());
            fromUrbanPrivat.setCredentialsId(swedbankCredential.get(user.getId()).getId());
            fromUrbanPrivat.setCategory(unCategorizedExpenseCategory);

            Transaction fromJeanettePrivat = testUtil.getNewTransaction(user.getId(), -1500, "Sparande");
            fromJeanettePrivat.setAccountId(StringUtils.generateUUID());
            fromJeanettePrivat.setCredentialsId(handelsbankenCredential.get(user.getId()).getId());
            fromJeanettePrivat.setCategory(unCategorizedExpenseCategory);

            Map<String, Transaction> transactions = ImmutableMap.<String, Transaction>builder()
                    .put(toSparUrban.getId(), toSparUrban)
                    .put(toSparJeanette.getId(), toSparJeanette)
                    .put(toOmbyggnad.getId(), toOmbyggnad)
                    .put(fromUrbanPrivat.getId(), fromUrbanPrivat)
                    .put(fromJeanettePrivat.getId(), fromJeanettePrivat)
                    .build();

            process(Lists.newArrayList(transactions.values()), user, Lists.newArrayList(), Cluster.TINK);

            assertNull(toOmbyggnad.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN));
            assertEquals(
                    toSparUrban.getId(),
                    transactions.get(toSparUrban.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN))
                            .getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN));
            assertEquals(
                    toSparJeanette.getId(),
                    transactions.get(toSparJeanette.getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN))
                            .getPayloadValue(TransactionPayloadTypes.TRANSFER_TWIN));

        });
    }
}
