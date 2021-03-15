package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.TestDataReader;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc.GetLoansResponse;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class BankdataLoanFetcherTest {

    private BankdataApiClient bankdataApiClient;
    private BankdataLoanFetcher bankdataLoanFetcher;

    @Before
    public void setUp() {
        bankdataApiClient = mock(BankdataApiClient.class);
        bankdataLoanFetcher = new BankdataLoanFetcher(bankdataApiClient);
    }

    @Test
    public void fetchAccountsDataAndCheckIfExactOneIsALoan() {
        // given
        GetLoansResponse loansResponse =
                TestDataReader.readFromFile(TestDataReader.ACCOUNTS_RESP, GetLoansResponse.class);
        when(bankdataApiClient.getLoans()).thenReturn(loansResponse);

        // when
        Collection<LoanAccount> loanAccounts = bankdataLoanFetcher.fetchAccounts();

        // then
        assertThat(loanAccounts.size()).isEqualTo(1);
        LoanAccount loanAccount = loanAccounts.stream().findFirst().get();
        assertThat(loanAccount.getExactBalance()).isEqualTo(ExactCurrencyAmount.inDKK(-5));
        assertThat(loanAccount.getDetails().getInitialBalance())
                .isEqualTo(ExactCurrencyAmount.inDKK(10));
        assertThat(loanAccount.getDetails().getType()).isEqualTo(Type.DERIVE_FROM_NAME);
        assertThat(loanAccount.getHolderName().toString()).isEqualTo("Olsen Brothers");
    }
}
