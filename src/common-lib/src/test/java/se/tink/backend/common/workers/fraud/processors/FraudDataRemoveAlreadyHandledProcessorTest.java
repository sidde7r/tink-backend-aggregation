package se.tink.backend.common.workers.fraud.processors;

import com.google.api.client.util.Lists;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import se.tink.backend.common.workers.fraud.AbstractFraudProcessTest;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsContent;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.FraudTransactionContent;
import se.tink.backend.core.Transaction;
import static org.junit.Assert.assertEquals;

public class FraudDataRemoveAlreadyHandledProcessorTest extends AbstractFraudProcessTest {

    @Test
    public void expectAllDetailsContentsForNoTransactionDetails() throws Exception {

        List<FraudDetailsContent> detailsContents = createTestDetailsContents();
        List<FraudDetails> inStoreDetailsList = createFraudDetails(createTestDetailsContents());
        int expContentsSize = detailsContents.size();
        int expInStoreDetailsSize = inStoreDetailsList.size();

        addFraudDetailsContent(Lists.newArrayList(detailsContents));
        addInStoreFraudDetails(Lists.newArrayList(inStoreDetailsList));

        new FraudDataRemoveAlreadyHandledProcessor().process(processorContext);

        assertEquals(expContentsSize, processorContext.getFraudDetailsContent().size());
        assertEquals(expContentsSize, processorContext.getFraudDetailsContent().size());
        assertEquals(expInStoreDetailsSize, processorContext.getInStoreFraudDetails().size());
    }

    @Test
    public void expectAllDetailsContentsForNoTransactionDetailsInStore() throws Exception {

        List<FraudDetailsContent> detailsContents = createTestDetailsContents();
        List<FraudDetails> inStoreDetailsList = createFraudDetails(createTestDetailsContents());

        detailsContents.add(createTransactionContent(FraudDetailsContentType.LARGE_EXPENSE));
        int expContentsSize = detailsContents.size();
        int expInStoreDetailsSize = inStoreDetailsList.size();

        addFraudDetailsContent(Lists.newArrayList(detailsContents));
        addInStoreFraudDetails(Lists.newArrayList(inStoreDetailsList));

        new FraudDataRemoveAlreadyHandledProcessor().process(processorContext);

        assertEquals(expContentsSize, processorContext.getFraudDetailsContent().size());
        assertEquals(expInStoreDetailsSize, processorContext.getInStoreFraudDetails().size());
    }

    @Test
    public void expectAllDetailsContentsForNoTransactionDetailsInBatch() throws Exception {

        List<FraudDetailsContent> detailsContents = createTestDetailsContents();
        List<FraudDetails> inStoreDetailsList = createFraudDetails(createTestDetailsContents());

        inStoreDetailsList.add(createFraudDetails(createTransactionContent(FraudDetailsContentType.LARGE_EXPENSE)));
        int expContentsSize = detailsContents.size();
        int expInStoreDetailsSize = inStoreDetailsList.size();

        addFraudDetailsContent(Lists.newArrayList(detailsContents));
        addInStoreFraudDetails(Lists.newArrayList(inStoreDetailsList));

        new FraudDataRemoveAlreadyHandledProcessor().process(processorContext);

        assertEquals(expContentsSize, processorContext.getFraudDetailsContent().size());
        assertEquals(expInStoreDetailsSize, processorContext.getInStoreFraudDetails().size());
    }

    @Test
    public void filterSameTransactionInDetailsContent() throws Exception {

        List<FraudDetailsContent> detailsContents = createTestDetailsContents();
        detailsContents.add(createTransactionContent(FraudDetailsContentType.LARGE_EXPENSE));

        List<FraudDetails> inStoreDetailsList = createFraudDetails(createTestDetailsContents());
        inStoreDetailsList.add(createFraudDetails(createTransactionContent(FraudDetailsContentType.LARGE_EXPENSE)));

        int expContentsSize = detailsContents.size() - 1;
        int expInStoreDetailsSize = inStoreDetailsList.size();

        addFraudDetailsContent(Lists.newArrayList(detailsContents));
        addInStoreFraudDetails(Lists.newArrayList(inStoreDetailsList));

        new FraudDataRemoveAlreadyHandledProcessor().process(processorContext);

        assertEquals(expContentsSize, processorContext.getFraudDetailsContent().size());
        assertEquals(expInStoreDetailsSize, processorContext.getInStoreFraudDetails().size());
    }

