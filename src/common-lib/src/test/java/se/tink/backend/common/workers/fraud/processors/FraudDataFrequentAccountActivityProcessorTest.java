package se.tink.backend.common.workers.fraud.processors;

import com.google.common.collect.Iterables;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.common.workers.fraud.AbstractFraudProcessTest;
import se.tink.backend.core.FraudDetailsContent;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.FraudTransactionContent;
import se.tink.backend.core.FraudTypes;
import se.tink.backend.utils.guavaimpl.predicates.StringEqualsPredicate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FraudDataFrequentAccountActivityProcessorTest extends AbstractFraudProcessTest {
    private FraudDataFrequentAccountActivityProcessor processor;

    @Before
    public void setUp() throws Exception {
        Date now = new Date();
        transactionMap.putAll(createTransactions(DateUtils.addMonths(now, -6),
               DateUtils.addDays(now, -30), frequentAccountId, noFrequentAccountId));

        processor = new FraudDataFrequentAccountActivityProcessor();
    }

    @Test
    public void testFraudContentCreated() throws Exception {
        Date today = new Date();
        String id1 = addTransaction(frequentAccountId, DateUtils.addDays(today, -1), -70,
                "categoryId");
        String id2 = addTransaction(frequentAccountId, DateUtils.addDays(today, -1), -70,
                "categoryId");
        String id3 = addTransaction(frequentAccountId, DateUtils.addDays(today, -1), -70,
                "categoryId");
        String id4 = addTransaction(frequentAccountId, DateUtils.addDays(today, -1), -70,
                "categoryId");
        String id5 = addTransaction(frequentAccountId, DateUtils.addDays(today, -1), -70,
                "categoryId");
        String id6 = addTransaction(frequentAccountId, DateUtils.addDays(today, -1), -70,
                "categoryId");
        String id7 = addTransaction(frequentAccountId, DateUtils.addDays(today, -1), -70,
                "categoryId");
        String id8 = addTransaction(frequentAccountId, DateUtils.addDays(today, -1), -70,
                "categoryId");
        String id9 = addTransaction(frequentAccountId, DateUtils.addDays(today, -1), -70,
                "categoryId");
        String id10 = addTransaction(frequentAccountId, DateUtils.addDays(today, -1),
                -70, "categoryId");
        String id11 = addTransaction(frequentAccountId, DateUtils.addDays(today, -1),
                -70, "categoryId");
        String id12 = addTransaction(frequentAccountId, DateUtils.addDays(today, -1),
                -70, "categoryId");

        processor.process(processorContext);

        List<FraudDetailsContent> contents = processorContext.getFraudDetailsContent();

        assertEquals(1, contents.size());
        assertEquals(FraudDetailsContentType.FREQUENT_ACCOUNT_ACTIVITY, contents.get(0).getContentType());
        assertEquals(FraudTypes.TRANSACTION, contents.get(0).itemType());

        FraudTransactionContent content = (FraudTransactionContent) contents.get(0);

        List<String> ids = content.getTransactionIds();

        assertTrue(Iterables.any(ids, new StringEqualsPredicate(id1)));
        assertTrue(Iterables.any(ids, new StringEqualsPredicate(id2)));
        assertTrue(Iterables.any(ids, new StringEqualsPredicate(id3)));
        assertTrue(Iterables.any(ids, new StringEqualsPredicate(id4)));
        assertTrue(Iterables.any(ids, new StringEqualsPredicate(id5)));
        assertTrue(Iterables.any(ids, new StringEqualsPredicate(id6)));
        assertTrue(Iterables.any(ids, new StringEqualsPredicate(id7)));
        assertTrue(Iterables.any(ids, new StringEqualsPredicate(id8)));
        assertTrue(Iterables.any(ids, new StringEqualsPredicate(id9)));
        assertTrue(Iterables.any(ids, new StringEqualsPredicate(id10)));
        assertTrue(Iterables.any(ids, new StringEqualsPredicate(id11)));
        assertTrue(Iterables.any(ids, new StringEqualsPredicate(id12)));
    }

    @Test
    public void testFraudContentNotCreatedDueToTooFewDataPointsOnAccountId1() throws Exception {
        Date today = new Date();
        addTransaction(noFrequentAccountId, DateUtils.addDays(today, -1), -70, "categoryId");
        addTransaction(noFrequentAccountId, DateUtils.addDays(today, -1), -70, "categoryId");
        addTransaction(noFrequentAccountId, DateUtils.addDays(today, -1), -70, "categoryId");
        addTransaction(noFrequentAccountId, DateUtils.addDays(today, -1), -70, "categoryId");
        addTransaction(noFrequentAccountId, DateUtils.addDays(today, -1), -70, "categoryId");
        addTransaction(noFrequentAccountId, DateUtils.addDays(today, -1), -70, "categoryId");
        addTransaction(noFrequentAccountId, DateUtils.addDays(today, -1), -70, "categoryId");
        addTransaction(noFrequentAccountId, DateUtils.addDays(today, -1), -70, "categoryId");
        addTransaction(noFrequentAccountId, DateUtils.addDays(today, -1), -70, "categoryId");
        addTransaction(noFrequentAccountId, DateUtils.addDays(today, -1), -70, "categoryId");
        addTransaction(noFrequentAccountId, DateUtils.addDays(today, -1), -70, "categoryId");
        addTransaction(noFrequentAccountId, DateUtils.addDays(today, -1), -70, "categoryId");
        addTransaction(noFrequentAccountId, DateUtils.addDays(today, -1), -70, "categoryId");

        processor.process(processorContext);

        List<FraudDetailsContent> contents = processorContext.getFraudDetailsContent();

        assertEquals(0, contents.size());
    }

    @Test
    public void testFraudContentNotCreatedDueToTooOldDataPointsOnFrequentAccount() throws Exception {
        Date today = new Date();
        int daysBack = 7;
        addTransaction(frequentAccountId, DateUtils.addDays(today, -daysBack), -70, "categoryId");
        addTransaction(frequentAccountId, DateUtils.addDays(today, -daysBack), -70, "categoryId");
        addTransaction(frequentAccountId, DateUtils.addDays(today, -daysBack), -70, "categoryId");
        addTransaction(frequentAccountId, DateUtils.addDays(today, -daysBack), -70, "categoryId");
        addTransaction(frequentAccountId, DateUtils.addDays(today, -daysBack), -70, "categoryId");
        addTransaction(frequentAccountId, DateUtils.addDays(today, -daysBack), -70, "categoryId");
        addTransaction(frequentAccountId, DateUtils.addDays(today, -daysBack), -70, "categoryId");
        addTransaction(frequentAccountId, DateUtils.addDays(today, -daysBack), -70, "categoryId");

        processor.process(processorContext);

        List<FraudDetailsContent> contents = processorContext.getFraudDetailsContent();

        assertEquals(0, contents.size());
    }
}
