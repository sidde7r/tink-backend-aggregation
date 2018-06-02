package se.tink.backend.system.workers.processor.deduplication;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.common.dao.CategoryChangeRecordDao;
import se.tink.backend.connector.rpc.seb.PartnerTransactionPayload;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.system.workers.processor.TransactionProcessorContext;
import se.tink.backend.system.workers.processor.TransactionProcessorUserData;
import se.tink.backend.system.workers.processor.seb.SEBPendingTransactionCommand;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;

/**
 * TODO this is a unit test
 */
public class SEBPendingTransactionCommandTest {
    private static String TRANSACTION_UUID_PREFIX = "0000000000000000000000000000";
    private static String CATEGORY_ID = "11111111111111111111111111111111";
    private static String CATEGORY_ID2 = "33333333333333333333333333333333";
    private static String PENDING_CATEGORY_ID_PREFIX = "2222222222222222222222222222222";
    private static String USER_ID = "99999999999999999999999999999999";

    private static TransactionProcessorUserData userData;
    private User user;

    @Before
    public void before() {
        userData = Mockito.mock(TransactionProcessorUserData.class);

        user = new User();
    }

    private TransactionProcessorContext context(List<Transaction> inBatch) {
        TransactionProcessorContext context = new TransactionProcessorContext(
                user,
                ImmutableMap.of(),
                inBatch
        );
        context.setUserData(userData);
        return context;
    }

    private static HashMap<String, Transaction> getInStoreTestTransactions(int nbrOfPending) {
        HashMap<String, Transaction> transactions = Maps.newHashMap();

        for (int i = 0; i < 5; i++) {
            Transaction t = createTestTransaction("booked" + i, "Mat", -99, 1, false);
            transactions.put(t.getId(), t);
        }

        for (int i = 0; i < nbrOfPending; i++) {
            Transaction t = createTestTransaction("pending" + i, "Mat", -99, -i, true);
            transactions.put(t.getId(), t);
        }

        return transactions;
    }

    private HashMap<String, Transaction> getPendingTransactionsWithCategoryAndReservationId(int nbrOfPending) {
        HashMap<String, Transaction> transactions = Maps.newHashMap();

        for (int i = 0; i < nbrOfPending; i++) {
            Transaction t = createTestTransaction("pending" + i, "Mat", -99, -i, true);
            setSebPayloadOnTransaction(t, PartnerTransactionPayload.createFromReservationIds("reservationId" + i));
            t.setCategoryId(PENDING_CATEGORY_ID_PREFIX + i);
            transactions.put(t.getId(), t);
        }

        return transactions;
    }

    private void runCommand(TransactionProcessorContext context) {
        SEBPendingTransactionCommand command = new SEBPendingTransactionCommand(context, Mockito.mock(
                CategoryChangeRecordDao.class));
        command.initialize();
        for (Transaction inBatch : context.getInBatchTransactions()) {
            command.execute(inBatch);
        }
        command.postProcess();
    }

    @Test
    public void newPending_verifyNoMarkedForDelete() {
        Mockito.when(userData.getInStoreTransactions()).thenReturn(getInStoreTestTransactions(1));
        TransactionProcessorContext context = context(Lists.newArrayList());

        runCommand(context);

        Assert.assertEquals(0, context.getTransactionsToDelete().size());
    }

    @Test
    public void makeSureWeDoNotDeleteEverythingOnNull() {
        Mockito.when(userData.getInStoreTransactions()).thenReturn(getInStoreTestTransactions(3));
        Transaction bookedTransaction = createTestTransaction("0001", "Ikea", -1000, 0, false);

        PartnerTransactionPayload payload = new PartnerTransactionPayload();
        List<String> ids = Lists.newArrayList();
        ids.add(null);
        payload.setReservationIds(ids);
        setSebPayloadOnTransaction(bookedTransaction, payload);

        TransactionProcessorContext context = context(Lists.newArrayList(bookedTransaction));

        runCommand(context);

        Assert.assertEquals(0, context.getTransactionsToDelete().size());
    }

