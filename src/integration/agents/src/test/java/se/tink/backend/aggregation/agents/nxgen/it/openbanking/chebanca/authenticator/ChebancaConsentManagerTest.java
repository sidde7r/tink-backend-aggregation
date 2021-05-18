package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.rpc.ConsentAuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.exception.UnsuccessfulApiCallException;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.CustomerIdResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.date.DateFormat;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class ChebancaConsentManagerTest {

    private static final int ERROR_RESPONSE_CODE = 500;
    private static final int SUCCESSFUL_RESPONSE_CODE = 200;
    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/it/openbanking/chebanca/resources";

    private ChebancaApiClient apiClient;
    private ChebancaConsentManager consentManager;
    private HttpResponse erroneousResponse = getErroneousResponse();
    private SupplementalInformationHelper supplementalInformationHelper;
    private StrongAuthenticationState strongAuthenticationState;
    private Credentials credentials;

    @Before
    public void initApiClientMockForPositiveScenario() {
        apiClient = mock(ChebancaApiClient.class);
        HttpResponse createConsentResponse = getMockedCreateConsentResponse();
        when(apiClient.createConsent(any())).thenReturn(createConsentResponse);
        HttpResponse authorizeConsentResponse = getMockedAuthorizeConsentResponse();
        when(apiClient.authorizeConsent(any())).thenReturn(authorizeConsentResponse);
        HttpResponse confirmConsentResponse = getMockedConfirmConsentResponse();
        when(apiClient.confirmConsent(any())).thenReturn(confirmConsentResponse);
        HttpResponse getAccountsResponse = getAccountsResponse();
        when(apiClient.getAccounts()).thenReturn(getAccountsResponse);
        HttpResponse customerIdResponse = getMockedCustomerIdResponse();
        when(apiClient.getCustomerId()).thenReturn(customerIdResponse);
        supplementalInformationHelper = mock(SupplementalInformationHelper.class);
        strongAuthenticationState = mock(StrongAuthenticationState.class);
        credentials = new Credentials();
        consentManager =
                new ChebancaConsentManager(
                        apiClient,
                        supplementalInformationHelper,
                        strongAuthenticationState,
                        credentials);
    }

    @Test
    public void shouldThrowIfFetchingCustomerIdFails() {
        // given
        when(apiClient.getCustomerId()).thenReturn(erroneousResponse);

        // when

        Throwable thrown = catchThrowable(consentManager::processConsent);

        // then
        assertThat(thrown)
                .isInstanceOf(UnsuccessfulApiCallException.class)
                .hasMessage(
                        "Could not get customer id. Error response code: " + ERROR_RESPONSE_CODE);
    }

    @Test
    public void shouldThrowIfFetchAccountsFailed() {
        // given
        when(apiClient.getAccounts()).thenReturn(erroneousResponse);

        // when
        Throwable thrown = catchThrowable(consentManager::processConsent);

        // then
        assertThat(thrown)
                .isInstanceOf(UnsuccessfulApiCallException.class)
                .hasMessage(
                        "Could not fetch accounts. Error response code: " + ERROR_RESPONSE_CODE);
    }

    @Test
    public void shouldThrowIfAuthorizeConsentFailed() {
        // given
        when(apiClient.authorizeConsent(any())).thenReturn(erroneousResponse);

        // when
        Throwable thrown = catchThrowable(consentManager::processConsent);

        // then
        assertThat(thrown)
                .isInstanceOf(UnsuccessfulApiCallException.class)
                .hasMessage(
                        "Could not authorize consent. Error response code: " + ERROR_RESPONSE_CODE);
    }

    @Test
    public void shouldThrowIfConfirmConsentFailed() {
        // given
        when(apiClient.confirmConsent(any())).thenReturn(erroneousResponse);

        // when
        Throwable thrown = catchThrowable(consentManager::processConsent);

        // then
        assertThat(thrown)
                .isInstanceOf(UnsuccessfulApiCallException.class)
                .hasMessage(
                        "Could not confirm consent. Error response code: " + ERROR_RESPONSE_CODE);
    }

    @Test
    public void processConsentShouldSetExpiryDateToCredentials() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Date expectedSessionExpiryDate =
                DateFormat.convertToDateViaInstant(now.plusDays(89).toLocalDate());

        // when
        consentManager.processConsent();

        // then
        assertThat(credentials.getSessionExpiryDate()).isNotNull();
        assertThat(credentials.getSessionExpiryDate()).isEqualTo(expectedSessionExpiryDate);
    }

    private HttpResponse getErroneousResponse() {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(ERROR_RESPONSE_CODE);
        return response;
    }

    private HttpResponse getMockedCreateConsentResponse() {
        ConsentResponse consentResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "consent_response.json").toFile(),
                        ConsentResponse.class);
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(SUCCESSFUL_RESPONSE_CODE);
        when(response.getBody(ConsentResponse.class)).thenReturn(consentResponse);
        return response;
    }

    private HttpResponse getMockedAuthorizeConsentResponse() {
        ConsentAuthorizationResponse authorizeConsentResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "authorize_consent_response.json").toFile(),
                        ConsentAuthorizationResponse.class);
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(SUCCESSFUL_RESPONSE_CODE);
        when(response.getBody(ConsentAuthorizationResponse.class))
                .thenReturn(authorizeConsentResponse);
        return response;
    }

    private HttpResponse getMockedConfirmConsentResponse() {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(SUCCESSFUL_RESPONSE_CODE);
        return response;
    }

    private HttpResponse getMockedCustomerIdResponse() {
        CustomerIdResponse customerIdResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "customer_id.json").toFile(),
                        CustomerIdResponse.class);
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(SUCCESSFUL_RESPONSE_CODE);
        when(response.getBody(CustomerIdResponse.class)).thenReturn(customerIdResponse);
        return response;
    }

    private HttpResponse getAccountsResponse() {
        GetAccountsResponse accountsResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "accounts_response.json").toFile(),
                        GetAccountsResponse.class);
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatus()).thenReturn(SUCCESSFUL_RESPONSE_CODE);
        when(response.getBody(GetAccountsResponse.class)).thenReturn(accountsResponse);
        return response;
    }
}
