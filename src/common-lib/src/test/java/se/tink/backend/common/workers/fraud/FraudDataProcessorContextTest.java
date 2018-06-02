package se.tink.backend.common.workers.fraud;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import se.tink.backend.core.FraudDetailsContent;
import se.tink.backend.core.FraudIdentityContent;
import se.tink.backend.core.FraudTransactionContent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FraudDataProcessorContextTest {

    @Test
    public void doNotChangeFraudContentListOnGettingList() {
        FraudDataProcessorContext fraudDataContext = new FraudDataProcessorContext();
        fraudDataContext.setFraudDetailsContent(fraudDetailsList(new FraudTransactionContent()));
        fraudDataContext.getFraudDetailsContent().add(new FraudIdentityContent());

        int expListSize = 1;
        assertEquals(expListSize, fraudDataContext.getFraudDetailsContent().size());
        assertTrue(fraudDataContext.getFraudDetailsContent().get(0) instanceof FraudTransactionContent);
    }

    @Test
    public void doNotAddFraudContentWithUsedTransactionId() {
        FraudDataProcessorContext fraudDataContext = new FraudDataProcessorContext();
        fraudDataContext.setFraudDetailsContent(fraudDetailsList(createTransactionContent("transactionId1")));
        fraudDataContext.addFraudDetailsContent(fraudDetailsList(createTransactionContent("transactionId2")));
        fraudDataContext
                .addFraudDetailsContent(fraudDetailsList(createTransactionContent("transactionId1")));

        int expListSize = 2;
        assertEquals(expListSize, fraudDataContext.getFraudDetailsContent().size());
        assertThat(fraudDataContext.getUsedTransactionIds()).containsOnly("transactionId1", "transactionId2");
    }

    @Test
    public void doNotAddFraudContentWithOneUsedTransactionId() {
        FraudDataProcessorContext fraudDataContext = new FraudDataProcessorContext();
        fraudDataContext.addFraudDetailsContent(fraudDetailsList(createTransactionContent("transactionId1")));
        fraudDataContext.addFraudDetailsContent(fraudDetailsList(createTransactionContent("transactionId2")));
        fraudDataContext.addFraudDetailsContent(fraudDetailsList(
                createTransactionContent("transactionId0", "transactionId1", "transactionId3")));

        int expListSize = 2;
        assertEquals(expListSize, fraudDataContext.getFraudDetailsContent().size());
        assertThat(fraudDataContext.getUsedTransactionIds()).containsOnly("transactionId1", "transactionId2");
    }

    @Test
    public void resetFraudContentForSetMethod() {
        FraudDataProcessorContext fraudDataContext = new FraudDataProcessorContext();
        fraudDataContext.setFraudDetailsContent(fraudDetailsList(
                createTransactionContent("transactionId1"),
                createTransactionContent("transactionId2"),
                createTransactionContent("transactionId3"),
                createTransactionContent("transactionId4")));
        fraudDataContext.setFraudDetailsContent(fraudDetailsList(
                createTransactionContent("transactionId4"),
                createTransactionContent("transactionId5")));

        int expListSize = 2;
        assertEquals(expListSize, fraudDataContext.getFraudDetailsContent().size());
        assertThat(fraudDataContext.getUsedTransactionIds()).containsOnly("transactionId4", "transactionId5");
    }

    private List<FraudDetailsContent> fraudDetailsList(FraudDetailsContent... fraudDetailsContents) {
        return Arrays.asList(fraudDetailsContents);
    }

    private FraudTransactionContent createTransactionContent(final String... transactionIds) {
        return new FraudTransactionContent() {{
            setTransactionIds(Arrays.asList(transactionIds));
        }};
    }
}
