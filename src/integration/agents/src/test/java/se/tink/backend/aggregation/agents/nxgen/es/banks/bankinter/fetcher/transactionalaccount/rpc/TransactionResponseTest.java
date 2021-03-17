package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount.rpc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterTestData.loadTestResponse;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.TransactionMatcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount.entities.PaginationKey;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionResponseTest {
    private TransactionMatcher matchesTransaction(String date, String description, double eur) {
        return new TransactionMatcher(date, description, eur);
    }

    @Test
    public void testTransactionsResponse() {
        final TransactionsResponse transactionsResponse =
                loadTestResponse("4.transactions.xhtml", TransactionsResponse.class);

        List<Transaction> transactions =
                transactionsResponse.toTinkTransactions().stream().collect(Collectors.toList());

        assertEquals(8, transactions.size());

        assertThat(
                transactions.get(0),
                matchesTransaction("29/04/2020", "Transf Nomin Otr En /inclam Sa", 1051.67));
        assertThat(
                transactions.get(1),
                matchesTransaction("28/04/2020", "Nintendo Of Europe Gmbh", -7.49));
        assertThat(transactions.get(2), matchesTransaction("21/04/2020", "TestCase", 7.5));
        assertThat(
                transactions.get(3),
                matchesTransaction("14/04/2020", "Transf Nomin Otr En /inclam Sa", 450.72));
        assertThat(transactions.get(4), matchesTransaction("13/04/2020", "Masked", -29.99));
        assertThat(
                transactions.get(5),
                matchesTransaction("06/04/2020", "Pago Bizum A Anders;Andersson", -165.00));
        assertThat(transactions.get(6), matchesTransaction("06/04/2020", "TestCase", -200.00));
        assertThat(transactions.get(7), matchesTransaction("06/04/2020", "TestCase", 200.00));

        final PaginationKey nextKey = transactionsResponse.getNextKey(0);
        assertEquals("j_id1111011110_5f006392", nextKey.getSource());
    }

    @Test
    public void testNoTransactionsResponse() {
        final TransactionsResponse transactionsResponse =
                loadTestResponse("5.transactions_none.xhtml", TransactionsResponse.class);

        Collection<Transaction> transactions = transactionsResponse.toTinkTransactions();

        assertEquals(0, transactions.size());

        final PaginationKey nextKey = transactionsResponse.getNextKey(0);
        assertEquals(1, nextKey.getConsecutiveEmptyReplies());
        assertEquals("j_id1111011110_5f006392", nextKey.getSource());
    }

    @Test
    public void testHtmlErrorPageAsResponse() {
        // given
        final TransactionsResponse transactionsResponse =
                loadTestResponse("13.error_html_page.xhtml", TransactionsResponse.class);

        // then
        assertTrue(transactionsResponse.hasError());
    }
}