    @Test
    public void oneOldPending_verifyNewBookedMarkedForDelete() {
        Mockito.when(userData.getInStoreTransactions()).thenReturn(getInStoreTestTransactions(1));
        TransactionProcessorContext context = context(Lists.newArrayList(
                createTestTransaction("in-batch-booked", "Mat", -99, 0, false)));

        runCommand(context);

        Assert.assertEquals(1, context.getTransactionsToDelete().size());
        Set<String> deletedIds = context.getTransactionsToDelete().stream().map(Transaction::getId)
                .collect(Collectors.toSet());
        Assert.assertTrue(deletedIds.contains(TRANSACTION_UUID_PREFIX + "pending0"));
    }

    @Test
    public void twoOldPending_oneBooked_verifyOnlyOnePendingMarkedForDelete() {
        Mockito.when(userData.getInStoreTransactions()).thenReturn(getInStoreTestTransactions(2));
        TransactionProcessorContext context = context(Lists.newArrayList(
                createTestTransaction("in-batch-booked", "Mat", -99, 0, false)));

        runCommand(context);

        Assert.assertEquals(1, context.getTransactionsToDelete().size());
        Transaction deletedTransaction = Iterables.get(context.getTransactionsToDelete(), 0);
        Assert.assertTrue(deletedTransaction.getId().startsWith(TRANSACTION_UUID_PREFIX + "pending")); //number doesn't matter
    }

    @Test
    public void twoOldPending_twoBooked_verifyBothPendingMarkedForDelete() {
        Mockito.when(userData.getInStoreTransactions()).thenReturn(getInStoreTestTransactions(2));
        TransactionProcessorContext context = context(Lists.newArrayList(
                createTestTransaction("0001", "Mat", -99, 0, false),
                createTestTransaction("in-batch-booked2", "Mat", -99, 1, false)));

        runCommand(context);

        Assert.assertEquals(2, context.getTransactionsToDelete().size());

        Set<String> deletedIds = context.getTransactionsToDelete().stream().map(Transaction::getId)
                .collect(Collectors.toSet());

        Assert.assertTrue(deletedIds.contains(TRANSACTION_UUID_PREFIX + "pending0"));
        Assert.assertTrue(deletedIds.contains(TRANSACTION_UUID_PREFIX + "pending1"));
    }

    @Test
    public void twoOldPending_twoBooked_onePending_verifyBothPendingMarkedForDelete() {
        Mockito.when(userData.getInStoreTransactions()).thenReturn(getInStoreTestTransactions(2));
        TransactionProcessorContext context = context(Lists.newArrayList(
                createTestTransaction("0001", "Mat", -99, 0, false),
                createTestTransaction("in-batch-booked2", "Mat", -99, 0, false),
                createTestTransaction("in-batch-pending1", "Mat", -99, 0, true)));

        runCommand(context);

        Assert.assertEquals(2, context.getTransactionsToDelete().size());

        Set<String> deletedIds = context.getTransactionsToDelete().stream().map(Transaction::getId)
                .collect(Collectors.toSet());

        Assert.assertTrue(deletedIds.contains(TRANSACTION_UUID_PREFIX + "pending0"));
        Assert.assertTrue(deletedIds.contains(TRANSACTION_UUID_PREFIX + "pending1"));
    }

    @Test
    public void twoOldPending_oneBooked_twoPending_verifyOnePendingMarkedForDelete() {
        Mockito.when(userData.getInStoreTransactions()).thenReturn(getInStoreTestTransactions(2));
        TransactionProcessorContext context = context(Lists.newArrayList(
                createTestTransaction("0001", "Mat", -99, 0, false),
                createTestTransaction("in-batch-pending1", "Mat", -99, 0, true),
                createTestTransaction("in-batch-pending2", "Mat", -99, 0, true)));

        runCommand(context);

        Assert.assertEquals(1, context.getTransactionsToDelete().size());
        Transaction deletedTransaction = Iterables.get(context.getTransactionsToDelete(), 0);
        Assert.assertTrue(deletedTransaction.getId().startsWith(TRANSACTION_UUID_PREFIX + "pending")); //number doesn't matter
    }

