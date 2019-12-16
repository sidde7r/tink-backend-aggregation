package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.fetcher.loan.rpc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.BankinterTestData.loadTestResponse;

import java.time.LocalDate;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.libraries.date.DateUtils;

public class LoanResponseTest {

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
        assertEquals(0.0119, loanAccount.getInterestRate().doubleValue(), 0.00001);

        assertNotNull(loanDetails.getInitialBalance());
        assertEquals(-170000.00d, loanDetails.getInitialBalance().doubleValue(), 0.001);

        assertNotNull(loanAccount.getExactBalance());
        assertEquals(-116525.73d, loanAccount.getExactBalance().getDoubleValue(), 0.001);

        assertNotNull(loanDetails.getNumMonthsBound());
        assertEquals(12, loanDetails.getNumMonthsBound().intValue());

        assertNotNull(loanDetails.getExactMonthlyAmortization());
        assertEquals(445.38d, loanDetails.getExactMonthlyAmortization().getDoubleValue(), 0.001);

        assertEquals(
                LocalDate.of(2015, 3, 13),
                DateUtils.toJavaTimeLocalDate(loanDetails.getInitialDate()));
    }
}
