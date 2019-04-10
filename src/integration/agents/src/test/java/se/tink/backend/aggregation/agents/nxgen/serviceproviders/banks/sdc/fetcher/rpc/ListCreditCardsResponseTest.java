package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities.SdcCreditCardEntity;

public class ListCreditCardsResponseTest {
    @Test
    public void listCreditCardResponse() throws Exception {

        ListCreditCardsResponse response =
                ListCreditCardsResponseTestData.getTestDataOneCreditCard();
        assertNotNull(response);
        List<SdcCreditCardEntity> creditCards = response.getCreditCards();

        assertEquals(1, creditCards.size());
    }

    @Test
    public void getCreditCardsForTwoCardsPerAccountPickFirst() throws Exception {
        ListCreditCardsResponse response =
                ListCreditCardsResponseTestData.getTestDataTwoCreditCardsForOneAccountPickFirst();
        assertNotNull(response);
        List<SdcCreditCardEntity> creditCards = response.getCreditCards();

        assertEquals(1, creditCards.size());
        assertEquals("776615xxxxxx3855", creditCards.get(0).getCreditcardNumber());
    }

    @Test
    public void getCreditCardsForTwoCardsPerAccountPickSecond() throws Exception {
        ListCreditCardsResponse response =
                ListCreditCardsResponseTestData.getTestDataTwoCreditCardsForOneAccountPickSecond();
        assertNotNull(response);
        List<SdcCreditCardEntity> creditCards = response.getCreditCards();

        assertEquals(1, creditCards.size());
        assertEquals("665549xxxxxx9444", creditCards.get(0).getCreditcardNumber());
    }

    @Test
    public void getCreditCardsForTwoCardsPerAccountFirstEndDateNull() throws Exception {
        ListCreditCardsResponse response =
                ListCreditCardsResponseTestData
                        .getTestDataTwoCreditCardsForOneAccountFirstEndDateNull();
        assertNotNull(response);
        List<SdcCreditCardEntity> creditCards = response.getCreditCards();

        assertEquals(1, creditCards.size());
        assertEquals("665549xxxxxx9444", creditCards.get(0).getCreditcardNumber());
    }

    @Test
    public void getCreditCardsForTwoCardsPerAccountSecondEndDateNull() throws Exception {
        ListCreditCardsResponse response =
                ListCreditCardsResponseTestData
                        .getTestDataTwoCreditCardsForOneAccountSecondEndDateNull();
        assertNotNull(response);
        List<SdcCreditCardEntity> creditCards = response.getCreditCards();

        assertEquals(1, creditCards.size());
        assertEquals("776615xxxxxx3855", creditCards.get(0).getCreditcardNumber());
    }

    @Test
    public void getCreditCardsForTwoCardsAndTwoAccounts() throws Exception {
        ListCreditCardsResponse response =
                ListCreditCardsResponseTestData.getTestDataTwoCreditCardsAndTwoAccounts();
        assertNotNull(response);
        List<SdcCreditCardEntity> creditCards = response.getCreditCards();

        boolean foundCard1 = false;
        boolean foundCard2 = false;
        assertEquals(2, creditCards.size());
        for (SdcCreditCardEntity creditCard : creditCards) {
            if (creditCard.getCreditcardNumber().equals("776615xxxxxx3855")) {
                System.out.println("Found 776615xxxxxx3855");
                foundCard1 = true;
            } else if (creditCard.getCreditcardNumber().equals("665549xxxxxx9444")) {
                System.out.println("Found 665549xxxxxx9444");
                foundCard2 = true;
            }
        }
        assertTrue(foundCard1 && foundCard2);
    }
}