    @Test
    public void oneOldPending_oneBookedWithNewCategoryChangedByUser_verifyBookedHasOldCategory() {
        Transaction bookedTransaction = createTestTransaction("0001", "Mat", -99, 0, false);
        TransactionProcessorContext context = context(Lists.newArrayList(bookedTransaction));

        HashMap<String, Transaction> inStoreTransactions = getInStoreTestTransactions(1);
        Transaction pendingTransaction = inStoreTransactions.get(TRANSACTION_UUID_PREFIX + "pending0");

        pendingTransaction.setCategory(CATEGORY_ID2, CategoryTypes.EXPENSES);
        pendingTransaction.setUserModifiedCategory(true);
        Mockito.when(userData.getInStoreTransactions()).thenReturn(inStoreTransactions);

        runCommand(context);

        Assert.assertEquals(CATEGORY_ID2, bookedTransaction.getCategoryId());
    }

    @Test
    public void oneOldPending_oneBookedWithNewCategoryNonUserChanged_verifyBookedHasNewCategory() {
        Transaction bookedTransaction = createTestTransaction("0001", "Mat", -99, 0, false);
        TransactionProcessorContext context = context(Lists.newArrayList(bookedTransaction));

        HashMap<String, Transaction> inStoreTransactions = getInStoreTestTransactions(1);
        Transaction pendingTransaction = inStoreTransactions.get(TRANSACTION_UUID_PREFIX + "pending0");

        pendingTransaction.setCategory(CATEGORY_ID2, CategoryTypes.EXPENSES);
        Mockito.when(userData.getInStoreTransactions()).thenReturn(inStoreTransactions);

        runCommand(context);

        Assert.assertEquals(CATEGORY_ID, bookedTransaction.getCategoryId());
    }

    @Test
    public void pendingWithReservationId_bookedWithReservationId_verifyMatch() {
        Transaction bookedTransaction = createTestTransaction("0001", "Mat", -99, 0, false);
        setSebPayloadOnTransaction(bookedTransaction, PartnerTransactionPayload.createFromReservationIds("reservationId1"));
        TransactionProcessorContext context = context(Lists.newArrayList(bookedTransaction));

        HashMap<String, Transaction> inStoreTransactions = getInStoreTestTransactions(1);
        Transaction pendingTransaction = inStoreTransactions.get(TRANSACTION_UUID_PREFIX + "pending0");
        setSebPayloadOnTransaction(pendingTransaction, PartnerTransactionPayload.createFromReservationIds("reservationId1"));

        Mockito.when(userData.getInStoreTransactions()).thenReturn(inStoreTransactions);

        runCommand(context);

        Assert.assertEquals(1, context.getTransactionsToDelete().size());

        Set<String> deletedIds = context.getTransactionsToDelete().stream().map(Transaction::getId)
                .collect(Collectors.toSet());
        Assert.assertTrue(deletedIds.contains(TRANSACTION_UUID_PREFIX + "pending0"));
    }

    @Test
    public void pendingWithReservationId_bookedWithOtherReservationId_verifyNoMatch() {
        Transaction bookedTransaction = createTestTransaction("0001", "Food", -109, 0, false);
        setSebPayloadOnTransaction(bookedTransaction, PartnerTransactionPayload.createFromReservationIds("reservationId2"));
        TransactionProcessorContext context = context(Lists.newArrayList(bookedTransaction));

        HashMap<String, Transaction> inStoreTransactions = getInStoreTestTransactions(1);
        Transaction pendingTransaction = inStoreTransactions.get(TRANSACTION_UUID_PREFIX + "pending0");
        setSebPayloadOnTransaction(pendingTransaction, PartnerTransactionPayload.createFromReservationIds("reservationId1"));

        Mockito.when(userData.getInStoreTransactions()).thenReturn(inStoreTransactions);

        runCommand(context);

        Assert.assertEquals(0, context.getTransactionsToDelete().size());
    }

    @Test
    public void pendingWithOldDate_verifyNotDeleted() {
        Transaction bookedTransaction = createTestTransaction("0001", "Food", -109, 0, false);
        TransactionProcessorContext context = context(Lists.newArrayList(bookedTransaction));

        HashMap<String, Transaction> inStoreTransactions = getInStoreTestTransactions(1);
        Transaction pendingTransaction = inStoreTransactions.get(TRANSACTION_UUID_PREFIX + "pending0");
        pendingTransaction.setOriginalDate(DateUtils.addDays(new Date(), -5));

        Mockito.when(userData.getInStoreTransactions()).thenReturn(inStoreTransactions);

        runCommand(context);

        Assert.assertEquals(0, context.getTransactionsToDelete().size());
    }

