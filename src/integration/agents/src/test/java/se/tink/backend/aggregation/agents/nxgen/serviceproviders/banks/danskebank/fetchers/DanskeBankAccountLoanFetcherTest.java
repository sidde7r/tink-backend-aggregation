package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.mapper.AccountEntityMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountInterestDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.InterestDetailEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListLoansRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.ListLoansResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.LoanDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DanskeBankAccountLoanFetcherTest {

    private static final String BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/danskebank/resources";

    private static final String ACCOUNT_ENTITIES_FILE = "accountEntities.json";
    private static final String MORTGAGES_FILE = "mortgageEntities.json";
    private static final String MORTGAGE_DETAILS_FILE = "mortgageDetails.json";
    private static final String ACCOUNT_DETAILS_IN_EXCEPTION_BODY_FILE =
            "accountDetailsResponseInExceptionBody.json";

    private static final String ACCOUNT_NO_INT = "12345";

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
    private static final AccountDetailsResponse ACCOUNT_DETAILS_RESPONSE_IN_EXCEPTION_BODY =
            SerializationUtils.deserializeFromString(
                    Paths.get(BASE_PATH, ACCOUNT_DETAILS_IN_EXCEPTION_BODY_FILE).toFile(),
                    AccountDetailsResponse.class);

    private DanskeBankAccountLoanFetcher accountLoanFetcher;

    private DanskeBankApiClient mockApiClient;
    private DanskeBankConfiguration mockConfiguration;
    private AccountEntityMapper mockAccountEntityMapper;
    private HttpResponse httpResponse;

    @Before
    public void setup() {
        mockApiClient = mock(DanskeBankApiClient.class);
        mockConfiguration = mock(DanskeBankConfiguration.class);
        mockAccountEntityMapper = mock(AccountEntityMapper.class);

        given(mockConfiguration.getLanguageCode()).willReturn("ZZ");
        given(mockConfiguration.getMarketCode()).willReturn("dk");
        given(mockConfiguration.getLoanAccountTypes()).willReturn(Collections.emptyMap());
        given(mockAccountEntityMapper.toLoanAccount(any(), any(), any()))
                .willReturn(mock(LoanAccount.class));

        accountLoanFetcher =
                new DanskeBankAccountLoanFetcher(
                        mockApiClient, mockConfiguration, mockAccountEntityMapper, false);

        httpResponse = mock(HttpResponse.class);
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
        verify(mockApiClient).fetchAccountDetails(any());
        verify(mockAccountEntityMapper).toLoanAccount(any(), any(), any());
        verify(mockApiClient).listLoans(any());
        verify(mockApiClient, times(2)).loanDetails(any());
        verifyNoMoreInteractions(mockApiClient, mockAccountEntityMapper);
    }

    @Test
    public void shouldFetchOnlyAccountLoansIfMortgagesSkipped() {
        // given
        given(mockApiClient.listAccounts(any(ListAccountsRequest.class)))
                .willReturn(ACCOUNTS_RESPONSE);

        // when
        Collection<LoanAccount> loanAccounts = accountLoanFetcher.fetchAccounts();

        // then
        assertThat(loanAccounts).hasSize(1);
        verify(mockApiClient).listAccounts(any());
        verify(mockApiClient).fetchAccountDetails(any());
        verify(mockAccountEntityMapper).toLoanAccount(any(), any(), any());
        verifyNoMoreInteractions(mockApiClient, mockAccountEntityMapper);
    }

    @Test
    public void shouldFetchLoanAccountDetailsWhenItIsInExceptionBody() {
        // given
        given(httpResponse.getStatus()).willReturn(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        given(httpResponse.hasBody()).willReturn(true);
        given(httpResponse.getBody(any())).willReturn(ACCOUNT_DETAILS_RESPONSE_IN_EXCEPTION_BODY);

        // and
        given(mockApiClient.fetchAccountDetails(any(AccountDetailsRequest.class)))
                .willThrow(new HttpResponseException("", null, httpResponse));

        // and
        AccountDetailsResponse expected = new AccountDetailsResponse();
        expected.setAccountInterestDetails(getAccountInterestDetailsEntity());

        // when
        AccountDetailsResponse result = accountLoanFetcher.fetchLoanAccountDetails(ACCOUNT_NO_INT);

        // then
        assertThat(result.getInterestRate()).isEqualTo(expected.getInterestRate());
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertThat(result.getResponseCode()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertThat(result.getAccountOwners()).isNull();
        assertThat(result.getAccountType()).isNull();
        assertThat(result.getIban()).isNull();
    }

    private AccountInterestDetailsEntity getAccountInterestDetailsEntity() {
        InterestDetailEntity interestOnCreditBalance = new InterestDetailEntity();
        interestOnCreditBalance.setRateInPercent("0.000");
        interestOnCreditBalance.setText("Interest on credit balance:");

        InterestDetailEntity interestOnDebitBalance = new InterestDetailEntity();
        interestOnDebitBalance.setRateInPercent("10.100");
        interestOnDebitBalance.setText("Interest on debit balance:");

        AccountInterestDetailsEntity accountInterestDetailsEntity =
                new AccountInterestDetailsEntity();
        accountInterestDetailsEntity.setInterestDetails(
                Arrays.asList(interestOnCreditBalance, interestOnDebitBalance));
        return accountInterestDetailsEntity;
    }

    @Test
    public void shouldReturnNewAccountDetailsWhenExceptionOccursDuringGettingResponseBody() {
        // given
        given(httpResponse.getStatus()).willReturn(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        given(httpResponse.hasBody()).willReturn(true);
        given(httpResponse.getBody(any())).willThrow(new HttpClientException(null));

        // and
        given(mockApiClient.fetchAccountDetails(any(AccountDetailsRequest.class)))
                .willThrow(new HttpResponseException("", null, httpResponse));

        // when
        AccountDetailsResponse result = accountLoanFetcher.fetchLoanAccountDetails(ACCOUNT_NO_INT);

        // then
        assertThat(result).isEqualTo(new AccountDetailsResponse());
    }
}
