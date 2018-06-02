package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc;

import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities.SdcCreditCardEntity;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ListCreditCardsResponseTest {
    @Test
    public void listCreditCardResponse() throws Exception {

        ListCreditCardsResponse response = ListCreditCardsResponseTestData.getTestData();
        assertNotNull(response);
        List<SdcCreditCardEntity> creditCards = response.getCreditCards();

        assertEquals(1, creditCards.size());
    }
}
