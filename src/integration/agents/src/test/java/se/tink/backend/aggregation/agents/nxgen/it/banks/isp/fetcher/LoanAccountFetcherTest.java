package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.apiclient.IspApiClient;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.libraries.account.enums.AccountIdentifierType;

public class LoanAccountFetcherTest {

    private LoanAccountFetcher fetcher;
    private IspApiClient apiClient;

    @Before
    public void setup() {
        this.apiClient = mock(IspApiClient.class);
        this.fetcher = new LoanAccountFetcher(apiClient);
    }

    @Test
    public void shouldFetchLoanAccount() {
        // given
        when(apiClient.fetchAccountsAndIdentities())
                .thenReturn(FetchersTestData.loanAccountResponse());
        // when
        List<LoanAccount> loanAccounts = new ArrayList<>(fetcher.fetchAccounts());
        // then
        assertThat(loanAccounts).hasSize(1);
        LoanAccount loanAccount = loanAccounts.get(0);
        assertThat(loanAccount.getDetails().getType()).isEqualTo(LoanDetails.Type.STUDENT);
        assertThat(loanAccount.getApiIdentifier()).isEqualTo("123456789");
        assertThat(loanAccount.getIdentifiers()).hasSize(1);
        assertThat(loanAccount.getIdentifiers().get(0).getType())
                .isEqualTo(AccountIdentifierType.IBAN);
        assertThat(loanAccount.getIdentifiers().get(0).getIdentifier())
                .isEqualTo("IT14X0300203280334787988525");
        assertThat(loanAccount.getExactBalance().getExactValue()).isEqualByComparingTo("-7500");
        assertThat(loanAccount.getName()).isEqualTo("Conto per Merito");
    }
}
