package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher;

import java.util.Collection;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.HandelsbankenFIAuthenticatedTest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.HandelsbankenFITestConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.HandelsbankenLoanFetcher;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.system.rpc.Loan;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static se.tink.backend.aggregation.utils.IsNot0Matcher.isNot0;

public class HandelsbankenFILoanFetcherTest extends HandelsbankenFIAuthenticatedTest {

    @Test
    public void canFetchLoans() throws Exception {
        autoAuthenticator.autoAuthenticate();

        HandelsbankenLoanFetcher fetcher = new HandelsbankenLoanFetcher(client, sessionStorage, credentials);
        Collection<LoanAccount> loanAccounts = fetcher.fetchAccounts();

        assertNotNull(loanAccounts);
        assertTrue(loanAccounts.isEmpty());
        //Next assertions will be active if test data actually has loans. Assertions are based on SE loans
        loanAccounts.forEach(a -> {
            Loan loan = a.getDetails().toSystemLoan(a);
            assertAccountAttributes(a);
            assertLoanAttributes(loan);
            assertAccountAndLoanMatch(a, loan);
        });
    }

    private void assertAccountAttributes(LoanAccount account) {
        assertThat("Account must have account number", account.getAccountNumber(), notNullValue());
        assertThat("Account must have name", account.getName(), notNullValue());
        assertThat("Account must have loan type", account.getType(), is(AccountTypes.LOAN));
        assertThat("Account must have balance", account.getBalance(), notNullValue());
        assertThat("Account must have non 0 balance", account.getBalance().getValue(), isNot0());
    }

    private void assertLoanAttributes(Loan loan) {
        assertThat("Loan does not have a name", loan.getName(), notNullValue());
        assertThat("Loan must have balance", loan.getBalance(), notNullValue());
        assertThat("Loan must have non 0 balance", loan.getBalance(), isNot0());
        assertThat("Loan needs the date the terms change", loan.getNextDayOfTermsChange(), notNullValue());
        assertThat("Loan needs to contain the monthly amortization",
                loan.getMonthlyAmortization(), notNullValue());
        assertThat("Some details should be available", loan.getLoanDetails(), notNullValue());
    }

    private void assertAccountAndLoanMatch(LoanAccount account, Loan loan) {
        assertEquals("Account and loan must match on balance", account.getBalance().getValue(), loan.getBalance(), 0.000001);
        assertEquals("Account and loan must match on name", account.getName(), loan.getName());
    }

    @Override
    protected HandelsbankenFITestConfig getTestConfig() {
        return HandelsbankenFITestConfig.USER_1;
    }
}
