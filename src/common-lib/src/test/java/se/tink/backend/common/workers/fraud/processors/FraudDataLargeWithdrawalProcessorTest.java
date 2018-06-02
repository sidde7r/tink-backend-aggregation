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
import se.tink.backend.core.Transaction;
import se.tink.backend.utils.guavaimpl.predicates.StringEqualsPredicate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FraudDataLargeWithdrawalProcessorTest extends AbstractFraudProcessTest {

    private FraudDataLargeWithdrawalProcessor processor;

    @Before
    public void setUp() throws Exception {
        processor = new FraudDataLargeWithdrawalProcessor();
    }

    @Test
    public void testFraudContentCreated() throws Exception {
        Date yesterday = DateUtils.addDays(new Date(), -1);
        addTransaction("account1", yesterday, -100, withdrawalCategory.getId());
        addTransaction("account1", yesterday, -200, withdrawalCategory.getId());
        addTransaction("account1", yesterday, -300, withdrawalCategory.getId());
        addTransaction("account1", yesterday, -400, withdrawalCategory.getId());
        addTransaction("account1", yesterday, -400, withdrawalCategory.getId());
        addTransaction("account1", yesterday, -600, withdrawalCategory.getId());
        addTransaction("account1", yesterday, -600, withdrawalCategory.getId());
        
        String transactionId = addTransaction("account1", new Date(), -2100, withdrawalCategory.getId());

        processor.process(processorContext);

        List<FraudDetailsContent> contents = processorContext.getFraudDetailsContent();

        assertEquals(1, contents.size());
        assertEquals(FraudDetailsContentType.LARGE_WITHDRAWAL, contents.get(0).getContentType());
        assertEquals(FraudTypes.TRANSACTION, contents.get(0).itemType());

        FraudTransactionContent content = (FraudTransactionContent) contents.get(0);

        List<String> ids = content.getTransactionIds();

        assertTrue(Iterables.any(ids, new StringEqualsPredicate(transactionId)));
    }

    @Test
    public void testFraudContentNotCreatedDueToTooFewDataPointsOnAccountId1() throws Exception {
        Date yesterday = DateUtils.addDays(new Date(), -1);
        addTransaction("account1", yesterday, -100, withdrawalCategory.getId());
        addTransaction("account1", yesterday, -100, withdrawalCategory.getId());
        addTransaction("account1", yesterday, -700, withdrawalCategory.getId());
        addTransaction("account1", yesterday, -800, withdrawalCategory.getId());
        addTransaction("account1", yesterday, -900, withdrawalCategory.getId());
        addTransaction("account1", yesterday, -900, withdrawalCategory.getId());
        addTransaction("account1", yesterday, -700, withdrawalCategory.getId());
        
        addTransaction("account1", new Date(), -1300, withdrawalCategory.getId());

        processor.process(processorContext);

        List<FraudDetailsContent> contents = processorContext.getFraudDetailsContent();

        assertEquals(0, contents.size());
    }

    @Test
    public void testFraudContentNotCreatedDueToTooUserCategorizedWithdrawal() throws Exception {
        Date yesterday = DateUtils.addDays(new Date(), -1);
        
        addTransaction("account1", yesterday, -100, withdrawalCategory.getId());
        addTransaction("account1", yesterday, -200, withdrawalCategory.getId());
        addTransaction("account1", yesterday, -300, withdrawalCategory.getId());
        addTransaction("account1", yesterday, -400, withdrawalCategory.getId());
        addTransaction("account1", yesterday, -400, withdrawalCategory.getId());
        addTransaction("account1", yesterday, -600, withdrawalCategory.getId());
        addTransaction("account1", yesterday, -600, withdrawalCategory.getId());

        Transaction transaction = createTransaction("Bankomat", "account1", yesterday, -2100, withdrawalCategory.getId());
        transaction.setUserModifiedCategory(true);
        transactionMap.put(transaction.getId(), transaction);


        processor.process(processorContext);

        List<FraudDetailsContent> contents = processorContext.getFraudDetailsContent();

        assertEquals(0, contents.size());
    }
}
