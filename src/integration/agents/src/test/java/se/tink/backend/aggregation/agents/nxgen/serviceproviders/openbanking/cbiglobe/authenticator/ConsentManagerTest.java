package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiUrlProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.ConsentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.MessageCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.CredentialsDetailRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.CredentialsDetailResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.PsuCredentialsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.PsuCredentialsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.TppErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.UpdateConsentPsuCredentialsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ActualLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class ConsentManagerTest {

    private final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/cbiglobe/resources";

    private static final String STATE = "state";
    private static final String CONSENT_ID = "123";

    private static final String ASPSP_PRODUCT_CODE = "aspspProductCode";
    private static final String USER_KEY = "USER_KEY";
    private static final String PASSWORD_KEY = "PASSWORD_KEY";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private ConsentManager consentManager;
    private CbiUrlProvider urlProvider;
    private CbiGlobeApiClient apiClient;
    private CbiUserState userState;

    @Before
    public void init() {
        apiClient = mock(CbiGlobeApiClient.class);
        userState = mock(CbiUserState.class);
        urlProvider = new CbiUrlProvider("https://example.com");
        consentManager =
                new ConsentManager(
                        apiClient,
                        userState,
                        new ActualLocalDateTimeSource(),
                        100L,
                        3,
                        urlProvider);
        when(userState.getUsername()).thenReturn(USERNAME);
        when(userState.getPassword()).thenReturn(PASSWORD);
    }

    @Test
    public void accountConsentValidPeriodShouldBe89Days() {
        // when
        ConsentRequest consentRequest = consentManager.createConsentRequestAccount();

        // then
        LocalDate localDate = LocalDate.parse(consentRequest.getValidUntil());
        assertThat(localDate).isEqualTo(LocalDate.now().plusDays(89));
    }

    @Test
    public void createAccountConsentShouldCallApiClientAndStartManualAuthentication() {
        // given
        ConsentResponse consentResponse = new ConsentResponse(null, CONSENT_ID, null);
        when(apiClient.createConsent(eq(STATE), eq(ConsentType.ACCOUNT), any()))
                .thenReturn(consentResponse);

        // when
        consentManager.createAccountConsent(STATE);

        // then
        verify(apiClient).createConsent(eq(STATE), eq(ConsentType.ACCOUNT), any());
        verify(userState).startManualAuthenticationStep(CONSENT_ID);
    }

    @Test
    public void transactionsConsentValidPeriodShouldBe89Days() {
        // given
        AccountsResponse accountsResponse =
                new AccountsResponse(Collections.singletonList(new AccountEntity("123")));

        // when
        ConsentRequest consentRequest =
                consentManager.createConsentRequestBalancesTransactions(accountsResponse);

        // then
        LocalDate localDate = LocalDate.parse(consentRequest.getValidUntil());
        assertThat(localDate).isEqualTo(LocalDate.now().plusDays(89));
    }

    @Test
    public void createTransactionsConsentShouldCallApiClientAndStartManualAuthentication() {
        // given
        ConsentResponse consentResponse = new ConsentResponse(null, CONSENT_ID, null);
        when(userState.getAccountsResponseFromStorage())
                .thenReturn(
                        new AccountsResponse(Collections.singletonList(new AccountEntity("123"))));
        when(apiClient.createConsent(eq(STATE), eq(ConsentType.BALANCE_TRANSACTION), any()))
                .thenReturn(consentResponse);

        // when
        consentManager.createTransactionsConsent(STATE);

        // then
        verify(apiClient).createConsent(eq(STATE), eq(ConsentType.BALANCE_TRANSACTION), any());
        verify(userState).startManualAuthenticationStep(CONSENT_ID);
    }

    @Test
    public void isConsentAcceptedShouldNotThrowExceptionIfAccepted() throws SessionException {
        // given
        when(apiClient.getConsentStatus(StorageKeys.CONSENT_ID)).thenReturn(ConsentStatus.VALID);

        // when
        boolean isAccepted = consentManager.verifyIfConsentIsAccepted();

        // then
        assertThat(isAccepted).isTrue();
    }

    @Test
    public void isConsentAcceptedShouldThrowExceptionIfNotAccepted() throws SessionException {
        // given
        when(apiClient.getConsentStatus(StorageKeys.CONSENT_ID)).thenReturn(ConsentStatus.REJECTED);

        // when
        Throwable thrown = catchThrowable(() -> consentManager.verifyIfConsentIsAccepted());

        // then
        verify(userState).resetAuthenticationState();

        Assertions.assertThat(thrown).isInstanceOf(SessionException.class);
    }

    @Test
    public void updateAuthenticationMethodShouldThrowExceptionIfGetConsentStatusThrowException()
            throws SessionException {
        // given
        when(apiClient.getConsentStatus(StorageKeys.CONSENT_ID)).thenThrow(SessionException.class);
        when(userState.getChosenAuthenticationMethodId()).thenReturn("1");
        when(userState.getConsentId()).thenReturn(CONSENT_ID);

        // when
        consentManager.updateAuthenticationMethod();

        // then
        verify(apiClient).updateConsent(any(), any());
    }

    @Test
    public void updatePsuCredentialsShouldInvokeApiClientWithProperValues() {
        // given
        UpdateConsentPsuCredentialsRequest updateConsentPsuCredentialsRequest =
                createUpdateConsentPsuCredentialsRequest();

        PsuCredentialsResponse psuCredentialsResponse = prepareConsentResponse();

        when(userState.getConsentId()).thenReturn(CONSENT_ID);

        // when
        consentManager.updatePsuCredentials(
                psuCredentialsResponse,
                urlProvider.getUpdateConsentsUrl().concat("/" + CONSENT_ID),
                ConsentResponse.class);

        // then
        verify(apiClient)
                .updatePsuCredentials(
                        eq(urlProvider.getUpdateConsentsUrl().concat("/" + CONSENT_ID)),
                        eq(updateConsentPsuCredentialsRequest),
                        eq(ConsentResponse.class));
    }

    private PsuCredentialsResponse prepareConsentResponse() {
        List<CredentialsDetailResponse> credentialsDetails =
                Arrays.asList(
                        new CredentialsDetailResponse(USER_KEY, false),
                        new CredentialsDetailResponse(PASSWORD_KEY, true));

        return new PsuCredentialsResponse(ASPSP_PRODUCT_CODE, credentialsDetails);
    }

    @Test
    public void updatePsuCredentialsShouldThrowExceptionIfNoSecretAmongCredentials() {
        // given
        List<CredentialsDetailResponse> credentialsDetails =
                Arrays.asList(
                        new CredentialsDetailResponse(USER_KEY, false),
                        new CredentialsDetailResponse(PASSWORD_KEY, false));

        PsuCredentialsResponse psuCredentialsResponse =
                new PsuCredentialsResponse(ASPSP_PRODUCT_CODE, credentialsDetails);

        when(userState.getConsentId()).thenReturn(CONSENT_ID);

        // when
        Throwable thrown =
                catchThrowable(
                        () ->
                                consentManager.updatePsuCredentials(
                                        psuCredentialsResponse, null, ConsentResponse.class));

        // then
        verifyNoMoreInteractions(apiClient);

        Assertions.assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void updatePsuCredentialsShouldThrowExceptionIfMoreThanOneSecretAmongCredentials() {
        // given
        List<CredentialsDetailResponse> credentialsDetails =
                Arrays.asList(
                        new CredentialsDetailResponse(USER_KEY, true),
                        new CredentialsDetailResponse(PASSWORD_KEY, true));

        PsuCredentialsResponse psuCredentialsResponse =
                new PsuCredentialsResponse(ASPSP_PRODUCT_CODE, credentialsDetails);

        when(userState.getConsentId()).thenReturn(CONSENT_ID);

        // when
        Throwable thrown =
                catchThrowable(
                        () ->
                                consentManager.updatePsuCredentials(
                                        psuCredentialsResponse, null, ConsentResponse.class));

        // then
        verifyNoMoreInteractions(apiClient);

        Assertions.assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void updatePsuCredentialsShouldThrowExceptionIfPsuCredentialsNull() {
        // given
        when(userState.getConsentId()).thenReturn(CONSENT_ID);

        // when
        Throwable thrown =
                catchThrowable(
                        () ->
                                consentManager.updatePsuCredentials(
                                        new PsuCredentialsResponse(), null, ConsentResponse.class));

        // then
        verifyNoMoreInteractions(apiClient);

        Assertions.assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
    }

    private UpdateConsentPsuCredentialsRequest createUpdateConsentPsuCredentialsRequest() {
        return new UpdateConsentPsuCredentialsRequest(
                new PsuCredentialsRequest(
                        ASPSP_PRODUCT_CODE,
                        Arrays.asList(
                                new CredentialsDetailRequest(USER_KEY, USERNAME),
                                new CredentialsDetailRequest(PASSWORD_KEY, PASSWORD))));
    }

    @Test
    public void waitForAcceptanceShouldRetryIfConsentPending() throws AuthenticationException {
        // given
        when(apiClient.getConsentStatus(StorageKeys.CONSENT_ID))
                .thenReturn(ConsentStatus.RECEIVED)
                .thenReturn(ConsentStatus.VALID);

        // when
        consentManager.waitForAcceptance();

        // then
        verify(apiClient, times(2)).getConsentStatus(StorageKeys.CONSENT_ID);
    }

    @Test
    public void waitForAcceptanceShouldThrowExceptionIfConsentRejected()
            throws AuthenticationException {
        // given
        when(apiClient.getConsentStatus(StorageKeys.CONSENT_ID)).thenReturn(ConsentStatus.REJECTED);

        // when
        Throwable thrown = catchThrowable(consentManager::waitForAcceptance);

        // then
        verify(apiClient).getConsentStatus(StorageKeys.CONSENT_ID);
        Assertions.assertThat(thrown).isInstanceOf(LoginException.class);
    }

    @Test
    public void waitForAcceptanceShouldThrowExceptionIfRetryLimitReached()
            throws AuthenticationException {
        // given
        when(apiClient.getConsentStatus(StorageKeys.CONSENT_ID))
                .thenReturn(ConsentStatus.RECEIVED)
                .thenReturn(ConsentStatus.RECEIVED)
                .thenReturn(ConsentStatus.RECEIVED);

        // when
        Throwable thrown = catchThrowable(consentManager::waitForAcceptance);

        // then
        verify(apiClient, times(3)).getConsentStatus(StorageKeys.CONSENT_ID);
        Assertions.assertThat(thrown).isInstanceOf(LoginException.class);
    }

    @Test
    @Parameters(method = "generateHttpResponseExceptions")
    public void isConsentAcceptedShouldThrowSessionExceptionIfIdentifiedHttpException(
            HttpResponseException ex) {
        // given
        when(ex.getResponse().getBody(String.class)).thenReturn(ex.getMessage());
        when(apiClient.getConsentStatus(StorageKeys.CONSENT_ID)).thenThrow(ex);
        // when
        Throwable throwable = catchThrowable(() -> consentManager.verifyIfConsentIsAccepted());
        // then
        assertThat(throwable).isInstanceOf(SessionException.class);
    }

    private Object[] generateHttpResponseExceptions() {
        HttpRequest request = mock(HttpRequest.class);
        HttpResponse response = mock(HttpResponse.class);
        return new Object[] {
            new HttpResponseException(MessageCodes.CONSENT_INVALID.name(), request, response),
            new HttpResponseException(MessageCodes.CONSENT_EXPIRED.name(), request, response),
            new HttpResponseException(MessageCodes.RESOURCE_UNKNOWN.name(), request, response)
        };
    }

    @Test
    public void isConsentAcceptedShoulRetryCallForConsentIfConsentStatusReceived() {
        // given
        when(apiClient.getConsentStatus(StorageKeys.CONSENT_ID)).thenReturn(ConsentStatus.RECEIVED);
        // when
        Throwable throwable = catchThrowable(() -> consentManager.verifyIfConsentIsAccepted());
        // then
        verify(apiClient, times(3)).getConsentStatus(any());
    }

    @Test
    public void shouldThrowInvalidCredentials() {
        // given
        HttpResponseException exception = prepareConsentHttpException();

        UpdateConsentPsuCredentialsRequest updateConsentPsuCredentialsRequest =
                createUpdateConsentPsuCredentialsRequest();

        PsuCredentialsResponse psuCredentialsResponse = prepareConsentResponse();

        when(userState.getConsentId()).thenReturn(CONSENT_ID);
        when(apiClient.updatePsuCredentials(
                        urlProvider.getUpdateConsentsUrl().concat("/" + CONSENT_ID),
                        updateConsentPsuCredentialsRequest,
                        ConsentResponse.class))
                .thenThrow(exception);

        // when
        Throwable throwable =
                catchThrowable(
                        () ->
                                consentManager.updatePsuCredentials(
                                        psuCredentialsResponse,
                                        urlProvider.getUpdateConsentsUrl().concat("/" + CONSENT_ID),
                                        ConsentResponse.class));
        // then
        assertThat(throwable)
                .isInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CREDENTIALS");
    }

    @Test
    public void shouldRetryConsentStatusCallWhenStillInReceivedState() {
        // given
        when(apiClient.getConsentStatus(StorageKeys.CONSENT_ID))
                .thenReturn(ConsentStatus.RECEIVED)
                .thenReturn(ConsentStatus.EXPIRED);
        // when
        consentManager.retryCallForConsentStatus();
        // then

        verify(apiClient, times(2)).getConsentStatus(StorageKeys.CONSENT_ID);
    }

    @Test
    @Parameters(method = "finalConsentStates")
    public void shouldNotRetryConsentStatusCallWithAnyOtherConsentStatus(
            ConsentStatus consentStatus) {
        // given
        when(apiClient.getConsentStatus(StorageKeys.CONSENT_ID)).thenReturn(consentStatus);

        // when
        consentManager.retryCallForConsentStatus();

        // then
        verify(apiClient).getConsentStatus(StorageKeys.CONSENT_ID);
    }

    private Object[] finalConsentStates() {
        return new Object[] {
            ConsentStatus.REJECTED,
            ConsentStatus.VALID,
            ConsentStatus.REVOKEDBYPSU,
            ConsentStatus.EXPIRED,
            ConsentStatus.TERMINATEDBYTPP,
            ConsentStatus.REPLACED,
            ConsentStatus.INVALIDATED,
            ConsentStatus.PENDINGEXPIRED
        };
    }

    private HttpResponseException prepareConsentHttpException() {
        HttpRequest httpRequest = mock(HttpRequest.class);
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpResponse.getBody(TppErrorResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "tpp_invalid_credentials.json").toFile(),
                                TppErrorResponse.class));
        return new HttpResponseException(httpRequest, httpResponse);
    }
}
