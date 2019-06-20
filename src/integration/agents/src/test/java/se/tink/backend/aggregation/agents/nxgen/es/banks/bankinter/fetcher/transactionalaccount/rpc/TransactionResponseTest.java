package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount.rpc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterTestData.loadTestResponse;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.transactionalaccount.entities.PaginationKey;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

public class TransactionResponseTest {
    private static final SimpleDateFormat TRANSACTION_DATE_FORMATTER =
            new SimpleDateFormat("dd/MM/yyyy");

    private class TransactionMatcher extends TypeSafeMatcher<Transaction> {
        final String dateString;
        final String description;
        final double amountInEur;

        TransactionMatcher(String date, String description, double eur) {
            this.dateString = date;
            this.description = description;
            this.amountInEur = eur;
        }

        @Override
        protected boolean matchesSafely(Transaction transaction) {
            return transaction.getDescription().equals(description)
                    && transaction.getAmount().equals(Amount.inEUR(amountInEur))
                    && TRANSACTION_DATE_FORMATTER.format(transaction.getDate()).equals(dateString);
        }

        @Override
        protected void describeMismatchSafely(Transaction item, Description mismatchDescription) {
            mismatchDescription.appendText(
                    String.format(
                            "was (%s, '%s', %g %s)",
                            TRANSACTION_DATE_FORMATTER.format(item.getDate()),
                            item.getDescription(),
                            item.getAmount().getValue(),
                            item.getAmount().getCurrency()));
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(
                    String.format("(%s, '%s', %g EUR)", dateString, this.description, amountInEur));
        }
    }

    private TransactionMatcher matchesTransaction(String date, String description, double eur) {
        return new TransactionMatcher(date, description, eur);
    }

    @Test
    public void testTransactionsResponse() {
        final TransactionsResponse transactionsResponse =
                loadTestResponse("4.transactions.xhtml", TransactionsResponse.class);

        List<Transaction> transactions =
                transactionsResponse.toTinkTransactions().stream().collect(Collectors.toList());

        assertEquals(13, transactions.size());

        assertThat(
                transactions.get(0),
                matchesTransaction("10/06/2019", "Pago Bizum De Mengano;ramirez;tal", 3.5));
        assertThat(
                transactions.get(1),
                matchesTransaction("10/06/2019", "Pago Bizum De Maria Luisa;garcia", 3.5));
        assertThat(
                transactions.get(2),
                matchesTransaction("07/06/2019", "Pago Bizum De Hermangarda;perez De", 3.5));
        assertThat(
                transactions.get(3),
                matchesTransaction("07/06/2019", "Pago Bizum De Manuel Francisco;gon", 3.5));
        assertThat(
                transactions.get(4),
                matchesTransaction("07/06/2019", "Pago Bizum De David;marin", 3.5));
        assertThat(
                transactions.get(5),
                matchesTransaction("07/06/2019", "Pago Bizum De Samuel;delgad", 3.5));
        assertThat(
                transactions.get(6),
                matchesTransaction("07/06/2019", "Pago Bizum De Marina;soler;v", 3.5));
        assertThat(
                transactions.get(7),
                matchesTransaction("05/06/2019", "Trans /ministerio de Educacion", 3019.21));
        assertThat(
                transactions.get(8),
                matchesTransaction("04/06/2019", "Recibo Visa Clasica", -238.12));
        assertThat(
                transactions.get(9),
                matchesTransaction("04/06/2019", "Trans /perez De tal Z", (double) 500));
        assertThat(
                transactions.get(10),
                matchesTransaction("04/06/2019", "Trans /ministerio de Educacion", 1337.42));
        assertThat(
                transactions.get(11),
                matchesTransaction("03/06/2019", "Recib /c.p. Rufino Blanco 42", -312.25));
        assertThat(
                transactions.get(12),
                matchesTransaction("03/06/2019", "Recibo /qualitas", -115.12));

        final PaginationKey nextKey = transactionsResponse.getNextKey(0);
        assertEquals("j_id374401928_5f006346:j_id374401928_5f006392", nextKey.getSource());
    }

    @Test
    public void testNoTransactionsResponse() {
        final TransactionsResponse transactionsResponse =
                loadTestResponse("5.transactions_none.xhtml", TransactionsResponse.class);

        Collection<Transaction> transactions = transactionsResponse.toTinkTransactions();

        assertEquals(0, transactions.size());

        final PaginationKey nextKey = transactionsResponse.getNextKey(0);
        assertEquals(1, nextKey.getConsecutiveEmptyReplies());
        assertEquals("j_id374401928_5f006346:j_id374401928_5f006392", nextKey.getSource());
    }
}