    @Test
    public void pendingWithExpiredDate_verifyDeleted() {
        Transaction bookedTransaction = createTestTransaction("0001", "Food", -109, 0, false);
        TransactionProcessorContext context = context(Lists.newArrayList(bookedTransaction));

        HashMap<String, Transaction> inStoreTransactions = getInStoreTestTransactions(1);
        Transaction pendingTransaction = inStoreTransactions.get(TRANSACTION_UUID_PREFIX + "pending0");
        pendingTransaction.setOriginalDate(DateUtils.addDays(new Date(), -5)); // should not matter anymore
        PartnerTransactionPayload payload = PartnerTransactionPayload.createFromReservationIds();
        payload.setPendingTransactionExpirationDate(DateUtils.addDays(new Date(), -1)); // this date matters now
        setSebPayloadOnTransaction(pendingTransaction, payload);

        Mockito.when(userData.getInStoreTransactions()).thenReturn(inStoreTransactions);

        runCommand(context);

        Assert.assertEquals(1, context.getTransactionsToDelete().size());

        Set<String> deletedIds = context.getTransactionsToDelete().stream().map(Transaction::getId)
                .collect(Collectors.toSet());
        Assert.assertTrue(deletedIds.contains(TRANSACTION_UUID_PREFIX + "pending0"));
    }

    @Test
    public void bookedWithThreeReservationIds_fourPending_verifyAllThreeMatch() {
        Transaction bookedTransaction = createTestTransaction("in-batch-booked", "Food", -3 * 99, 0, false);
        TransactionProcessorContext context = context(Lists.newArrayList(bookedTransaction));

        PartnerTransactionPayload sebPayload = new PartnerTransactionPayload();
        sebPayload.setReservationIds(Lists.newArrayList("reservationId0", "reservationId1", "reservationId2"));
        setSebPayloadOnTransaction(bookedTransaction, sebPayload);

        HashMap<String, Transaction> inStoreTransactions = getPendingTransactionsWithCategoryAndReservationId(4);

        Mockito.when(userData.getInStoreTransactions()).thenReturn(inStoreTransactions);

        runCommand(context);

        Assert.assertEquals(3, context.getTransactionsToDelete().size());

        Set<String> deletedIds = context.getTransactionsToDelete().stream().map(Transaction::getId)
                .collect(Collectors.toSet());
        Assert.assertFalse(deletedIds.contains(TRANSACTION_UUID_PREFIX + "pending3"));
    }

    @Test
    public void bookedWithTwoReservationIds_oneReservationId_threePending_verifyTwoMatch() {
        Transaction bookedTransaction = createTestTransaction("in-batch-booked", "Food", -2 * 99, 0, false);
        TransactionProcessorContext context = context(Lists.newArrayList(bookedTransaction));

        PartnerTransactionPayload sebPayload = new PartnerTransactionPayload();
        sebPayload.setReservationIds(Lists.newArrayList("reservationId0", "reservationId1"));

        // This one should be ignored, since the SEB payload for multiple ids has been set.
        sebPayload.setReservationId("reservationId2");

        setSebPayloadOnTransaction(bookedTransaction, sebPayload);

        HashMap<String, Transaction> inStoreTransactions = getPendingTransactionsWithCategoryAndReservationId(3);

        Mockito.when(userData.getInStoreTransactions()).thenReturn(inStoreTransactions);

        runCommand(context);

        Assert.assertEquals(2, context.getTransactionsToDelete().size());

        Set<String> deletedIds = context.getTransactionsToDelete().stream().map(Transaction::getId)
                .collect(Collectors.toSet());

        Assert.assertFalse(deletedIds.contains(TRANSACTION_UUID_PREFIX + "pending2"));
    }

