package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.mapper.AccountEntityMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListLoansRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListLoansResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.LoanDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DanskeBankAccountLoanFetcherTest {

    private static final String BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/danskebank/resources";

    private static final String ACCOUNT_ENTITIES_FILE = "accountEntities.json";
    private static final String MORTGAGES_FILE = "mortgageEntities.json";
    private static final String MORTGAGE_DETAILS_FILE = "mortgageDetails.json";

    private static final ListAccountsResponse ACCOUNTS_RESPONSE =
            SerializationUtils.deserializeFromString(
                    Paths.get(BASE_PATH, ACCOUNT_ENTITIES_FILE).toFile(),
                    ListAccountsResponse.class);

    private static final ListLoansResponse MORTGAGES_RESPONSE =
            SerializationUtils.deserializeFromString(
                    Paths.get(BASE_PATH, MORTGAGES_FILE).toFile(), ListLoansResponse.class);
    private static final LoanDetailsResponse MORTGAGE_DETAILS_RESPONSE =
            SerializationUtils.deserializeFromString(
                    Paths.get(BASE_PATH, MORTGAGE_DETAILS_FILE).toFile(),
                    LoanDetailsResponse.class);
    DanskeBankAccountLoanFetcher accountLoanFetcher;

    private DanskeBankApiClient mockApiClient;
    private DanskeBankConfiguration mockConfiguration;
    private AccountEntityMapper mockAccountEntityMapper;

    @Before
    public void setup() {
        mockApiClient = mock(DanskeBankApiClient.class);
        mockConfiguration = mock(DanskeBankConfiguration.class);
        mockAccountEntityMapper = mock(AccountEntityMapper.class);

        given(mockConfiguration.getLanguageCode()).willReturn("ZZ");
        given(mockConfiguration.getLoanAccountTypes()).willReturn(Collections.emptyMap());
        given(mockAccountEntityMapper.toLoanAccount(any(), any()))
                .willReturn(mock(LoanAccount.class));
    }

    @Test
    public void shouldFetchBothKindOfLoansIfSetUpToDoSo() {
        // given
        accountLoanFetcher =
                new DanskeBankAccountLoanFetcher(
                        mockApiClient, mockConfiguration, mockAccountEntityMapper, true);
        given(mockApiClient.listAccounts(any(ListAccountsRequest.class)))
                .willReturn(ACCOUNTS_RESPONSE);
        given(mockApiClient.listLoans(any(ListLoansRequest.class))).willReturn(MORTGAGES_RESPONSE);
        given(mockApiClient.loanDetails(any(LoanDetailsRequest.class)))
                .willReturn(MORTGAGE_DETAILS_RESPONSE);

        // when
        Collection<LoanAccount> loanAccounts = accountLoanFetcher.fetchAccounts();

        // then
        assertThat(loanAccounts).hasSize(3);
        verify(mockApiClient).listAccounts(any());
        verify(mockAccountEntityMapper).toLoanAccount(any(), any());
        verify(mockApiClient).listLoans(any());
        verify(mockApiClient, times(2)).loanDetails(any());
        verifyNoMoreInteractions(mockApiClient, mockAccountEntityMapper);
    }

    @Test
    public void shouldFetchOnlyAccountLoansIfMortgagesSkipped() {
        // given
        accountLoanFetcher =
                new DanskeBankAccountLoanFetcher(
                        mockApiClient, mockConfiguration, mockAccountEntityMapper, false);

        given(mockApiClient.listAccounts(any(ListAccountsRequest.class)))
                .willReturn(ACCOUNTS_RESPONSE);

        // when
        Collection<LoanAccount> loanAccounts = accountLoanFetcher.fetchAccounts();

        // then
        assertThat(loanAccounts).hasSize(1);
        verify(mockApiClient).listAccounts(any());
        verify(mockAccountEntityMapper).toLoanAccount(any(), any());
        verifyNoMoreInteractions(mockApiClient, mockAccountEntityMapper);
    }
}
