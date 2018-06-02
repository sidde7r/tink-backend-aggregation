package se.tink.backend.system.workers.processor.deduplication.detector;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.assertj.core.util.Maps;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.system.workers.processor.deduplication.DeduplicationResult;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.Histogram;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.backend.core.Account;
import se.tink.backend.core.Provider;
import se.tink.backend.core.Transaction;
import se.tink.libraries.date.DateUtils;

public class FuzzyTransactionDeduplicatorTest {

    private List<Account> accounts = Lists.newArrayList();
    private MetricRegistry metricRegistry;
    private Provider provider;

    @Before
    public void setup() {
        Account a1 = new Account();
        a1.setCertainDate(DateUtils.parseDate("2016-12-29"));

        Account a2 = new Account();
        a2.setCertainDate(DateUtils.parseDate("2016-12-05"));

        accounts.add(a1);
        accounts.add(a2);

        this.metricRegistry = mockMetricRegistry();
        this.provider = mockProvider("swedbank-bankid");
    }

    private MetricRegistry mockMetricRegistry() {
        MetricRegistry metricRegistry = Mockito.mock(MetricRegistry.class);

        MetricId metricId = Mockito.mock(MetricId.class);
        Histogram histogram = Mockito.mock(Histogram.class);
        Counter counter = Mockito.mock(Counter.class);

        Mockito.when(metricId.label(Mockito.any(MetricId.MetricLabels.class)))
                .thenReturn(metricId);
        Mockito.when(metricRegistry.histogram(Mockito.any(MetricId.class), Mockito.<Number>anyList()))
                .thenReturn(histogram);
        Mockito.when(metricRegistry.meter(Mockito.any(MetricId.class)))
                .thenReturn(counter);

        return metricRegistry;
    }

    private Provider mockProvider(String name) {
        Provider provider = Mockito.mock(Provider.class);

        Mockito.when(provider.getName())
                .thenReturn(name);
        Mockito.when(provider.getGroupDisplayName())
                .thenReturn("test");

        return provider;
    }

    @Test
    public void ensureToMapByAccountId_returnsEmptyMap_whenTransactionList_isNull() {
        Map<String, List<Transaction>> transactionsByAccount = FuzzyTransactionDeduplicator.toMapByAccountId(null);

        Assert.assertEquals(0, transactionsByAccount.size());
    }

    @Test
    public void ensureToMapByAccountId_returnsEmptyMap_whenTransactionList_isEmpty() {
        Map<String, List<Transaction>> transactionsByAccount = FuzzyTransactionDeduplicator.toMapByAccountId(
                Lists.<Transaction>newArrayList());

        Assert.assertEquals(0, transactionsByAccount.size());
    }

