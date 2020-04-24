package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.component.loan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.component.loan.detail.LoanTestData;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.detail.LoanAccountDto;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.NovoBancoLoanAccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;

public class NovoBancoLoanAccountFetcherTest {

    @Test
    public void shouldReturnEmptyCollectionIfNoLoansAvailable() {
        // given
        NovoBancoApiClient apiClient = mock(NovoBancoApiClient.class);
        when(apiClient.getLoanAccounts()).thenReturn(Collections.emptyList());
        NovoBancoLoanAccountFetcher fetcher = new NovoBancoLoanAccountFetcher(apiClient);

        // when
        Collection<LoanAccount> accounts = fetcher.fetchAccounts();

        // then
        assertTrue(accounts.isEmpty());
    }

    @Test
    public void shouldReturnNonEmptyCollectionIfLoansAvailable() {
        // given
        NovoBancoApiClient apiClient = mock(NovoBancoApiClient.class);
        when(apiClient.getLoanAccounts()).thenReturn(LoanTestData.getLoanData());
        NovoBancoLoanAccountFetcher fetcher = new NovoBancoLoanAccountFetcher(apiClient);

        // when
        Collection<LoanAccount> accounts = fetcher.fetchAccounts();

        // then
        assertEquals(2, accounts.size());
    }

    @Test
    public void shouldReturnAccountsMappedCorrectly() {
        // given
        NovoBancoApiClient apiClient = mock(NovoBancoApiClient.class);
        when(apiClient.getLoanAccounts()).thenReturn(LoanTestData.getLoanData());
        NovoBancoLoanAccountFetcher fetcher = new NovoBancoLoanAccountFetcher(apiClient);

        // when
        Collection<LoanAccount> accounts = fetcher.fetchAccounts();

        // then
        for (LoanAccount account : accounts) {
            String loanNumber = account.getDetails().getLoanNumber();
            LoanAccountDto referenceAccount = LoanTestData.getReferenceLoanAccountDto(loanNumber);
            assertNotNull(
                    "Could not find a reference Loan Account matching given loan number",
                    referenceAccount);
            assertAccountEqual(referenceAccount, account);
        }
    }

    private void assertAccountEqual(LoanAccountDto referenceAccount, LoanAccount account) {
        assertEquals(referenceAccount.getAccountNumber(), account.getAccountNumber());
        assertTrue(account.isUniqueIdentifierEqual(referenceAccount.getUniqueIdentifier()));
        assertEquals(referenceAccount.getContractId(), account.getDetails().getLoanNumber());
        assertEquals(
                referenceAccount.getInitialBalance(), account.getDetails().getInitialBalance());
        assertEquals(
                referenceAccount.getInitialDate(),
                dateToString(account.getDetails().getInitialDate()));
        assertEquals(referenceAccount.getExactBalance(), account.getExactBalance());
        assertEquals(referenceAccount.getInterestRate(), account.getInterestRate());
        assertEquals(LoanDetails.Type.MORTGAGE, account.getDetails().getType());
        assertEquals(referenceAccount.getProductName(), account.getIdModule().getProductName());
    }

    private String dateToString(Date date) {
        return new SimpleDateFormat("dd-MM-yyyy").format(date);
    }
}
