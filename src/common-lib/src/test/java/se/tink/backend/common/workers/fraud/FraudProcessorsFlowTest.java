package se.tink.backend.common.workers.fraud;

import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.repository.mysql.main.FraudDetailsContentRepository;
import se.tink.backend.common.workers.fraud.processors.FraudDataBlockedIdentityProcessor;
import se.tink.backend.common.workers.fraud.processors.FraudDataDeduplicationProcessor;
import se.tink.backend.common.workers.fraud.processors.FraudDataEmptyStatesProcessor;
import se.tink.backend.common.workers.fraud.processors.FraudDataFrequentAccountActivityProcessor;
import se.tink.backend.common.workers.fraud.processors.FraudDataLargeWithdrawalProcessor;
import se.tink.backend.common.workers.fraud.processors.FraudDataNewDetailsProcessor;
import se.tink.backend.common.workers.fraud.processors.FraudDataProcessor;
import se.tink.backend.common.workers.fraud.processors.FraudDataRemoveAlreadyHandledProcessor;
import se.tink.backend.common.workers.fraud.processors.FraudDataRemoveDetailsProcessor;
import se.tink.backend.common.workers.fraud.processors.FraudDataTransformActivityToFraudDetailsProcessor;
import se.tink.backend.common.workers.fraud.processors.FraudDataTransformContentToFraudDetailsProcessor;
import se.tink.backend.common.workers.fraud.processors.FraudDataUpdateItemsProcessor;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsContent;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.FraudIdentityContent;
import se.tink.backend.core.Transaction;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FraudProcessorsFlowTest extends AbstractFraudProcessTest {
    private ServiceContext serviceContext;

    @Before
    public void setUp() throws Exception {
        mockData();
    }

    @Test
    public void testSimpleFlowForChangingUserName() {
        List<FraudDetailsContent> detailsContents = createTestDetailsContents();
        String newName = "New name";
        String expName = changeName(detailsContents, newName);
        Assert.assertNotEquals(expName, newName);

        List<FraudDetails> inStoreDetailsList = createFraudDetails(detailsContents);
        addInStoreFraudDetails(Lists.newArrayList(inStoreDetailsList));

        executeProcessors();

        int expContentSize = 1;
        int expRemoveSize = 0;
        int expUpdateSize = 0;

        assertSize(expContentSize, expRemoveSize, expUpdateSize);
        assertDetailsContent(expName);
    }

    @Test
    public void testFullFlowForChangingUserName() throws Exception {
        addDataToRunEveryProcessor();

        List<FraudDetailsContent> detailsContents = createTestDetailsContents();
        detailsContents.add(createFraudTransactionContent(FraudDetailsContentType.LARGE_EXPENSE,
                (Transaction) processorContext.getActivities().get(0).getContent()));

        String newName = "New name";
        String expName = changeName(detailsContents, newName);
        Assert.assertNotEquals(expName, newName);

        List<FraudDetails> inStoreDetailsList = createFraudDetails(detailsContents);
        addInStoreFraudDetails(Lists.newArrayList(inStoreDetailsList));

        executeProcessors();

        int expContentSize = 1;
        int expRemoveSize = 0;
        int expUpdateSize = 0;

        assertSize(expContentSize, expRemoveSize, expUpdateSize);
        assertDetailsContent(expName);
    }

    private void assertSize(int expContentSize, int expRemoveSize, int expUpdateSize) {
        Assert.assertEquals("Incorrect FraudDetailsContent size", expContentSize, processorContext.getFraudDetailsContent().size());
        Assert.assertEquals("Incorrect FraudDetailsRemoveList size", expRemoveSize, processorContext.getFraudDetailsRemoveList().size());
        Assert.assertEquals("Incorrect FraudDetailsUpdateList size", expUpdateSize, processorContext.getFraudDetailsUpdateList().size());
    }

    private void assertDetailsContent(String expName) {
        Assert.assertTrue(
                "Expected instance of FraudIdentityContent, found: " + processorContext.getFraudDetailsContent()
                        .getClass().getSimpleName(),
                processorContext.getFraudDetailsContent().get(0) instanceof FraudIdentityContent);
        Assert.assertEquals(expName,
                ((FraudIdentityContent) processorContext.getFraudDetailsContent().get(0)).getFirstName());
    }

    private void executeProcessors() {
        List<FraudDataProcessor> processors = getTestProcessors();
        for (FraudDataProcessor processor : processors) {
            processor.process(processorContext);
        }
    }

    private void mockData() {
        serviceContext = mock(ServiceContext.class);
        when(serviceContext.getCacheClient()).thenReturn(mock(CacheClient.class));

        FraudDetailsContentRepository fraudDetailsContentRepository = mock(FraudDetailsContentRepository.class);

        when(serviceContext.getRepository(FraudDetailsContentRepository.class))
                .thenReturn(fraudDetailsContentRepository);
        when(fraudDetailsContentRepository.findByUserId(anyString(), any(CacheClient.class)))
                .thenReturn(createTestDetailsContents());
    }

    private String changeName(List<FraudDetailsContent> detailsContents, String newName) {
        for (FraudDetailsContent detailsContent : detailsContents) {
            if (detailsContent instanceof FraudIdentityContent) {
                String oldName = ((FraudIdentityContent) detailsContent).getFirstName();
                ((FraudIdentityContent) detailsContent).setFirstName(newName);
                return oldName;
            }
        }

        return null;
    }

    /**
     * The same processors as in `FraudProcessorWorker`. The reason of creation them manually is
     */
    protected List<FraudDataProcessor> getTestProcessors() {
        List<FraudDataProcessor> processors = Lists.newArrayList();

        // Create FraudDetailsContent to context.

        processors.add(new FraudDataTransformActivityToFraudDetailsProcessor());
        processors.add(new FraudDataTransformContentToFraudDetailsProcessor(serviceContext));
        processors.add(new FraudDataFrequentAccountActivityProcessor());
        processors.add(new FraudDataLargeWithdrawalProcessor());
        processors.add(new FraudDataBlockedIdentityProcessor());

        // Remove duplicates and create new FraudDetails if any new.

        processors.add(new FraudDataRemoveAlreadyHandledProcessor());
        processors.add(new FraudDataRemoveDetailsProcessor());
        processors.add(new FraudDataDeduplicationProcessor());
        processors.add(new FraudDataNewDetailsProcessor());
        processors.add(new FraudDataEmptyStatesProcessor());

        // Update FraudItems based on new FraudDetails.

        processors.add(new FraudDataUpdateItemsProcessor());
        return processors;
    }

}