    @Test(expected = NullPointerException.class)
    public void ensureTruncateTransactionsByAccount_throwNPE_whenAccounts_isNull() {
        FuzzyTransactionDeduplicator.truncateTransactionsByAccount(
                FuzzyTransactionDeduplicator.toMapByAccountId(createExistingTransactions()),
                FuzzyTransactionDeduplicator.toMapByAccountId(createIncomingTransactions()),
                null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureTruncateTransactionsByAccount_throwIllegalArgumentException_whenAccounts_isEmpty() {
        FuzzyTransactionDeduplicator.truncateTransactionsByAccount(
                FuzzyTransactionDeduplicator.toMapByAccountId(createExistingTransactions()),
                FuzzyTransactionDeduplicator.toMapByAccountId(createIncomingTransactions()),
                Lists.<Account>newArrayList());
    }

    private List<Transaction> createExistingTransactionsForCutTest() {
        return Lists.newArrayList(
                TransactionCreator.create("t1", 10, accounts.get(0), -2),
                TransactionCreator.create("t2", 20, accounts.get(0), -1),
                TransactionCreator.create("t3", 30, accounts.get(0), 0, true),
                TransactionCreator.create("t4", 40, accounts.get(0), 1),
                TransactionCreator.create("t5", 50, accounts.get(0), 2, true),
                TransactionCreator.create("t6", 60, accounts.get(0), 3, true)
        );
    }

    @Test
    public void ensureIncomingTransactions_isNotModified_whenExistingTransactions_areEmpty() {
        List<Transaction> incomingTransactions = Lists.newArrayList(
                TransactionCreator.create("t1", 10, accounts.get(0), 0)
        );

        Map<String, List<Transaction>> existingTransactionsByAccount = FuzzyTransactionDeduplicator
                .toMapByAccountId(Lists.<Transaction>newArrayList());
        Map<String, List<Transaction>> incomingTransactionsByAccount = FuzzyTransactionDeduplicator
                .toMapByAccountId(incomingTransactions);

        FuzzyTransactionDeduplicator.truncateTransactionsByAccount(
                existingTransactionsByAccount, incomingTransactionsByAccount, accounts);

        Assert.assertEquals(1, incomingTransactionsByAccount.get(accounts.get(0).getId()).size());
        Assert.assertNull(existingTransactionsByAccount.get(accounts.get(0).getId()));
    }

    @Test
    public void ensureExistingAndIncomingTransactions_isIgnored_whenIncomingTransactions_isEmpty() {
        List<Transaction> existingTransactions = Lists.newArrayList(
                TransactionCreator.create("t1", 10, accounts.get(0), -1),
                TransactionCreator.create("t2", 20, accounts.get(0), 0)
        );

        Map<String, List<Transaction>> existingTransactionsByAccount = FuzzyTransactionDeduplicator
                .toMapByAccountId(existingTransactions);
        Map<String, List<Transaction>> incomingTransactionsByAccount = FuzzyTransactionDeduplicator
                .toMapByAccountId(Lists.<Transaction>newArrayList());

        FuzzyTransactionDeduplicator.truncateTransactionsByAccount(
                existingTransactionsByAccount, incomingTransactionsByAccount, accounts);

        List<Transaction> formattedExistingTransactions = existingTransactionsByAccount.get(accounts.get(0).getId());
        List<Transaction> formattedIncomingTransactions = incomingTransactionsByAccount.get(accounts.get(0).getId());

        Assert.assertNull(formattedExistingTransactions);
        Assert.assertNull(formattedIncomingTransactions);
    }

    @Test
    public void ensureExistingAndIncomingTransactions_isIgnored_whenIncomingTransactionsList_isNull() {
        List<Transaction> existingTransactions = Lists.newArrayList(
                TransactionCreator.create("t1", 10, accounts.get(0), -1),
                TransactionCreator.create("t2", 20, accounts.get(0), 0)
        );

        Map<String, List<Transaction>> existingTransactionsByAccount = FuzzyTransactionDeduplicator
                .toMapByAccountId(existingTransactions);
        Map<String, List<Transaction>> incomingTransactionsByAccount = Maps.newHashMap();

        incomingTransactionsByAccount.put(accounts.get(0).getId(), null);

        FuzzyTransactionDeduplicator.truncateTransactionsByAccount(
                existingTransactionsByAccount, incomingTransactionsByAccount, accounts);

        List<Transaction> formattedExistingTransactions = existingTransactionsByAccount.get(accounts.get(0).getId());
        List<Transaction> formattedIncomingTransactions = incomingTransactionsByAccount.get(accounts.get(0).getId());

        Assert.assertNull(formattedExistingTransactions);
        Assert.assertNull(formattedIncomingTransactions);
    }

    @Test
    public void ensureExistingTransactions_thatIsNotLinkedToAnAccount_isIgnored() {
        Account unknownAccount = new Account();

        Map<String, List<Transaction>> existingTransactionsByAccount = FuzzyTransactionDeduplicator
                .toMapByAccountId(Lists.newArrayList(
                        TransactionCreator.create("t1", 10, unknownAccount, 0)));

        FuzzyTransactionDeduplicator.truncateTransactionsByAccount(
                existingTransactionsByAccount, Maps.newHashMap(), accounts);

        Assert.assertTrue(existingTransactionsByAccount.isEmpty());
    }

    @Test
    public void ensureExistingAndIncomingTransactions_thatIsNotLinkedToAnAccount_isIgnored() {
        Account unknownAccount = new Account();

        Map<String, List<Transaction>> incomingTransactionsByAccount = FuzzyTransactionDeduplicator
                .toMapByAccountId(Lists.newArrayList(
                        TransactionCreator.create("t1", 10, unknownAccount, 0)));

        FuzzyTransactionDeduplicator.truncateTransactionsByAccount(
                Maps.newHashMap(), incomingTransactionsByAccount, accounts);

        Assert.assertTrue(incomingTransactionsByAccount.isEmpty());
    }

    @Test
    public void ensureExistingTransactions_isFilteredOut_whenMostRecentExistingTransaction_isOlderThan_certainDate() {
        List<Transaction> existingTransactions = Lists.newArrayList(
                TransactionCreator.create("t1", 10, accounts.get(0), -3)
        );
        List<Transaction> incomingTransactions = Lists.newArrayList(
                TransactionCreator.create("t2", 20, accounts.get(0), -1),
                TransactionCreator.create("t3", 30, accounts.get(0), 0),
                TransactionCreator.create("t4", 40, accounts.get(0), 0)
        );

        Map<String, List<Transaction>> existingTransactionsByAccount = FuzzyTransactionDeduplicator
                .toMapByAccountId(existingTransactions);
        Map<String, List<Transaction>> incomingTransactionsByAccount = FuzzyTransactionDeduplicator
                .toMapByAccountId(incomingTransactions);

        FuzzyTransactionDeduplicator.truncateTransactionsByAccount(
                existingTransactionsByAccount, incomingTransactionsByAccount, accounts);

        Assert.assertNull(existingTransactionsByAccount.get(accounts.get(0).getId()));
        Assert.assertEquals(3, incomingTransactionsByAccount.get(accounts.get(0).getId()).size());
    }

    @Test
    public void ensureExistingTransactions_isCutOnOldestIncomingTransaction_whenMostRecentExistingTransaction_isOlderThan_certainDate_andYoungerThan_oldestIncomingTransaction() {
        List<Transaction> existingTransactions = Lists.newArrayList(
                TransactionCreator.create("t1", 10, accounts.get(0), -3),
                TransactionCreator.create("t2", 20, accounts.get(0), -1)
        );
        List<Transaction> incomingTransactions = Lists.newArrayList(
                TransactionCreator.create("t2", 20, accounts.get(0), -1),
                TransactionCreator.create("t3", 30, accounts.get(0), 0),
                TransactionCreator.create("t4", 40, accounts.get(0), 0)
        );

        Map<String, List<Transaction>> existingTransactionsByAccount = FuzzyTransactionDeduplicator
                .toMapByAccountId(existingTransactions);
        Map<String, List<Transaction>> incomingTransactionsByAccount = FuzzyTransactionDeduplicator
                .toMapByAccountId(incomingTransactions);

        FuzzyTransactionDeduplicator.truncateTransactionsByAccount(
                existingTransactionsByAccount, incomingTransactionsByAccount, accounts);

        Assert.assertEquals(1, existingTransactionsByAccount.get(accounts.get(0).getId()).size());
        Assert.assertEquals(3, incomingTransactionsByAccount.get(accounts.get(0).getId()).size());
    }

    @Test
    public void ensureIncomingTransactions_thatBelongToUnknownAccount_isIgnored() {
        List<Transaction> existingTransactions = Lists.newArrayList(
                TransactionCreator.create("t1", 10, accounts.get(0), -1),
                TransactionCreator.create("t2", 20, accounts.get(0), 0)
        );
        List<Transaction> incomingTransactions = Lists.newArrayList(
                TransactionCreator.create("t1", 10, accounts.get(0), -1),
                TransactionCreator.create("t2", 20, accounts.get(0), 0),
                TransactionCreator.create("t3", 30, null, 0)
        );

        Map<String, List<Transaction>> existingTransactionsByAccount = FuzzyTransactionDeduplicator
                .toMapByAccountId(existingTransactions);
        Map<String, List<Transaction>> incomingTransactionsByAccount = FuzzyTransactionDeduplicator
                .toMapByAccountId(incomingTransactions);

        FuzzyTransactionDeduplicator.truncateTransactionsByAccount(
                existingTransactionsByAccount, incomingTransactionsByAccount, accounts);

        List<Transaction> formattedExistingTransactions = existingTransactionsByAccount.get(accounts.get(0).getId());
        List<Transaction> formattedIncomingTransactions = incomingTransactionsByAccount.get(accounts.get(0).getId());

        Assert.assertEquals(1, formattedExistingTransactions.size());
        Assert.assertEquals(1, formattedIncomingTransactions.size());
    }

    @Test
    public void ensureNonPendingExistingTransactions_beforeOldestIncoming_isIgnored_whenOldestIncoming_isBetween_youngestAndOldestExisting() {
        List<Transaction> existingTransactions = Lists.newArrayList(
                TransactionCreator.create("t1", 10, accounts.get(0), -2),
                TransactionCreator.create("t2", 20, accounts.get(0), -1),
                TransactionCreator.create("t3", 30, accounts.get(0), 0, true),
                TransactionCreator.create("t4", 40, accounts.get(0), 1),
                TransactionCreator.create("t5", 50, accounts.get(0), 2, true),
                TransactionCreator.create("t6", 60, accounts.get(0), 3),
                TransactionCreator.create("t7", 70, accounts.get(0), 3),
                TransactionCreator.create("t8", 80, accounts.get(0), 4),
                TransactionCreator.create("t9", 90, accounts.get(0), 5, true)
        );
        List<Transaction> incomingTransactions = Lists.newArrayList(
                TransactionCreator.create("t3", 30, accounts.get(0), 2),
                TransactionCreator.create("t5", 50, accounts.get(0), 3),
                TransactionCreator.create("t6", 60, accounts.get(0), 3),
                TransactionCreator.create("t7", 70, accounts.get(0), 3),
                TransactionCreator.create("t8", 80, accounts.get(0), 4),
                TransactionCreator.create("t9", 90, accounts.get(0), 5),
                TransactionCreator.create("t10", 100, accounts.get(0), 6, true)
        );

        Map<String, List<Transaction>> existingTransactionsByAccount = FuzzyTransactionDeduplicator
                .toMapByAccountId(existingTransactions);
        Map<String, List<Transaction>> incomingTransactionsByAccount = FuzzyTransactionDeduplicator
                .toMapByAccountId(incomingTransactions);

        FuzzyTransactionDeduplicator.truncateTransactionsByAccount(
                existingTransactionsByAccount, incomingTransactionsByAccount, accounts);

        List<Transaction> formattedExistingTransactions = existingTransactionsByAccount.get(accounts.get(0).getId());
        List<Transaction> formattedIncomingTransactions = incomingTransactionsByAccount.get(accounts.get(0).getId());

        Assert.assertEquals(6, formattedExistingTransactions.size());
        Assert.assertEquals(7, formattedIncomingTransactions.size());
    }

    @Test
    // Same as above test but with null as the accounts certain date
    public void ensureNonPendingExistingTransactions_beforeOldestIncoming_isIgnored_whenCertainDate_isNull() {
        List<Transaction> existingTransactions = Lists.newArrayList(
                TransactionCreator.create("t1", 10, accounts.get(0), -2),
                TransactionCreator.create("t2", 20, accounts.get(0), -1),
                TransactionCreator.create("t3", 30, accounts.get(0), 0, true),
                TransactionCreator.create("t4", 40, accounts.get(0), 1),
                TransactionCreator.create("t5", 50, accounts.get(0), 2, true),
                TransactionCreator.create("t6", 60, accounts.get(0), 3),
                TransactionCreator.create("t7", 70, accounts.get(0), 3),
                TransactionCreator.create("t8", 80, accounts.get(0), 4),
                TransactionCreator.create("t9", 90, accounts.get(0), 5, true)
        );
        List<Transaction> incomingTransactions = Lists.newArrayList(
                TransactionCreator.create("t3", 30, accounts.get(0), 2),
                TransactionCreator.create("t5", 50, accounts.get(0), 3),
                TransactionCreator.create("t6", 60, accounts.get(0), 3),
                TransactionCreator.create("t7", 70, accounts.get(0), 3),
                TransactionCreator.create("t8", 80, accounts.get(0), 4),
                TransactionCreator.create("t9", 90, accounts.get(0), 5),
                TransactionCreator.create("t10", 100, accounts.get(0), 6, true)
        );

        accounts.get(0).setCertainDate(null);

        Map<String, List<Transaction>> existingTransactionsByAccount = FuzzyTransactionDeduplicator
                .toMapByAccountId(existingTransactions);
        Map<String, List<Transaction>> incomingTransactionsByAccount = FuzzyTransactionDeduplicator
                .toMapByAccountId(incomingTransactions);

        FuzzyTransactionDeduplicator.truncateTransactionsByAccount(
                existingTransactionsByAccount, incomingTransactionsByAccount, accounts);

        List<Transaction> formattedExistingTransactions = existingTransactionsByAccount.get(accounts.get(0).getId());
        List<Transaction> formattedIncomingTransactions = incomingTransactionsByAccount.get(accounts.get(0).getId());

        Assert.assertEquals(6, formattedExistingTransactions.size());
        Assert.assertEquals(7, formattedIncomingTransactions.size());
    }

    @Test
    // 'oldestExisting' = oldest transaction in the database after the accounts certainDate
    public void ensureIncomingTransactions_isCutOnOldestExisting_whenOldestIncoming_isOlderThan_oldestExisting() {
        List<Transaction> incomingTransactions = Lists.newArrayList(
                TransactionCreator.create("t2", 20, accounts.get(0), -1),
                TransactionCreator.create("t3", 30, accounts.get(0), 1),
                TransactionCreator.create("t4", 40, accounts.get(0), 1),
                TransactionCreator.create("t5", 50, accounts.get(0), 3),
                TransactionCreator.create("t6", 60, accounts.get(0), 4, true),
                TransactionCreator.create("t7", 70, accounts.get(0), 4, true)
        );

        Map<String, List<Transaction>> existingTransactionsByAccount = FuzzyTransactionDeduplicator
                .toMapByAccountId(createExistingTransactionsForCutTest());
        Map<String, List<Transaction>> incomingTransactionsByAccount = FuzzyTransactionDeduplicator
                .toMapByAccountId(incomingTransactions);

        FuzzyTransactionDeduplicator.truncateTransactionsByAccount(
                existingTransactionsByAccount, incomingTransactionsByAccount, accounts);

        List<Transaction> formattedExistingTransactions = existingTransactionsByAccount.get(accounts.get(0).getId());
        List<Transaction> formattedIncomingTransactions = incomingTransactionsByAccount.get(accounts.get(0).getId());

        Assert.assertEquals(4, formattedExistingTransactions.size());
        Assert.assertEquals(5, formattedIncomingTransactions.size());
    }

    @Test
    public void ensureExistingTransactions_areRemoved_whenOldestIncoming_isAfter_mostRecentExisting() {
        List<Transaction> incomingTransactions = Lists.newArrayList(
                TransactionCreator.create("t7", 70, accounts.get(0), 4, true),
                TransactionCreator.create("t8", 80, accounts.get(0), 5)
        );

        Map<String, List<Transaction>> existingTransactionsByAccount = FuzzyTransactionDeduplicator
                .toMapByAccountId(createExistingTransactionsForCutTest());
        Map<String, List<Transaction>> incomingTransactionsByAccount = FuzzyTransactionDeduplicator
                .toMapByAccountId(incomingTransactions);

        FuzzyTransactionDeduplicator.truncateTransactionsByAccount(
                existingTransactionsByAccount, incomingTransactionsByAccount, accounts);

        List<Transaction> formattedExistingTransactions = existingTransactionsByAccount.get(accounts.get(0).getId());
        List<Transaction> formattedIncomingTransactions = incomingTransactionsByAccount.get(accounts.get(0).getId());

        Assert.assertEquals(0, formattedExistingTransactions.size());
        Assert.assertEquals(2, formattedIncomingTransactions.size());
    }

    @Test
    public void ensureFilter_removesRedundantTransactions() {
        List<Transaction> incomingTransactions = Lists.newArrayList(
                TransactionCreator.create("t1", 10, accounts.get(0), -1),
                TransactionCreator.create("Vattenfall", 231, accounts.get(0), 0));

        List<Transaction> existingTransactions = Lists.newArrayList(
                TransactionCreator.create("t1", 10, accounts.get(0), -1),
                TransactionCreator.create("Vattenfall", 231, accounts.get(0), 0),
                TransactionCreator.create("Vattenfall", 231, accounts.get(0), 0));

        DeduplicationResult result = filter(existingTransactions, incomingTransactions,
                accounts);

        Assert.assertEquals(1, result.getTransactionsToDelete().size());
    }

    @Test
    public void ensureFilter_ignoreExactMatches() {
        List<Transaction> incomingTransactions = Lists.newArrayList(
                TransactionCreator.create("t1", 10, accounts.get(0), -1),
                TransactionCreator.create("Vattenfall", 231, accounts.get(0), 0));

        List<Transaction> existingTransactions = Lists.newArrayList(
                TransactionCreator.create("t1", 10, accounts.get(0), -1),
                TransactionCreator.create("Vattenfall", 231, accounts.get(0), 0));

        DeduplicationResult result = filter(existingTransactions, incomingTransactions,
                accounts);

        Assert.assertEquals(0, result.getTransactionsToDelete().size());
        Assert.assertEquals(0, result.getTransactionsToSave().size());
    }

    @Test
    public void testMultiple_updatesRemovesAndInserts() {
        List<Transaction> incomingTransactions = createIncomingTransactions();
        List<Transaction> existingTransactions = createExistingTransactions();

        FuzzyTransactionDeduplicator deduplicator = new FuzzyTransactionDeduplicator(metricRegistry, provider, accounts);
        DeduplicationResult result = deduplicator
                .deduplicate(existingTransactions, incomingTransactions);

        List<Transaction> transactionsToDelete = result.getTransactionsToDelete();
        Assert.assertEquals(2, transactionsToDelete.size());
        // Redundant/Duplicate transaction
        Assert.assertEquals(existingTransactions.get(3), transactionsToDelete.get(0));

        List<Transaction> transactionsToSave = result.getTransactionsToSave();
        Assert.assertEquals(8, transactionsToSave.size());

        // New transactions
        Assert.assertTrue(transactionsToSave.contains(incomingTransactions.get(11)));
        Assert.assertTrue(transactionsToSave.contains(incomingTransactions.get(13)));

        // Updated transactions
        Assert.assertEquals(incomingTransactions.get(3), deduplicator.getTransactionToReplace(existingTransactions.get(4)));
        Assert.assertEquals(incomingTransactions.get(5), deduplicator.getTransactionToReplace(existingTransactions.get(5)));
        Assert.assertEquals(incomingTransactions.get(4), deduplicator.getTransactionToReplace(existingTransactions.get(6)));
        Assert.assertEquals(incomingTransactions.get(6), deduplicator.getTransactionToReplace(existingTransactions.get(7)));
        Assert.assertEquals(incomingTransactions.get(8), deduplicator.getTransactionToReplace(existingTransactions.get(9)));
        Assert.assertEquals(incomingTransactions.get(12), deduplicator.getTransactionToReplace(existingTransactions.get(12)));
    }

    @Test
    public void whenIncomingIsEmpty_ensureThereIsNoNPE() {
        DeduplicationResult result = filter(createExistingTransactions(),
                Lists.<Transaction>newArrayList(), accounts);

        Assert.assertEquals(0, result.getTransactionsToSave().size());
    }

    @Test
    public void whenExistingIsEmpty_ensureThereIsNoNPE() {
        DeduplicationResult result = filter(Lists.<Transaction>newArrayList(),
                createIncomingTransactions(), accounts);

        Assert.assertEquals(0, result.getTransactionsToDelete().size());
        Assert.assertEquals(14, result.getTransactionsToSave().size());
    }

    @Test
    public void whenIsPendingNoSettlementDifferentMonth() {
        List<Transaction> incomingTransactions = Lists.newArrayList(
                TransactionCreator.create("MiMix", 21, accounts.get(0), 2),
                TransactionCreator.create("Vattenfall", 231, accounts.get(0), 3, true));

        List<Transaction> existingTransactions = Lists.newArrayList(
                TransactionCreator.create("Vattenfall", 231, accounts.get(0), 2, true));

        DeduplicationResult result = filter(existingTransactions, incomingTransactions,
                accounts);
        List<Transaction> transactionsToSave = result.getTransactionsToSave();
        List<Transaction> transactionsToDelete = result.getTransactionsToDelete();
        Assert.assertEquals(2, transactionsToSave.size());
        Assert.assertEquals(1, transactionsToDelete.size());
    }

    @Test
    public void whenIsPendingNoSettlementSameDay() {
        List<Transaction> incomingTransactions = Lists.newArrayList(
                TransactionCreator.create("Vattenfall", 231, accounts.get(0), 1, true));

        List<Transaction> existingTransactions = Lists.newArrayList(
                TransactionCreator.create("Vattenfall", 231, accounts.get(0), 1, true));

        DeduplicationResult result = filter(existingTransactions, incomingTransactions,
                accounts);
        List<Transaction> transactionsToSave = result.getTransactionsToSave();
        List<Transaction> transactionsToDelete = result.getTransactionsToDelete();
        Assert.assertEquals(0, transactionsToSave.size());
        Assert.assertEquals(0, transactionsToDelete.size());
    }

    @Test
    public void whenIsPendingSettledSameMonth() {
        List<Transaction> incomingTransactions = Lists.newArrayList(
                TransactionCreator.create("MiMix", 21, accounts.get(0), 2),
                TransactionCreator.create("Vattenfall", 231, accounts.get(0), 3));

        List<Transaction> existingTransactions = Lists.newArrayList(
                TransactionCreator.create("Vattenfall", 231, accounts.get(0), 2, true));

        DeduplicationResult result = filter(existingTransactions, incomingTransactions,
                accounts);
        List<Transaction> transactionsToSave = result.getTransactionsToSave();
        List<Transaction> transactionsToDelete = result.getTransactionsToDelete();
        Assert.assertEquals(2, transactionsToSave.size());
        Assert.assertEquals(1, transactionsToDelete.size());
    }


    @Test
    public void testDuplicateIncomingPendingSameDay() {
        List<Transaction> incomingTransactions = Lists.newArrayList(
                TransactionCreator.create("  www.ica supermarket sabbatsberg", -69.9, accounts.get(0), DateTime.parse("2018-02-26T11:00:00").toDate(), true)
        );

        ArrayList<Transaction> existingTransactions = Lists.newArrayList(
                TransactionCreator.create("  www.ica supermarket sabbatsberg", -69.9, accounts.get(0), DateTime.parse("2018-02-26T11:00:00").toDate(), true)
        );


        DeduplicationResult result = filter(existingTransactions, incomingTransactions, accounts);
        Assert.assertEquals(0, result.getTransactionsToSave().size());
        Assert.assertEquals(0, result.getTransactionsToDelete().size());
    }

    @Test
    public void testDuplicateIncomingNonPendingSameDay() {
        List<Transaction> incomingTransactions = Lists.newArrayList(
                TransactionCreator.create("  www.ica supermarket sabbatsberg", -69.9, accounts.get(0), DateTime.parse("2018-02-26T11:00:00").toDate(), false)
        );

        ArrayList<Transaction> existingTransactions = Lists.newArrayList(
                TransactionCreator.create("  www.ica supermarket sabbatsberg", -69.9, accounts.get(0), DateTime.parse("2018-02-26T11:00:00").toDate(), false),
                TransactionCreator.create("MiMix", 21, accounts.get(0), 4),
                TransactionCreator.create("Vattenfall", 231, accounts.get(0), 2)
        );


        DeduplicationResult result = filter(existingTransactions, incomingTransactions, accounts);
        Assert.assertEquals(0, result.getTransactionsToSave().size());
        Assert.assertEquals(0, result.getTransactionsToDelete().size());
    }

    @Test
    public void testFutureTransactionDontTrunacteIncomingTransactions() {
        List<Transaction> incomingTransactions = Lists.newArrayList(
                TransactionCreator.create("www.ica supermarket sabbatsberg", -69.9, accounts.get(0), DateTime.parse("2018-02-20T11:00:00").toDate(), false),
                TransactionCreator.create("MiMix", -21, accounts.get(0), DateTime.parse("2018-02-21T11:00:00").toDate(), false),
                TransactionCreator.create("Vattenfall", -231, accounts.get(0), DateTime.parse("2018-02-22T11:00:00").toDate(), false),
                TransactionCreator.create("Future Transaction", -123, accounts.get(0), DateTime.parse("2018-02-26T11:00:00").toDate(), true)
        );

        ArrayList<Transaction> existingTransactions = Lists.newArrayList(
                TransactionCreator.create("Future Transaction", -123, accounts.get(0), DateTime.parse("2018-02-26T11:00:00").toDate(), true)
        );


        DeduplicationResult result = filter(existingTransactions, incomingTransactions, accounts);
        Assert.assertEquals(3, result.getTransactionsToSave().size());
        Assert.assertEquals(0, result.getTransactionsToDelete().size());
    }

    @Test
    public void whenIsPendingSettledDifferentMonths() {
        List<Transaction> incomingTransactions = Lists.newArrayList(
                TransactionCreator.create("MiMix", 21, accounts.get(0), 2),
                TransactionCreator.create("Vattenfall", 231, accounts.get(0), 3));

        List<Transaction> existingTransactions = Lists.newArrayList(
                TransactionCreator.create("pending_Vattenfall", 231, accounts.get(0), 2, true));

        DeduplicationResult result = filter(existingTransactions, incomingTransactions,
                accounts);
        List<Transaction> transactionsToSave = result.getTransactionsToSave();
        List<Transaction> transactionsToDelete = result.getTransactionsToDelete();
        Assert.assertEquals(2, transactionsToSave.size());
        Assert.assertEquals(1, transactionsToDelete.size());
        Assert.assertEquals(existingTransactions.get(0).getId(), transactionsToDelete.get(0).getId());
        Assert.assertEquals(transactionsToSave.get(1).getDescription(), "Vattenfall");
    }


    @Test
    public void whenIsPendingChange_ensurePayloadIsSet() {
        List<Transaction> incomingTransactions = Lists.newArrayList(
                TransactionCreator.create("t1", 10, accounts.get(0), -1),
                TransactionCreator.create("Vattenfall", 231, accounts.get(0), 0));

        List<Transaction> existingTransactions = Lists.newArrayList(
                TransactionCreator.create("t1", 10, accounts.get(0), -1),
                TransactionCreator.create("Vattenfall", 231, accounts.get(0), 0, true));

        DeduplicationResult result = filter(existingTransactions, incomingTransactions,
                accounts);

        Assert.assertEquals("{\"UNSETTLED_AMOUNT\":\"231.0\"}", result.getTransactionsToSave()
                .get(0).getPayloadSerialized());
    }

    @Test
    public void whenIsPendingChange_ensurePayloadIsSetDifferentMonths() {
        List<Transaction> incomingTransactions = Lists.newArrayList(
                TransactionCreator.create("t1", 10, accounts.get(0), 2),
                TransactionCreator.create("Vattenfall", 231, accounts.get(0), 3));

        List<Transaction> existingTransactions = Lists.newArrayList(
                TransactionCreator.create("t1", 10, accounts.get(0), 2),
                TransactionCreator.create("Vattenfall", 231, accounts.get(0), 3, true));

        DeduplicationResult result = filter(existingTransactions, incomingTransactions,
                accounts);

        Assert.assertEquals("{\"UNSETTLED_AMOUNT\":\"231.0\"}", result.getTransactionsToSave()
                .get(0).getPayloadSerialized());
    }

    @Test
    public void whenIsPendingAndAmountChange_ensurePayloadIsSetCorrectly() {
        List<Transaction> incomingTransactions = Lists.newArrayList(
                TransactionCreator.create("t1", 10, accounts.get(0), -1),
                TransactionCreator.create("Vattenfall", 231, accounts.get(0), 0));

        List<Transaction> existingTransactions = Lists.newArrayList(
                TransactionCreator.create("t1", 10, accounts.get(0), -1),
                TransactionCreator.create("Vattenfall", 232, accounts.get(0), 0, true));

        DeduplicationResult result = filter(existingTransactions, incomingTransactions,
                accounts);

        Assert.assertEquals("{\"UNSETTLED_AMOUNT\":\"232.0\"}", result.getTransactionsToSave()
                .get(0).getPayloadSerialized());
    }

    @Test
    public void whenIsPendingAndAmountChange_ensurePayloadIsSetCorrectlyDifferentMonths() {
        List<Transaction> incomingTransactions = Lists.newArrayList(
                TransactionCreator.create("t1", 10, accounts.get(0), 2),
                TransactionCreator.create("Vattenfall", 231, accounts.get(0), 3));

        List<Transaction> existingTransactions = Lists.newArrayList(
                TransactionCreator.create("t1", 10, accounts.get(0), 2),
                TransactionCreator.create("Vattenfall", 232, accounts.get(0), 2, true));

        DeduplicationResult result = filter(existingTransactions, incomingTransactions,
                accounts);

        Assert.assertEquals("{\"UNSETTLED_AMOUNT\":\"232.0\"}", result.getTransactionsToSave()
                .get(0).getPayloadSerialized());
    }


    @Test
    public void whenUnresolvedAbsent_ensurePendingTransactionsBeforeIncoming_isRemoved() {
        List<Transaction> incomingTransactions = Lists.newArrayList(
                TransactionCreator.create("t1", 10, accounts.get(1), -1),
                TransactionCreator.create("Mattes mattor", 300, accounts.get(1), 2),
                TransactionCreator.create("T2", 2, accounts.get(1), 3));

        List<Transaction> existingTransactions = Lists.newArrayList(
                TransactionCreator.create("t1", 10, accounts.get(1), -1),
                TransactionCreator.create("Kalles korvmoj", 35, accounts.get(1), 0, true),
                TransactionCreator.create("T2", 2, accounts.get(1), 3));

        DeduplicationResult result = filter(existingTransactions, incomingTransactions,
                accounts);

        List<Transaction> transactionsToSave = result.getTransactionsToSave();
        List<Transaction> transactionsToDelete = result.getTransactionsToDelete();

        Assert.assertEquals(1, transactionsToSave.size());
        Assert.assertEquals(1, transactionsToDelete.size());
        Assert.assertEquals(incomingTransactions.get(1), transactionsToSave.get(0));
        Assert.assertEquals(existingTransactions.get(1), transactionsToDelete.get(0));
    }

    @Test
    public void whenUnresolvedAbsent_ensurePendingTransactionsBeforeIncoming_isRemoved_differentMonths() {
        List<Transaction> incomingTransactions = Lists.newArrayList(
                TransactionCreator.create("t1", 10, accounts.get(1), 2),
                TransactionCreator.create("Mattes mattor", 300, accounts.get(1), 2),
                TransactionCreator.create("T2", 2, accounts.get(1), 3));

        List<Transaction> existingTransactions = Lists.newArrayList(
                TransactionCreator.create("t1", 10, accounts.get(1), 2),
                TransactionCreator.create("Kalles korvmoj", 35, accounts.get(1), 2, true),
                TransactionCreator.create("T2", 2, accounts.get(1), 2));

        DeduplicationResult result = filter(existingTransactions, incomingTransactions,
                accounts);

        List<Transaction> transactionsToSave = result.getTransactionsToSave();
        List<Transaction> transactionsToDelete = result.getTransactionsToDelete();

        Assert.assertEquals(2, transactionsToSave.size());
        Assert.assertEquals(2, transactionsToDelete.size());
        Assert.assertEquals(incomingTransactions.get(1), transactionsToSave.get(0));
        Assert.assertEquals(existingTransactions.get(1), transactionsToDelete.get(0));
    }

    @Test
    public void whenUnresolvedPresent_ensureTransactionIsUpdated() {
        List<Transaction> incomingTransactions = Lists.newArrayList(
                TransactionCreator.create("t1", 10, accounts.get(1), -1),
                TransactionCreator.create("Kalles korvmoj", 35, accounts.get(1), 2),
                TransactionCreator.create("T2", 2, accounts.get(1), 3));

        List<Transaction> existingTransactions = Lists.newArrayList(
                TransactionCreator.create("t1", 10, accounts.get(1), -1),
                TransactionCreator.create("Kalles korvmoj", 35, accounts.get(1), 0, true),
                TransactionCreator.create("T2", 2, accounts.get(1), 3));

        DeduplicationResult result = filter(existingTransactions, incomingTransactions,
                accounts);

        Assert.assertEquals(1, result.getTransactionsToSave().size());
    }

    @Test
    public void ensureThat_orderDoesNotMakeAnyDifference_whenMultipleTransactionsAreSimilar() {
        List<Transaction> incomingTransactions = Lists.newArrayList(
                TransactionCreator.create("t1", 10, accounts.get(0), -1),
                TransactionCreator.create("ahlens", -299, accounts.get(0), 1),
                TransactionCreator.create("Ahlquist", -299, accounts.get(0), 1));

        List<Transaction> existingTransactions = Lists.newArrayList(
                TransactionCreator.create("t1", 10, accounts.get(0), -1),
                TransactionCreator.create("ahl", -299, accounts.get(0), 1, true),
                TransactionCreator.create("ahlens AB", -299, accounts.get(0), 1, true));

        FuzzyTransactionDeduplicator deduplicator = new FuzzyTransactionDeduplicator(metricRegistry, provider, accounts);
        DeduplicationResult result = deduplicator.deduplicate(existingTransactions, incomingTransactions);

        Assert.assertEquals(2, result.getTransactionsToSave().size());
        Assert.assertEquals(0, result.getTransactionsToDelete().size());
        Assert.assertEquals(deduplicator.getTransactionToReplace(existingTransactions.get(1)), incomingTransactions.get(2));
        Assert.assertEquals(deduplicator.getTransactionToReplace(existingTransactions.get(2)), incomingTransactions.get(1));
    }

    @Test
    public void ensureThat_orderDoesNotMakeAnyDifference_whenMultipleTransactionsAreSimilarDifferentMonths() {
        List<Transaction> incomingTransactions = Lists.newArrayList(
                TransactionCreator.create("t1", 10, accounts.get(0), 2),
                TransactionCreator.create("ahlens", -299, accounts.get(0), 3),
                TransactionCreator.create("Ahlquist", -299, accounts.get(0), 3));

        List<Transaction> existingTransactions = Lists.newArrayList(
                TransactionCreator.create("t1", 10, accounts.get(0), -1),
                TransactionCreator.create("ahl", -299, accounts.get(0), 2, true),
                TransactionCreator.create("ahlens AB", -299, accounts.get(0), 2, true));

        FuzzyTransactionDeduplicator deduplicator = new FuzzyTransactionDeduplicator(metricRegistry, provider, accounts);
        DeduplicationResult result = deduplicator.deduplicate(existingTransactions, incomingTransactions);

        Assert.assertEquals(3, result.getTransactionsToSave().size());
        Assert.assertEquals(2, result.getTransactionsToDelete().size());
        Assert.assertEquals(deduplicator.getTransactionToReplace(existingTransactions.get(1)), incomingTransactions.get(2));
        Assert.assertEquals(deduplicator.getTransactionToReplace(existingTransactions.get(2)), incomingTransactions.get(1));
    }

    private DeduplicationResult filter(Collection<Transaction> existingTransactions,
            List<Transaction> incomingTransactions, List<Account> accounts) {
        FuzzyTransactionDeduplicator deduplicator = new FuzzyTransactionDeduplicator(metricRegistry, provider, accounts);

        return deduplicator.deduplicate(existingTransactions, incomingTransactions);
    }

    private List<Transaction> createExistingTransactions() {
        List<Transaction> transactions = Lists.newArrayList();

        transactions.add(TransactionCreator.create("ICA Nära ENSKEDE", 114, accounts.get(0), -3)); // Outside of scope
        transactions.add(TransactionCreator.create("ICA Nära ENSKEDE", 210, accounts.get(1), -2)); // Outside of scope
        transactions.add(TransactionCreator.create("Vattenfall", 540, accounts.get(0), 0)); // Exact match
        transactions.add(TransactionCreator.create("Vattenfall", 540, accounts.get(0), 0)); // Redundant
        transactions.add(TransactionCreator.create("ICA Nära ENSKEDE", 97, accounts.get(1), 1, true)); // Updated desc & date
        transactions.add(TransactionCreator.create("Restaurang Esperanza", 119, accounts.get(0), 2, true)); // Updated amount & date
        transactions.add(TransactionCreator.create("ICA Nära ENSKEDE", 463, accounts.get(0), 3, true)); // Updated desc
        transactions.add(TransactionCreator.create("ICA Nära ENSKEDE", 314, accounts.get(0), 5, true)); // Updated desc
        transactions.add(TransactionCreator.create("ICA Nära ENSKEDE", 47, accounts.get(1), 6)); // Exact match
        transactions.add(TransactionCreator.create("ICA Nära TENSTA TORG", 47, accounts.get(1), 6, true)); // Updated desc & date
        transactions.add(TransactionCreator.create("ICA Nära ENSKEDE", 98, accounts.get(1), 8)); // Exact match
        transactions.add(TransactionCreator.create("IKEA Kungens", 299, accounts.get(0), 8)); // Exact match
        transactions.add(TransactionCreator.create("TICNET", 798, accounts.get(0), 10, true)); // Updated date

        return transactions;
    }

    private List<Transaction> createIncomingTransactions() {
        List<Transaction> transactions = Lists.newArrayList();

        transactions.add(TransactionCreator.create("ICA Nära ENSENSKEDE", 114, accounts.get(0), -3)); // Outside of scope
        transactions.add(TransactionCreator.create("ICA Nära ENSENSKEDE", 210, accounts.get(1), -2)); // Outside of scope
        transactions.add(TransactionCreator.create("Vattenfall", 540, accounts.get(0), 0)); // Exact match
        transactions.add(TransactionCreator.create("ICA Nära ENSENSKEDE", 97, accounts.get(1), 1)); // Updated desc
        transactions.add(TransactionCreator.create("ICA Nära ENSENSKEDE", 463, accounts.get(0), 3)); // Updated desc
        transactions.add(TransactionCreator.create("Restaurang Esperanza", 150, accounts.get(0), 5)); // Updated amount & date
        transactions.add(TransactionCreator.create("ICA Nära ENSENSKEDE", 314, accounts.get(0), 5)); // Updated desc
        transactions.add(TransactionCreator.create("ICA Nära ENSKEDE", 47, accounts.get(1), 6)); // // Exact match
        transactions.add(TransactionCreator.create("ICA Nära TENSTA", 47, accounts.get(1), 7)); // Updated desc & date
        transactions.add(TransactionCreator.create("ICA Nära ENSKEDE", 98, accounts.get(1), 8)); // Exact match
        transactions.add(TransactionCreator.create("IKEA Kungens", 299, accounts.get(0), 8)); // Exact match
        transactions.add(TransactionCreator.create("ICA Nära ENSKEDE", 58, accounts.get(0), 11)); // New transaction
        transactions.add(TransactionCreator.create("TICNET", 798, accounts.get(0), 12)); // Updated date
        transactions.add(TransactionCreator.create("ICA Nära ENSKEDE", 149, accounts.get(1), 12)); // New transaction
        transactions.add(TransactionCreator.create("ICA Nära Stuvsta", 237, null, 13)); // No account

        return transactions;
    }
}