    @Test
    public void bookedWithContradictoryReservationIds_verifyReservationIdsIsPrioritized() {
        Transaction bookedTransaction = createTestTransaction("in-batch-booked", "Food", -99, 0, false);
        TransactionProcessorContext context = context(Lists.newArrayList(bookedTransaction));

        // The RESERVATION_IDS field should be prioritized over RESERVATION_ID. These fields should hopefully never
        // be contradictory, but the policy is to always choose the field for multiple ids if it is not empty.
        PartnerTransactionPayload sebPayload = new PartnerTransactionPayload();
        sebPayload.setReservationIds(Lists.newArrayList("reservationId0"));
        sebPayload.setReservationId("reservationId1");
        setSebPayloadOnTransaction(bookedTransaction, sebPayload);

        HashMap<String, Transaction> inStoreTransactions = getPendingTransactionsWithCategoryAndReservationId(2);

        Mockito.when(userData.getInStoreTransactions()).thenReturn(inStoreTransactions);

        runCommand(context);

        Assert.assertEquals(1, context.getTransactionsToDelete().size());

        Set<String> deletedIds = context.getTransactionsToDelete().stream().map(Transaction::getId)
                .collect(Collectors.toSet());
        Assert.assertFalse(deletedIds.contains(TRANSACTION_UUID_PREFIX + "pending1"));
    }

    @Test
    public void fourOldPending_someWithUserModifiedCategories_oneBooked_verifyBookedHasCorrectCategory() {
        Transaction bookedTransaction = createTestTransaction("0001", "Mat", -1000, 0, false);
        PartnerTransactionPayload payload = PartnerTransactionPayload
                .createFromReservationIds("reservationId0", "reservationId1", "reservationId2", "reservationId3");
        setSebPayloadOnTransaction(bookedTransaction, payload);

        TransactionProcessorContext context = context(Lists.newArrayList(bookedTransaction));

        HashMap<String, Transaction> inStoreTransactions = getPendingTransactionsWithCategoryAndReservationId(4);

        // Since there is a pending transaction with a user modified category which makes up a big part of the booked
        // transaction, the booked transaction should get that category.
        inStoreTransactions.get(TRANSACTION_UUID_PREFIX + "pending0").setAmount(-100.);
        inStoreTransactions.get(TRANSACTION_UUID_PREFIX + "pending0").setUserModifiedCategory(true);
        inStoreTransactions.get(TRANSACTION_UUID_PREFIX + "pending1").setAmount(-600.);
        inStoreTransactions.get(TRANSACTION_UUID_PREFIX + "pending1").setUserModifiedCategory(true);
        inStoreTransactions.get(TRANSACTION_UUID_PREFIX + "pending2").setAmount(-100.);
        inStoreTransactions.get(TRANSACTION_UUID_PREFIX + "pending3").setAmount(-200.);

        Mockito.when(userData.getInStoreTransactions()).thenReturn(inStoreTransactions);

        runCommand(context);

        Assert.assertEquals(4, context.getTransactionsToDelete().size());
        Assert.assertEquals(PENDING_CATEGORY_ID_PREFIX + "1", bookedTransaction.getCategoryId());
    }

    @Test
    public void twoOldPendingWithCategories_oneBooked_verifyBookedHasCorrectCategory() {
        Transaction bookedTransaction = createTestTransaction("0001", "Mat", -200, 0, false);
        PartnerTransactionPayload payload = PartnerTransactionPayload
                .createFromReservationIds("reservationId0", "reservationId1");
        setSebPayloadOnTransaction(bookedTransaction, payload);

        TransactionProcessorContext context = context(Lists.newArrayList(bookedTransaction));

        HashMap<String, Transaction> inStoreTransactions = getPendingTransactionsWithCategoryAndReservationId(2);

        // Since there is no pending transaction with a user modified category, the booked transaction should not get a
        // category from a pending transaction.
        inStoreTransactions.get(TRANSACTION_UUID_PREFIX + "pending0").setAmount(-50.);
        inStoreTransactions.get(TRANSACTION_UUID_PREFIX + "pending1").setAmount(-150.);

        Mockito.when(userData.getInStoreTransactions()).thenReturn(inStoreTransactions);

        runCommand(context);

        Assert.assertEquals(2, context.getTransactionsToDelete().size());
        Assert.assertEquals(CATEGORY_ID, bookedTransaction.getCategoryId());
    }

