package se.tink.backend.system.workers.processor.deduplication.detector;

import com.google.common.collect.ImmutableList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.libraries.date.DateUtils;

public class DuplicateTransactionDetectorTest {
    private Transaction transaction1;
    private Transaction transaction2;
    private DuplicateTransactionDetector detector;

    private static Transaction createTestTransaction() {
        Transaction t = new Transaction();
        t.setOriginalDescription("test1");
        t.setAccountId("account1");
        t.setOriginalDate(new Date());
        t.setOriginalAmount(1000.45);
        t.setInserted(System.currentTimeMillis());
        return t;
    }

    @Before
    public void setUp() {
        transaction1 = createTestTransaction();
        transaction2 = transaction1.clone();
        transaction2.setInserted(transaction1.getInserted() + 1L);

        detector = new DuplicateTransactionDetector(Collections.singletonList(transaction1));
    }

    @Test
    public void testWithNoDifference() {
        Assert.assertTrue(detector.findAndRemoveDuplicate(transaction2).isPresent());
    }

    @Test
    public void testWithNoDifferenceSameBatch() {
        transaction2.setInserted(transaction1.getInserted());
        Assert.assertFalse(detector.findAndRemoveDuplicate(transaction2).isPresent());
    }

    @Test
    public void testWithAccountDifference() {
        transaction2.setAccountId("account2");
        Assert.assertFalse(detector.findAndRemoveDuplicate(transaction2).isPresent());
    }

    @Test
    public void testWithPendingDifference() {
        transaction2.setPending(true);
        Assert.assertFalse(detector.findAndRemoveDuplicate(transaction2).isPresent());

        transaction1.setPending(true);
        detector = new DuplicateTransactionDetector(Collections.singletonList(transaction1));
        Assert.assertTrue(detector.findAndRemoveDuplicate(transaction2).isPresent());
    }

    @Test
    public void testWithDescriptionDifference() {
        transaction2.setOriginalDescription("test2");
        Assert.assertFalse(detector.findAndRemoveDuplicate(transaction2).isPresent());
    }

    @Test
    public void testWithAmountDifference() {

        // A small amount difference is fine (mostly for floating point precision).

        transaction2.setOriginalAmount(transaction1.getOriginalAmount() + 0.0005d);
        Assert.assertTrue(detector.findAndRemoveDuplicate(transaction2).isPresent());

        // A large amount difference is not however.

        transaction2.setOriginalAmount(transaction1.getOriginalAmount() + 0.0015d);
        Assert.assertFalse(detector.findAndRemoveDuplicate(transaction2).isPresent());
    }

    @Test
    public void testWithDateDifference() {

        // Transactions on different dates shouldn't match as duplicates.

        Calendar calendar = DateUtils.getCalendar();
        calendar.add(Calendar.DAY_OF_YEAR, -1);

        transaction2.setOriginalDate(calendar.getTime());
        Assert.assertFalse(detector.findAndRemoveDuplicate(transaction2).isPresent());
    }

    /**
     * Test when we have one transaction imported with external id and it is compared to one without external id
     */
    @Test
    public void testWithExternalIdDifference() {
        transaction1.setPayload(TransactionPayloadTypes.EXTERNAL_ID, "externalId1");
        transaction2.setPayload(TransactionPayloadTypes.EXTERNAL_ID, null);

        DuplicateTransactionDetector detector = new DuplicateTransactionDetector(
                Collections.singletonList(transaction1));

        Assert.assertTrue(detector.findAndRemoveDuplicate(transaction2).isPresent());
    }

    /**
     * Test when we have one transaction imported without external id and it is compared to one with external id
     */
    @Test
    public void testWithExternalIdNotImported() {
        transaction1.setPayload(TransactionPayloadTypes.EXTERNAL_ID, "externalId1");
        transaction2.setPayload(TransactionPayloadTypes.EXTERNAL_ID, null);

        detector = new DuplicateTransactionDetector(Collections.singletonList(transaction2));

        Assert.assertTrue(detector.findAndRemoveDuplicate(transaction1).isPresent());
    }

    /**
     * Test two transactions with same external id same batch
     */
    @Test
    public void testWithSameExternalIdsSameBatch() {
        transaction1.setPayload(TransactionPayloadTypes.EXTERNAL_ID, "externalId1");

        detector = new DuplicateTransactionDetector(Collections.singletonList(transaction1));

        transaction2.setOriginalDescription("description2");
        transaction2.setPayload(TransactionPayloadTypes.EXTERNAL_ID, "externalId1");

        // Use the same inserted/batch timestamp
        transaction2.setInserted(transaction1.getInserted());

        Assert.assertTrue(detector.findAndRemoveDuplicate(transaction2).isPresent());
    }

    /**
     * Test two transactions with same external id different batches
     */
    @Test
    public void testWithSameExternalIdsDifferentBatches() {
        transaction1.setPayload(TransactionPayloadTypes.EXTERNAL_ID, "externalId1");

        detector = new DuplicateTransactionDetector(Collections.singletonList(transaction1));

        transaction2.setOriginalDescription("test2");
        transaction2.setPayload(TransactionPayloadTypes.EXTERNAL_ID, "externalId1");

        Assert.assertTrue(detector.findAndRemoveDuplicate(transaction2).isPresent());
    }

    /**
     * Test two transactions with different external ids
     */
    @Test
    public void testWithDifferentExternalIds() {
        transaction1.setPayload(TransactionPayloadTypes.EXTERNAL_ID, "externalId1");

        detector = new DuplicateTransactionDetector(Collections.singletonList(transaction1));

        transaction2.setOriginalDescription(transaction1.getOriginalDescription());
        transaction2.setPayload(TransactionPayloadTypes.EXTERNAL_ID, "externalId2");

        Assert.assertFalse(detector.findAndRemoveDuplicate(transaction2).isPresent());
    }

    @Test
    public void testRemovingDuplicateTransactions() {
        detector = new DuplicateTransactionDetector(ImmutableList.of(transaction1, transaction2));

        Assert.assertFalse(detector.isEmpty());
        Assert.assertTrue(detector.findAndRemoveDuplicate(transaction1).isPresent());
        Assert.assertFalse(detector.isEmpty());
        Assert.assertTrue(detector.findAndRemoveDuplicate(transaction2).isPresent());

        Assert.assertTrue(detector.isEmpty());
        Assert.assertFalse(detector.findAndRemoveDuplicate(transaction1).isPresent());
        Assert.assertFalse(detector.findAndRemoveDuplicate(transaction2).isPresent());
    }
}
