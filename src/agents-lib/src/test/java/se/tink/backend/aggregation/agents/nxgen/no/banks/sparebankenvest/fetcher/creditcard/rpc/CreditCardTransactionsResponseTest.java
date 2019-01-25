package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.creditcard.rpc;

import java.util.Calendar;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.libraries.serialization.utils.SerializationUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CreditCardTransactionsResponseTest {
    @Test
    public void testPagination() throws Exception {
        CreditCardTransactionsResponse response =
                SerializationUtils.deserializeFromString(TEST_DATA_PAGINATION, CreditCardTransactionsResponse.class);
        assertTrue(response.hasMoreTransactions());
        assertTrue(100 == response.getNextStartOffset());
    }

    @Test
    public void testParseActualResponse() throws Exception {
        CreditCardTransactionsResponse response =
                SerializationUtils.deserializeFromString(TEST_DATA_PARSE, CreditCardTransactionsResponse.class);

        assertFalse(response.hasMoreTransactions());
        assertTrue(50 == response.getNextStartOffset());
        List<CreditCardTransaction> transactions = response.getTinkTransactions();
        assertEquals(11, response.getTinkTransactions().size());
        transactions.stream()
                .forEach(tx -> {
                    assertEquals(TransactionTypes.CREDIT_CARD, tx.getType());
                    assertEquals("NOK", tx.getAmount().getCurrency());
                    assertTrue(0 != tx.getAmount().getValue());
                    assertNotNull(tx.getDate());
                    assertNotNull(tx.getDescription());
                });
        Calendar cExpect = Calendar.getInstance();
        cExpect.set(Calendar.YEAR, 2018);
        cExpect.set(Calendar.MONTH, 2);
        cExpect.set(Calendar.DATE, 8);

        Calendar cActual = Calendar.getInstance();
        cActual.setTime(transactions.get(0).getDate());

        assertEquals(cExpect.get(Calendar.YEAR), cActual.get(Calendar.YEAR));
        assertEquals(cExpect.get(Calendar.MONTH), cActual.get(Calendar.MONTH));
        assertEquals(cExpect.get(Calendar.DATE), cActual.get(Calendar.DATE));
    }

    private static final String TEST_DATA_PAGINATION = "{"
            + "\"totalCount\": 118,"
            + "\"start\": 50,"
            + "\"step\": 50,"
            + "\"list\": [],"
            + "\"groupCount\": {"
            + "\"accountsCards\": {}"
            + "}"
            + "}";

    private static final String TEST_DATA_PARSE = "{"
            + "\"totalCount\": 11,"
            + "\"start\": 0,"
            + "\"step\": 50,"
            + "\"list\": [{"
            + "\"id\": 232332,"
            + "\"transtype\": \"Korttrans\","
            + "\"kundenummer\": \"121212\","
            + "\"kortKontonummer\": \"2323232      \","
            + "\"periode\": null,"
            + "\"bokfoeringsdato\": \"2018-03-08T00:00:00\","
            + "\"tidspunkt\": null,"
            + "\"valutadato\": \"2018-03-07T00:00:00\","
            + "\"belop\": -101.02,"
            + "\"alfareferanse\": \"PAYPAL *AVID SHOP        2323232  LU\","
            + "\"valuta\": \"NOK\","
            + "\"basisBelop\": -101.02,"
            + "\"kategori\": \"Varekjøp         \","
            + "\"visningsEnhet\": 100,"
            + "\"beskrivelse\": null"
            + "}, {"
            + "\"id\": 23233232,"
            + "\"transtype\": \"Korttrans\","
            + "\"kundenummer\": \"23233\","
            + "\"kortKontonummer\": \"2121221      \","
            + "\"periode\": null,"
            + "\"bokfoeringsdato\": \"2018-03-06T00:00:00\","
            + "\"tidspunkt\": null,"
            + "\"valutadato\": \"2018-03-05T00:00:00\","
            + "\"belop\": -10.00,"
            + "\"alfareferanse\": \"ITUNES.COM/BILL          ITUNES.COM   IE\","
            + "\"valuta\": \"NOK\","
            + "\"basisBelop\": -10.00,"
            + "\"kategori\": \"Varekjøp         \","
            + "\"visningsEnhet\": 100,"
            + "\"beskrivelse\": null"
            + "}, {"
            + "\"id\": 34334343,"
            + "\"transtype\": \"Korttrans\","
            + "\"kundenummer\": \"3434343434\","
            + "\"kortKontonummer\": \"3434343434      \","
            + "\"periode\": null,"
            + "\"bokfoeringsdato\": \"2018-03-02T00:00:00\","
            + "\"tidspunkt\": null,"
            + "\"valutadato\": \"2018-03-01T00:00:00\","
            + "\"belop\": -590.00,"
            + "\"alfareferanse\": \"ADOBE  CREATIVE CLOUD    ADOBE.COM    IE\","
            + "\"valuta\": \"NOK\","
            + "\"basisBelop\": -590.00,"
            + "\"kategori\": \"Varekjøp         \","
            + "\"visningsEnhet\": 100,"
            + "\"beskrivelse\": null"
            + "}, {"
            + "\"id\": 43534545,"
            + "\"transtype\": \"Korttrans\","
            + "\"kundenummer\": \"4545454\","
            + "\"kortKontonummer\": \"45454545      \","
            + "\"periode\": null,"
            + "\"bokfoeringsdato\": \"2018-02-28T00:00:00\","
            + "\"tidspunkt\": null,"
            + "\"valutadato\": \"2018-02-28T00:00:00\","
            + "\"belop\": -129.18,"
            + "\"alfareferanse\": \"RENTETRANSAKSJON         \","
            + "\"valuta\": \"NOK\","
            + "\"basisBelop\": -129.18,"
            + "\"kategori\": \"RENTETRANSAKSJON \","
            + "\"visningsEnhet\": 100,"
            + "\"beskrivelse\": null"
            + "}, {"
            + "\"id\": 45454545,"
            + "\"transtype\": \"Korttrans\","
            + "\"kundenummer\": \"45454454\","
            + "\"kortKontonummer\": \"45454545      \","
            + "\"periode\": null,"
            + "\"bokfoeringsdato\": \"2018-02-27T00:00:00\","
            + "\"tidspunkt\": null,"
            + "\"valutadato\": \"2018-02-26T00:00:00\","
            + "\"belop\": -200.36,"
            + "\"alfareferanse\": \"LYNDA.COM, INC.          888-67676767  US\","
            + "\"valuta\": \"USD\","
            + "\"basisBelop\": -24.99,"
            + "\"kategori\": \"Varekjøp         \","
            + "\"visningsEnhet\": 1,"
            + "\"beskrivelse\": null"
            + "}, {"
            + "\"id\": 67676676,"
            + "\"transtype\": \"Korttrans\","
            + "\"kundenummer\": \"67676767\","
            + "\"kortKontonummer\": \"67676767      \","
            + "\"periode\": null,"
            + "\"bokfoeringsdato\": \"2018-02-26T00:00:00\","
            + "\"tidspunkt\": null,"
            + "\"valutadato\": \"2018-02-23T00:00:00\","
            + "\"belop\": -139.00,"
            + "\"alfareferanse\": \"NETFLIX.COM              67676776     NL\","
            + "\"valuta\": \"NOK\","
            + "\"basisBelop\": -139.00,"
            + "\"kategori\": \"Varekjøp         \","
            + "\"visningsEnhet\": 100,"
            + "\"beskrivelse\": null"
            + "}, {"
            + "\"id\": 7878787,"
            + "\"transtype\": \"Korttrans\","
            + "\"kundenummer\": \"7878788787\","
            + "\"kortKontonummer\": \"787878787878      \","
            + "\"periode\": null,"
            + "\"bokfoeringsdato\": \"2018-02-23T00:00:00\","
            + "\"tidspunkt\": null,"
            + "\"valutadato\": \"2018-02-22T00:00:00\","
            + "\"belop\": -67.91,"
            + "\"alfareferanse\": \"AMAZON DIGITAL SVCS 78787878 US\","
            + "\"valuta\": \"USD\","
            + "\"basisBelop\": -8.44,"
            + "\"kategori\": \"Varekjøp         \","
            + "\"visningsEnhet\": 1,"
            + "\"beskrivelse\": null"
            + "}, {"
            + "\"id\": 898989,"
            + "\"transtype\": \"Korttrans\","
            + "\"kundenummer\": \"89898998\","
            + "\"kortKontonummer\": \"898989898      \","
            + "\"periode\": null,"
            + "\"bokfoeringsdato\": \"2018-02-22T00:00:00\","
            + "\"tidspunkt\": null,"
            + "\"valutadato\": \"2018-02-21T00:00:00\","
            + "\"belop\": -99.00,"
            + "\"alfareferanse\": \"ITUNES.COM/BILL          ITUNES.COM   IE\","
            + "\"valuta\": \"NOK\","
            + "\"basisBelop\": -99.00,"
            + "\"kategori\": \"Varekjøp         \","
            + "\"visningsEnhet\": 100,"
            + "\"beskrivelse\": null"
            + "}, {"
            + "\"id\": 144141414,"
            + "\"transtype\": \"Korttrans\","
            + "\"kundenummer\": \"14141414141\","
            + "\"kortKontonummer\": \"1414141141      \","
            + "\"periode\": null,"
            + "\"bokfoeringsdato\": \"2018-02-21T00:00:00\","
            + "\"tidspunkt\": null,"
            + "\"valutadato\": \"2018-02-21T00:00:00\","
            + "\"belop\": 1500.00,"
            + "\"alfareferanse\": \"Innbetaling                             \","
            + "\"valuta\": \"NOK\","
            + "\"basisBelop\": 1500.00,"
            + "\"kategori\": \"Innbetaling      \","
            + "\"visningsEnhet\": 100,"
            + "\"beskrivelse\": null"
            + "}, {"
            + "\"id\": 26262662,"
            + "\"transtype\": \"Korttrans\","
            + "\"kundenummer\": \"2626262626\","
            + "\"kortKontonummer\": \"2626266      \","
            + "\"periode\": null,"
            + "\"bokfoeringsdato\": \"2018-02-19T00:00:00\","
            + "\"tidspunkt\": null,"
            + "\"valutadato\": \"2018-02-18T00:00:00\","
            + "\"belop\": -59.00,"
            + "\"alfareferanse\": \"JOTTACLOUD NOK           OSLO         NO\","
            + "\"valuta\": \"NOK\","
            + "\"basisBelop\": -59.00,"
            + "\"kategori\": \"Varekjøp         \","
            + "\"visningsEnhet\": 100,"
            + "\"beskrivelse\": null"
            + "}, {"
            + "\"id\": 3733737,"
            + "\"transtype\": \"Korttrans\","
            + "\"kundenummer\": \"3737373773\","
            + "\"kortKontonummer\": \"373737373      \","
            + "\"periode\": null,"
            + "\"bokfoeringsdato\": \"2018-02-19T00:00:00\","
            + "\"tidspunkt\": null,"
            + "\"valutadato\": \"2018-02-18T00:00:00\","
            + "\"belop\": -90.00,"
            + "\"alfareferanse\": \"PAYPAL *MICROSOFT OFFI   5858585858  LU\","
            + "\"valuta\": \"NOK\","
            + "\"basisBelop\": -90.00,"
            + "\"kategori\": \"Varekjøp         \","
            + "\"visningsEnhet\": 100,"
            + "\"beskrivelse\": null"
            + "}],"
            + "\"groupCount\": {"
            + "\"accountsCards\": {}"
            + "}"
            + "}";
}
