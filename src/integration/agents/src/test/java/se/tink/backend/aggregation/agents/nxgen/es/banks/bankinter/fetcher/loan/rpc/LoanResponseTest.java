package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.loan.rpc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterTestData.loadTestResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.TransactionMatcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.loan.entities.PaginationKey;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.date.DateUtils;

public class LoanResponseTest {
    private TransactionMatcher matchesTransaction(String date, double eur) {
        return new TransactionMatcher(date, "", eur);
    }

    @Test
    public void testLoanResponse() {
        final LoanResponse response = loadTestResponse("10.loan.xhtml", LoanResponse.class);
        final LoanAccount loanAccount = response.toLoanAccount();
        final LoanDetails loanDetails = loanAccount.getDetails();
        assertNotNull(loanDetails);

        assertEquals("ES3001281337851111111111", loanAccount.getAccountNumber());
        assertEquals("ES3001281337851111111111", loanDetails.getLoanNumber());
        assertEquals("ES30 0128 1337 8511 1111 1111", loanAccount.getName());

        final List<String> applicants = loanDetails.getApplicants();
        assertEquals(2, applicants.size());
        assertEquals("Fulano Pérez de Tal", applicants.get(0));
        assertEquals("Perengana García de Cual", applicants.get(1));

        assertNotNull(loanAccount.getInterestRate());
        assertEquals(0.0119, loanAccount.getInterestRate(), 0.00001);

        assertNotNull(loanDetails.getInitialBalance());
        assertEquals(-170000.00d, loanDetails.getInitialBalance().getDoubleValue(), 0.001);

        assertNotNull(loanAccount.getExactBalance());
        assertEquals(-116525.73d, loanAccount.getExactBalance().getDoubleValue(), 0.001);

        assertNotNull(loanDetails.getNumMonthsBound());
        assertEquals(12, loanDetails.getNumMonthsBound().intValue());

        assertNotNull(loanDetails.getExactMonthlyAmortization());
        assertEquals(445.38d, loanDetails.getExactMonthlyAmortization().getDoubleValue(), 0.001);

        assertEquals(
                LocalDate.of(2015, 3, 13),
                DateUtils.toJavaTimeLocalDate(loanDetails.getInitialDate()));

        final Optional<PaginationKey> paginationKey =
                loanAccount.getFromTemporaryStorage(
                        StorageKeys.FIRST_PAGINATION_KEY, PaginationKey.class);
        assertTrue(paginationKey.isPresent());

        assertEquals(0, paginationKey.get().getSkip());
        assertEquals(3, paginationKey.get().getOffset());
        assertEquals("prestaForm:j_id470190521_4_1cf1b38d", paginationKey.get().getSource());
        assertEquals(
                "Z9/cO6Hzj4tmh/CS9qVOwiOQ2DHhZxLo3cXv0OYWaULpAH744kGIfnAB/aEvwX0ynAkN984L63w2BpEuFn5mT9a0WQwKnSyTW6WU8YPpp8osq4DypZIVDBYv9LLlvYlrUxpUqiLWrppHqafanm9JKMj2fQ==",
                paginationKey.get().getViewState());

        final List<? extends Transaction> transactions = response.toTinkTransactions(0);
        assertEquals(4, transactions.size());

        assertThat(transactions.get(0), matchesTransaction("13/12/2019", 445.38));
        assertThat(transactions.get(1), matchesTransaction("13/11/2019", 445.38));
        assertThat(transactions.get(2), matchesTransaction("13/10/2019", 445.38));
        assertThat(transactions.get(3), matchesTransaction("13/09/2019", 445.38));

        assertEquals(4, response.getNextPaginationKey().getSkip());
    }

    @Test
    public void testLoanResponseLastPage() {
        final LoanResponse response =
                loadTestResponse("11.loan_last_page.xhtml", LoanResponse.class);
        final List<? extends Transaction> transactions = response.toTinkTransactions(0);

        assertEquals(19, transactions.size());
        assertNull(response.getNextPaginationKey());
    }
}
