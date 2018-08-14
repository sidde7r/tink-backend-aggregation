package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.rpc.DetailedLoanResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.rpc.LoanOverviewResponse;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.LoanDetails;
import se.tink.libraries.serialization.utils.SerializationUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;

public class SwedbankSELoanFetcherTest {

    private SwedbankSEApiClient apiClient;
    private SwedbankSELoanFetcher fetcher;

    @Before
    public void before() {
        apiClient = Mockito.mock(SwedbankSEApiClient.class);
        fetcher = new SwedbankSELoanFetcher(apiClient);
    }

    @Test
    public void fetchAccounts_parseMembershipLoan() {
        Mockito.when(apiClient.loanOverview())
                .thenReturn(SerializationUtils.deserializeFromString(SwedBankSELoansFetcherTestData.OVERVIEW_OF_LOANS,
                        LoanOverviewResponse.class));
        Mockito.when(apiClient
                .loadDetailsEntity(argThat(a -> Optional.ofNullable(a.getUri()).orElse("").contains("MEMBERSHIPLOAN"))))
                .thenReturn(SerializationUtils
                        .deserializeFromString(SwedBankSELoansFetcherTestData.MAMBERSHIP_CONSUMTION_LOAN_DETAILS,
                                DetailedLoanResponse.class));

        List<LoanAccount> loanAccounts = fetcher.fetchAccounts().stream()
                .filter(l -> LoanDetails.Type.MEMBERSHIP.equals(l.getDetails().getType())).collect(Collectors.toList());
        assertEquals(1, loanAccounts.size());
        LoanAccount loanAccount = loanAccounts.get(0);

        assertEquals(Double.valueOf(-88888.0d), loanAccount.getBalance().getValue());
        assertEquals("SEK", loanAccount.getBalance().getCurrency());

        assertEquals("111 111 111-1", loanAccount.getAccountNumber());

        assertEquals(2, loanAccount.getDetails().getApplicants().size());
    }

    @Test
    public void fetchAccounts_parseMortgage() {
        Mockito.when(apiClient.loanOverview())
                .thenReturn(SerializationUtils.deserializeFromString(SwedBankSELoansFetcherTestData.OVERVIEW_OF_LOANS,
                        LoanOverviewResponse.class));
        Mockito.when(apiClient
                .loadDetailsEntity(argThat(a -> Optional.ofNullable(a.getUri()).orElse("").contains("MORTGAGE"))))
                .thenReturn(SerializationUtils
                        .deserializeFromString(SwedBankSELoansFetcherTestData.MOTGAGE_DETAILS,
                                DetailedLoanResponse.class));

        List<LoanAccount> loanAccounts = fetcher.fetchAccounts().stream()
                .filter(l -> LoanDetails.Type.MORTGAGE.equals(l.getDetails().getType())).collect(Collectors.toList());
        assertEquals(5, loanAccounts.size());
        LoanAccount loanAccount = loanAccounts.stream().filter(l -> !l.getDetails().getApplicants().isEmpty())
                .findFirst().get();

        assertEquals(Double.valueOf(-333000.0d), loanAccount.getBalance().getValue());
        assertEquals("SEK", loanAccount.getBalance().getCurrency());

        assertEquals("555 555 555-2", loanAccount.getAccountNumber());
        assertTrue(loanAccount.getDetails().getSecurity().contains("QUITE"));

        assertEquals(1, loanAccount.getDetails().getApplicants().size());
        assertFalse(loanAccount.getDetails().hasCoApplicant());
    }
}