    @Test
    public void twoOldPendingWithUserModifiedCategories_oneBooked_verifyBookedHasCorrectCategory() {
        Transaction bookedTransaction = createTestTransaction("0001", "Mat", -200, 0, false);
        PartnerTransactionPayload payload = PartnerTransactionPayload
                .createFromReservationIds("reservationId0", "reservationId1");
        setSebPayloadOnTransaction(bookedTransaction, payload);

        TransactionProcessorContext context = context(Lists.newArrayList(bookedTransaction));

        HashMap<String, Transaction> inStoreTransactions = getPendingTransactionsWithCategoryAndReservationId(2);

        // Since there is a pending transaction with a user modified category which makes up a big part of the booked
        // transaction, the booked transaction should get that category.
        inStoreTransactions.get(TRANSACTION_UUID_PREFIX + "pending0").setAmount(-50.);
        inStoreTransactions.get(TRANSACTION_UUID_PREFIX + "pending0").setUserModifiedCategory(true);
        inStoreTransactions.get(TRANSACTION_UUID_PREFIX + "pending1").setAmount(-150.);
        inStoreTransactions.get(TRANSACTION_UUID_PREFIX + "pending1").setUserModifiedCategory(true);

        Mockito.when(userData.getInStoreTransactions()).thenReturn(inStoreTransactions);

        runCommand(context);

        Assert.assertEquals(2, context.getTransactionsToDelete().size());
        Assert.assertEquals(PENDING_CATEGORY_ID_PREFIX + "1", bookedTransaction.getCategoryId());
    }

    @Test
    public void threeOldPendingWithCategories_oneUserModified_sameAmount_oneBooked_verifyBookedHasCorrectCategory() {
        Transaction bookedTransaction = createTestTransaction("0001", "Mat", -300, 0, false);
        PartnerTransactionPayload payload = PartnerTransactionPayload
                .createFromReservationIds("reservationId0", "reservationId1", "reservationId2");
        setSebPayloadOnTransaction(bookedTransaction, payload);

        TransactionProcessorContext context = context(Lists.newArrayList(bookedTransaction));

        HashMap<String, Transaction> inStoreTransactions = getPendingTransactionsWithCategoryAndReservationId(3);

        // Since none of the pending transactions with a user modified category make up a big enough part of the booked
        // transaction (in terms of amount) the booked transaction shouldn't get any category from the pending ones.
        inStoreTransactions.get(TRANSACTION_UUID_PREFIX + "pending0").setAmount(-100.);
        inStoreTransactions.get(TRANSACTION_UUID_PREFIX + "pending1").setAmount(-100.);
        inStoreTransactions.get(TRANSACTION_UUID_PREFIX + "pending1").setUserModifiedCategory(true);
        inStoreTransactions.get(TRANSACTION_UUID_PREFIX + "pending2").setAmount(-100.);

        Mockito.when(userData.getInStoreTransactions()).thenReturn(inStoreTransactions);

        runCommand(context);

        Assert.assertEquals(3, context.getTransactionsToDelete().size());
        Assert.assertEquals(CATEGORY_ID, bookedTransaction.getCategoryId());
    }

    private void setSebPayloadOnTransaction(Transaction transaction, PartnerTransactionPayload payload) {
        String serializedPayload = SerializationUtils.serializeToString(payload);
        transaction.setInternalPayload(Transaction.InternalPayloadKeys.SEB_PAYLOAD, serializedPayload);
    }

    private static Transaction createTestTransaction(String idSuffix, String description, double amount, int dateOffset,
            boolean pending) {
        Transaction transaction = new Transaction();
        transaction.setId(TRANSACTION_UUID_PREFIX + idSuffix);
        transaction.setDescription(description);
        transaction.setOriginalDescription(description);
        transaction.setAmount(amount);
        transaction.setOriginalAmount(amount);
        transaction.setDate(DateUtils.addDays(new Date(), dateOffset));
        transaction.setOriginalDate(DateUtils.addDays(new Date(), dateOffset));
        transaction.setPending(pending);
        transaction.setAccountId("accountId");
        transaction.setUserId(USER_ID);
        transaction.setCategory(CATEGORY_ID, CategoryTypes.EXPENSES);

        return transaction;
    }
}
