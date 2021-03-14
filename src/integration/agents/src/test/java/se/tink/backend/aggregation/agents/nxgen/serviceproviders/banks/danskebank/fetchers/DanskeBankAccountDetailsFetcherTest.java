package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.nio.file.Paths;
import java.util.Arrays;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountInterestDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.InterestDetailEntity;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DanskeBankAccountDetailsFetcherTest {

    private static final String BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/danskebank/resources";

    private static final String ACCOUNT_DETAILS_IN_EXCEPTION_BODY_FILE =
            "accountDetailsResponseInExceptionBody.json";

    private static final String ACCOUNT_NO_INT = "12345";

    private static final AccountDetailsResponse ACCOUNT_DETAILS_RESPONSE_IN_EXCEPTION_BODY =
            SerializationUtils.deserializeFromString(
                    Paths.get(BASE_PATH, ACCOUNT_DETAILS_IN_EXCEPTION_BODY_FILE).toFile(),
                    AccountDetailsResponse.class);

    private DanskeBankAccountDetailsFetcher accountDetailsFetcher;
    private DanskeBankApiClient mockApiClient;
    private HttpResponse httpResponse;

    @Before
    public void setup() {
        mockApiClient = mock(DanskeBankApiClient.class);
        accountDetailsFetcher = new DanskeBankAccountDetailsFetcher(mockApiClient);
        httpResponse = mock(HttpResponse.class);
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
        AccountDetailsResponse result = accountDetailsFetcher.fetchAccountDetails(ACCOUNT_NO_INT);

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
        AccountDetailsResponse result = accountDetailsFetcher.fetchAccountDetails(ACCOUNT_NO_INT);

        // then
        assertThat(result).isEqualTo(new AccountDetailsResponse());
    }
}