    @Test
    public void expectAllDetailsContentsForDifferentTransactionsType() throws Exception {

        List<FraudDetailsContent> detailsContents = createTestDetailsContents();
        List<FraudDetails> inStoreDetailsList = createFraudDetails(createTestDetailsContents());

        detailsContents.add(createTransactionContent(FraudDetailsContentType.LARGE_EXPENSE));
        inStoreDetailsList.add(createFraudDetails(createTransactionContent(FraudDetailsContentType.DOUBLE_CHARGE)));
        int expContentsSize = detailsContents.size();
        int expInStoreDetailsSize = inStoreDetailsList.size();

        addFraudDetailsContent(Lists.newArrayList(detailsContents));
        addInStoreFraudDetails(Lists.newArrayList(inStoreDetailsList));

        new FraudDataRemoveAlreadyHandledProcessor().process(processorContext);

        assertEquals(expContentsSize, processorContext.getFraudDetailsContent().size());
        assertEquals(expInStoreDetailsSize, processorContext.getInStoreFraudDetails().size());
    }

    @Test
    public void expectAllDetailsContentsForDifferentTransactionsDescription() throws Exception {

        List<FraudDetailsContent> detailsContents = createTestDetailsContents();
        List<FraudDetails> inStoreDetailsList = createFraudDetails(createTestDetailsContents());

        detailsContents.add(createTransactionContent(FraudDetailsContentType.LARGE_EXPENSE));

        FraudTransactionContent transactionContent = createTransactionContent(FraudDetailsContentType.LARGE_EXPENSE);
        transactionContent.getTransactions().get(0).setDescription("another description");
        inStoreDetailsList.add(createFraudDetails(transactionContent));

        int expContentsSize = detailsContents.size();
        int expInStoreDetailsSize = inStoreDetailsList.size();

        addFraudDetailsContent(Lists.newArrayList(detailsContents));
        addInStoreFraudDetails(Lists.newArrayList(inStoreDetailsList));

        new FraudDataRemoveAlreadyHandledProcessor().process(processorContext);

        assertEquals(expContentsSize, processorContext.getFraudDetailsContent().size());
        assertEquals(expInStoreDetailsSize, processorContext.getInStoreFraudDetails().size());
    }

    @Test
    public void filterDetailsContentsForSameTransactionsTypeAndDescription() throws Exception {

        List<FraudDetailsContent> detailsContents = createTestDetailsContents();
        List<FraudDetails> inStoreDetailsList = createFraudDetails(createTestDetailsContents());

        detailsContents.add(createTransactionContent(FraudDetailsContentType.LARGE_EXPENSE));
        inStoreDetailsList.add(createFraudDetails(createTransactionContent(FraudDetailsContentType.LARGE_EXPENSE)));
        int expContentsSize = detailsContents.size() - 1;
        int expInStoreDetailsSize = inStoreDetailsList.size();

        addFraudDetailsContent(Lists.newArrayList(detailsContents));
        addInStoreFraudDetails(Lists.newArrayList(inStoreDetailsList));

        new FraudDataRemoveAlreadyHandledProcessor().process(processorContext);

        assertEquals(expContentsSize, processorContext.getFraudDetailsContent().size());
        assertEquals(expInStoreDetailsSize, processorContext.getInStoreFraudDetails().size());
    }

    @Test
    public void notFilterDetailsContentsForSameTransactionsTypeAndOneNotSameDescription() throws Exception {
        List<FraudDetails> inStoreDetailsList = Collections.singletonList(createFraudDetails(
                createTransactionContent(FraudDetailsContentType.LARGE_EXPENSE, "transactionDescription1",
                        "transactionDescription2")));

        List<FraudDetailsContent> detailsContents = Arrays.<FraudDetailsContent>asList(
                createTransactionContent(FraudDetailsContentType.LARGE_EXPENSE, "transactionDescription0",
                        "transactionDescription1"),
                createTransactionContent(FraudDetailsContentType.LARGE_EXPENSE, "transactionDescription2"));

        addInStoreFraudDetails(inStoreDetailsList);
        addFraudDetailsContent(detailsContents);
        new FraudDataRemoveAlreadyHandledProcessor().process(processorContext);

        int expInStoreDetailsSize = 1;
        int expContentsSize = 1;
        int expTransactionsInContent = 2;

        assertEquals(expInStoreDetailsSize, processorContext.getInStoreFraudDetails().size());
        assertEquals(expContentsSize, processorContext.getFraudDetailsContent().size());
        assertEquals(detailsContents.get(0).getContentId(),
                processorContext.getFraudDetailsContent().get(0).getContentId());
        assertEquals(expTransactionsInContent,
                ((FraudTransactionContent) processorContext.getFraudDetailsContent().get(0)).getTransactions().size());
    }

    private FraudTransactionContent createTransactionContent(FraudDetailsContentType type) {
        return createFraudTransactionContent(type,
                createTransaction("transactionDescription", "account1", today, -200, "categoryId"
        ));
    }

    private FraudTransactionContent createTransactionContent(FraudDetailsContentType type, String... transactionDescriptions) {
        List<Transaction> transactions = Lists.newArrayList();
        for (String transactionDescription : transactionDescriptions) {
            transactions.add(createTransaction(transactionDescription, "account1", today, -200, "categoryId"));
        }
        return createFraudTransactionContent(type, transactions);
    }

}
