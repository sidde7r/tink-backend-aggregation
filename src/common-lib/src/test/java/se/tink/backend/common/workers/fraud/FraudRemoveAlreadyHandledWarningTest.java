package se.tink.backend.common.workers.fraud;

import java.util.Date;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.common.workers.fraud.processors.FraudDataLargeWithdrawalProcessor;
import se.tink.backend.common.workers.fraud.processors.FraudDataRemoveAlreadyHandledProcessor;
import se.tink.backend.core.Category;
import se.tink.backend.core.FraudDetailsContent;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.FraudStatus;
import se.tink.backend.core.FraudTransactionContent;

public class FraudRemoveAlreadyHandledWarningTest extends AbstractFraudProcessTest {

    @Test
    public void testWarnOnAlreadyWarnedDetailsOfSameType() throws Exception {
        mockupDataForTest();

        addInStoreFraudDetails(
                createFraudDetails(
                        createTransactionContent(FraudDetailsContentType.LARGE_WITHDRAWAL, "Large Withdrawal")));

        FraudDataLargeWithdrawalProcessor largeWithdrawalProcessor = new FraudDataLargeWithdrawalProcessor();
        FraudDataRemoveAlreadyHandledProcessor removeAlreadyHandledProcessor = new FraudDataRemoveAlreadyHandledProcessor();
        largeWithdrawalProcessor.process(processorContext);
        removeAlreadyHandledProcessor.process(processorContext);

        List<FraudDetailsContent> fraudDetaialsContents = processorContext.getFraudDetailsContent();

        Assert.assertEquals(0, fraudDetaialsContents.size());
    }

    @Test
    public void testWarnOnAlreadyWarnedDetailsOfOtherType() throws Exception {
        mockupDataForTest();
        addInStoreFraudDetails(
                createFraudDetails(
                        createTransactionContent(FraudDetailsContentType.FREQUENT_ACCOUNT_ACTIVITY, "Large Withdrawal")));

        FraudDataLargeWithdrawalProcessor largeWithdrawalProcessor = new FraudDataLargeWithdrawalProcessor();
        FraudDataRemoveAlreadyHandledProcessor removeAlreadyHandledProcessor = new FraudDataRemoveAlreadyHandledProcessor();
        largeWithdrawalProcessor.process(processorContext);
        removeAlreadyHandledProcessor.process(processorContext);

        List<FraudDetailsContent> fraudDetaialsContents = processorContext.getFraudDetailsContent();

        Assert.assertEquals(1, fraudDetaialsContents.size());
    }

    @Test
    public void testWarnOnAlreadyWarnedDetailsOfNonHandled() throws Exception {
        mockupDataForTest();
        addInStoreFraudDetails(
                createFraudDetails(
                        createTransactionContent(FraudDetailsContentType.LARGE_WITHDRAWAL, ""), FraudStatus.CRITICAL));

        FraudDataLargeWithdrawalProcessor largeWithdrawalProcessor = new FraudDataLargeWithdrawalProcessor();
        FraudDataRemoveAlreadyHandledProcessor removeAlreadyHandledProcessor = new FraudDataRemoveAlreadyHandledProcessor();
        largeWithdrawalProcessor.process(processorContext);
        removeAlreadyHandledProcessor.process(processorContext);

        List<FraudDetailsContent> fraudDetaialsContents = processorContext.getFraudDetailsContent();

        Assert.assertEquals(1, fraudDetaialsContents.size());
    }

    @Test
    public void testWarnOnAlreadyWarnedDetailsOfWrongDescription() throws Exception {
        mockupDataForTest();
        addInStoreFraudDetails(
                createFraudDetails(
                        createTransactionContent(FraudDetailsContentType.LARGE_WITHDRAWAL, "Other Merchant")));

        FraudDataLargeWithdrawalProcessor largeWithdrawalProcessor = new FraudDataLargeWithdrawalProcessor();
        FraudDataRemoveAlreadyHandledProcessor removeAlreadyHandledProcessor = new FraudDataRemoveAlreadyHandledProcessor();
        largeWithdrawalProcessor.process(processorContext);
        removeAlreadyHandledProcessor.process(processorContext);

        List<FraudDetailsContent> fraudDetaialsContents = processorContext.getFraudDetailsContent();

        Assert.assertEquals(1, fraudDetaialsContents.size());
    }

    @Test
    public void testWarnOnAlreadyWarnedDetailsManyOldDetails() throws Exception {
        mockupDataForTest();
        addInStoreFraudDetails(
                createFraudDetails(
                        createTransactionContent(FraudDetailsContentType.LARGE_WITHDRAWAL, "Other Merchant")));
        addInStoreFraudDetails(
                createFraudDetails(createTransactionContent(FraudDetailsContentType.LARGE_EXPENSE, "One Merchant")));
        addInStoreFraudDetails(
                createFraudDetails(createTransactionContent(FraudDetailsContentType.LARGE_WITHDRAWAL, "Merchant")));
        addInStoreFraudDetails(
                createFraudDetails(
                        createTransactionContent(FraudDetailsContentType.LARGE_WITHDRAWAL, "Large Withdrawal")));
        addInStoreFraudDetails(
                createFraudDetails(createTransactionContent(FraudDetailsContentType.LARGE_WITHDRAWAL, "Merchant 2")));

        FraudDataLargeWithdrawalProcessor largeWithdrawalProcessor = new FraudDataLargeWithdrawalProcessor();
        FraudDataRemoveAlreadyHandledProcessor removeAlreadyHandledProcessor = new FraudDataRemoveAlreadyHandledProcessor();
        largeWithdrawalProcessor.process(processorContext);
        removeAlreadyHandledProcessor.process(processorContext);

        List<FraudDetailsContent> fraudDetaialsContents = processorContext.getFraudDetailsContent();

        Assert.assertEquals(0, fraudDetaialsContents.size());
    }

    private void mockupDataForTest() throws Exception {
        mockupTransactions(createTransactionsLargeWithdrawal(10));
    }

    private FraudTransactionContent createTransactionContent(FraudDetailsContentType type, String description) {
        final Category withdrawalCategory = processorContext.getCategoriesByCodeForLocale()
                .get(processorContext.getCategoryConfiguration().getWithdrawalsCode());
        return createFraudTransactionContent(type,
                createTransaction(description, "", new Date(), -20000, withdrawalCategory.getId()));
    }
}
