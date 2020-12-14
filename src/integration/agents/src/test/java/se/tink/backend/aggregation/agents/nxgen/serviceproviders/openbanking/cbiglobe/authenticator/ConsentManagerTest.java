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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.ConsentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.MessageCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.CredentialsDetailRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.CredentialsDetailResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.PsuCredentialsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.PsuCredentialsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.UpdateConsentPsuCredentialsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RunWith(JUnitParamsRunner.class)
public class ConsentManagerTest {

    private static final String STATE = "state";
    private static final String CONSENT_ID = "123";

    private static final String ASPSP_PRODUCT_CODE = "aspspProductCode";
    private static final String USER_KEY = "USER_KEY";
    private static final String PASSWORD_KEY = "PASSWORD_KEY";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private ConsentManager consentManager;
    private CbiGlobeApiClient apiClient;
    private CbiUserState userState;

    @Before
    public void init() {
        apiClient = mock(CbiGlobeApiClient.class);
        userState = mock(CbiUserState.class);
        consentManager = new ConsentManager(apiClient, userState, 100L, 3);
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
        GetAccountsResponse getAccountsResponse =
                new GetAccountsResponse(Collections.singletonList(new AccountEntity("123")));

        // when
        ConsentRequest consentRequest =
                consentManager.createConsentRequestBalancesTransactions(getAccountsResponse);

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
                        new GetAccountsResponse(
                                Collections.singletonList(new AccountEntity("123"))));
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
        assertThat(isAccepted).isEqualTo(true);
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
        verify(apiClient).updateConsent(eq(CONSENT_ID), any());
    }

    @Test
    public void updatePsuCredentialsShouldInvokeApiClientWithProperValues() {
        // given
        UpdateConsentPsuCredentialsRequest updateConsentPsuCredentialsRequest =
                createUpdateConsentPsuCredentialsRequest();

        List<CredentialsDetailResponse> credentialsDetails =
                Arrays.asList(
                        new CredentialsDetailResponse(USER_KEY, false),
                        new CredentialsDetailResponse(PASSWORD_KEY, true));

        PsuCredentialsResponse psuCredentialsResponse =
                new PsuCredentialsResponse(ASPSP_PRODUCT_CODE, credentialsDetails);

        when(userState.getConsentId()).thenReturn(CONSENT_ID);

        // when
        consentManager.updatePsuCredentials(USERNAME, PASSWORD, psuCredentialsResponse);

        // then
        verify(apiClient)
                .updateConsentPsuCredentials(
                        eq(CONSENT_ID), eq(updateConsentPsuCredentialsRequest));
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
                                        USERNAME, PASSWORD, psuCredentialsResponse));

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
                                        USERNAME, PASSWORD, psuCredentialsResponse));

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
                catchThrowable(() -> consentManager.updatePsuCredentials(USERNAME, PASSWORD, null));

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
}
