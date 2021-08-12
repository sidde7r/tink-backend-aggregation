package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.creditcard.rpc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterTestData.loadTestResponse;

import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.TransactionMatcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.creditcard.entities.PaginationKey;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

public class TransactionsResponseTest {
    private static final String VIEWSTATE_8 =
            "xke6arlzmH/6rAddNQHY/G/SrdF6fqI6pwFORLKy027rrPqsBuYoRXQ4ToJkz0VlOPgJk+kWuLIcB6sQcYZXzQn9QHrrezhAN/7cdGMzb6YkPC2g5qZZ5Hpl7bpqquTN+TAayeLvxm3mfZ/lXxiwAuhPuDhDZAHmzJGt7w==";
    private static final String VIEWSTATE_9 =
            "OyPm8aiN8SQaE7EQ8rjxGsrh+4b/I+xs911jRgF2c1YheTq38/4TWduBSP1cMgLSgL+OT1olBTHpPYiXIU7+FDOCDSIJhko01EtbSuVBxnkhZX8kdUkAysnVY+Id39schMB5e2xpP/eCE0lBpL9J6Am7XAIBzIf621dPOA==";

    @Test
    public void testTransactionsResponse() {
        final TransactionsResponse response =
                loadTestResponse("8.card_transactions.xhtml", TransactionsResponse.class);

        assertEquals(VIEWSTATE_8, response.getViewState());

        final PaginationKey nextKey = response.getNextKey();
        assertNotNull(nextKey);
        assertEquals("movimientos-form:j_id1277815336_280505e6", nextKey.getSource());
        assertEquals(VIEWSTATE_8, nextKey.getViewState());

        final List<CreditCardTransaction> transactions = response.toTinkTransactions(null);
        assertEquals(19, transactions.size());
        assertThat(
                transactions.get(0),
                matchesTransaction("08/12/2019", "Bebidas De Calidad De Mad", -24.60));
        assertThat(transactions.get(1), matchesTransaction("08/12/2019", "Climiparking", -8.65));
        assertThat(transactions.get(2), matchesTransaction("08/12/2019", "Grupo Vips", -8.64));
        assertThat(
                transactions.get(3), matchesTransaction("07/12/2019", "Outlet Pc Online", -38.98));
        assertThat(
                transactions.get(4),
                matchesTransaction("07/12/2019", "Amzn Mktp Es*f48iy4yq5", -38.85));
        assertThat(
                transactions.get(5),
                matchesTransaction("06/12/2019", "Yelmo Cines Plaza Norte 2", -72.90));
        assertThat(
                transactions.get(6), matchesTransaction("06/12/2019", "Anul. App Club Vips", 1.00));
        assertThat(transactions.get(7), matchesTransaction("06/12/2019", "Grupo Vips", -10.64));
        assertThat(transactions.get(8), matchesTransaction("06/12/2019", "App Club Vips", -1.00));
        assertThat(
                transactions.get(9),
                matchesTransaction("05/12/2019", "Amzn Mktp Es*h82ag6i15", -12.97));
        assertThat(
                transactions.get(10),
                matchesTransaction("04/12/2019", "Anul. Amz*mr-trade", 12.97));
        assertThat(
                transactions.get(11),
                matchesTransaction("04/12/2019", "Amzn Mktp Es*928ij67u5", -29.90));
        assertThat(transactions.get(12), matchesTransaction("04/12/2019", "Caraguapa.org", -10.00));
        assertThat(
                transactions.get(13),
                matchesTransaction("04/12/2019", "Ballenoil Sanse Ii (madri", -8.20));
        assertThat(
                transactions.get(14),
                matchesTransaction("03/12/2019", "Mcdonalds El Juncal", -17.35));
        assertThat(
                transactions.get(15),
                matchesTransaction("03/12/2019", "Marisqueria Provencio", -11.00));
        assertThat(
                transactions.get(16), matchesTransaction("02/12/2019", "Corner Repsol 1", -4.40));
        assertThat(
                transactions.get(17),
                matchesTransaction("29/11/2019", "Grupo Hostelero Diversia", -36.95));
        assertThat(
                transactions.get(18),
                matchesTransaction("29/11/2019", "Amzn Mktp Es*wg3gn71b5", -13.29));
    }

    @Test
    public void testLastPageTransactionsResponse() {
        final TransactionsResponse response =
                loadTestResponse("9.card_transactions_last_page.xhtml", TransactionsResponse.class);
        assertEquals(VIEWSTATE_9, response.getViewState());
        assertNull(response.getNextKey());
    }

    @Test
    public void testEmptyPageTransactionsResponse() {
        final TransactionsResponse response =
                loadTestResponse("12.card_transactions_empty.xhtml", TransactionsResponse.class);
        assertEquals(0, response.toTinkTransactions(null).size());
    }

    private TransactionMatcher matchesTransaction(String date, String description, double eur) {
        return new TransactionMatcher(date, description, eur);
    }
}
