package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.CONSENT_RESPONSE_MISSING_LINK;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.CONSENT_RESPONSE_OK;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.CONSENT_STATUS_RESPONSE_NOT_OK;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.CONSENT_STATUS_RESPONSE_OK;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.FINALIZE_AUTH_FAILED;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.FINALIZE_AUTH_OK;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.FINALIZE_AUTH_OTHER;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.HTTP_RESPONSE_EXCEPTION;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.INIT_AUTH_RESPONSE_NO_METHOD;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.INIT_AUTH_RESPONSE_OK_ONE_METHOD;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.INIT_AUTH_RESPONSE_OK_TWO_METHODS;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.LOGIN_EXCEPTION;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.OK_CREDENTIALS;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.OK_PASSWORD;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.OK_USERNAME;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.SELECT_AUTH_METHOD_NO_CHALLENGE_DATA;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.SELECT_AUTH_METHOD_OK;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.SUPPLEMENTAL_INFO_EXCEPTION;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.SUPPLEMENTAL_RESPONSE_OK;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.TEST_AUTHORIZATION_ID;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.TEST_AUTH_URL;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.TEST_CONSENT_ID;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.TEST_OTP;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.TEST_SCA_METHOD_ID;

import java.util.Map;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.AuthenticationMethodResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.FinalizeAuthorizationResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;

@RunWith(JUnitParamsRunner.class)
public class SparkassenAuthenticatorTest {

    private Catalog catalog;
    private SupplementalInformationHelper supplementalInformationHelper;
    private SparkassenApiClient apiClient;
    private SparkassenPersistentStorage persistentStorage;

    private SparkassenAuthenticator authenticator;

    @Before
    public void setup() {
        catalog = mock(Catalog.class);
        supplementalInformationHelper = mock(SupplementalInformationHelper.class);
        apiClient = mock(SparkassenApiClient.class);
        persistentStorage = new SparkassenPersistentStorage(new PersistentStorage());

        when(catalog.getString(anyString())).thenReturn("");
        when(catalog.getString(any(LocalizableKey.class))).thenReturn("");
        when(catalog.getString(any(LocalizableKey.class))).thenReturn("");
        when(catalog.getString(any(), any())).thenReturn("");
        authenticator =
                new SparkassenAuthenticator(
                        catalog, supplementalInformationHelper, apiClient, persistentStorage);
    }

    @Test
    public void shouldThrowSessionExceptionWhenNoConsentStored() {
        // given

        // when
        Throwable throwable = catchThrowable(() -> authenticator.autoAuthenticate());

        // then
        assertThat(throwable).isInstanceOf(SessionException.class);
    }

    @Test
    public void shouldThrowSessionExceptionWhenConsentNotValid() {
        // given
        persistentStorage.saveConsentId(TEST_CONSENT_ID);
        when(apiClient.getConsentStatus(any())).thenReturn(CONSENT_STATUS_RESPONSE_NOT_OK);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.autoAuthenticate());

        // then
        verify(apiClient).getConsentStatus(any());
        verifyNoMoreInteractions(apiClient);
        assertThat(throwable).isInstanceOf(SessionException.class);
    }

    @Test
    public void shouldCompleteAutoAuthenticationWhenConsentStillValid() {
        // given
        persistentStorage.saveConsentId(TEST_CONSENT_ID);
        when(apiClient.getConsentStatus(TEST_CONSENT_ID)).thenReturn(CONSENT_STATUS_RESPONSE_OK);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.autoAuthenticate());

        // then
        verify(apiClient).getConsentStatus(any());
        verifyNoMoreInteractions(apiClient);
        assertThat(throwable).isNull();
    }

    private Object[] possibleWrongCredentialTypes() {
        return new Object[] {
            CredentialsTypes.MOBILE_BANKID,
            CredentialsTypes.KEYFOB,
            CredentialsTypes.FRAUD,
            CredentialsTypes.THIRD_PARTY_APP
        };
    }

    @Test
    @Parameters(method = "possibleWrongCredentialTypes")
    public void shouldThrowNotImplementedExceptionWhenProvidedWithWrongTypeOfCredentials(
            CredentialsTypes type) {
        // given
        Credentials credentials = new Credentials();
        credentials.setType(type);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(throwable).isInstanceOf(NotImplementedException.class);
    }

    private Object[] possibleWrongCredentials() {
        return new Object[] {
            new Object[] {"", null},
            new Object[] {"", ""},
            new Object[] {"", "ASDF"},
            new Object[] {null, null},
            new Object[] {null, ""},
            new Object[] {null, "ASDF"},
            new Object[] {"ASDF", null},
            new Object[] {"ASDF", ""},
        };
    }

    @Test
    @Parameters(method = "possibleWrongCredentials")
    public void shouldThrowLoginExceptionWhenSomeCredentialsAreMissing(
            String username, String password) {
        // given
        Credentials credentials = new Credentials();
        credentials.setType(CredentialsTypes.PASSWORD);
        credentials.setField(Field.Key.USERNAME, username);
        credentials.setField(Field.Key.PASSWORD, password);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(throwable).isInstanceOf(LoginException.class);
    }

    @Test
    public void shouldThrowHttpResponseExceptionWhenCreateConsentCallFails() throws LoginException {
        // given
        whenCreateConsentThrow(HTTP_RESPONSE_EXCEPTION);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.authenticate(OK_CREDENTIALS));

        // then
        assertThat(throwable).isEqualTo(HTTP_RESPONSE_EXCEPTION);
        verifyCreateConsentCalled();
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationHelper);
    }

    @Test
    public void shouldThrowIllegalStateExceptionWhenCreateConsentResponseMissingCrucialData()
            throws LoginException {
        // given
        whenCreateConsentReturn(CONSENT_RESPONSE_MISSING_LINK);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.authenticate(OK_CREDENTIALS));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(SparkassenConstants.ErrorMessages.MISSING_SCA_AUTHORIZATION_URL);
        verifyCreateConsentCalled();
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationHelper);
    }

    @Test
    public void shouldThrowHttpResponseExceptionWhenInitializeAuthorizationCallFails()
            throws AuthenticationException {
        // given
        whenCreateConsentReturn(CONSENT_RESPONSE_OK);
        whenInitializeAuthorizationThrow(HTTP_RESPONSE_EXCEPTION);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.authenticate(OK_CREDENTIALS));

        // then
        assertThat(persistentStorage.getConsentId()).isEqualTo(TEST_CONSENT_ID);
        assertThat(throwable).isEqualTo(HTTP_RESPONSE_EXCEPTION);
        verifyCreateConsentCalled();
        verifyInitializeAuthorizationCalled();
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationHelper);
    }

    @Test
    public void shouldThrowLoginExceptionWhenWrongCredentialsProvided()
            throws AuthenticationException {
        // given
        whenCreateConsentReturn(CONSENT_RESPONSE_OK);
        whenWrongCredentialsThrow(LOGIN_EXCEPTION);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.authenticate(OK_CREDENTIALS));

        // then
        assertThat(persistentStorage.getConsentId()).isEqualTo(TEST_CONSENT_ID);
        assertThat(throwable).isEqualTo(LOGIN_EXCEPTION);
        verifyCreateConsentCalled();
        verifyInitializeAuthorizationCalled();
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationHelper);
    }

    @Test
    public void shouldThrowIllegalStateExceptionWhenCreateConsentResponseHasNoSCAMethods()
            throws AuthenticationException {
        // given
        whenCreateConsentReturn(CONSENT_RESPONSE_OK);
        whenInitializeAuthorizationReturn(INIT_AUTH_RESPONSE_NO_METHOD);
        // when
        Throwable throwable = catchThrowable(() -> authenticator.authenticate(OK_CREDENTIALS));

        // then
        assertThat(persistentStorage.getConsentId()).isEqualTo(TEST_CONSENT_ID);
        assertThat(persistentStorage.getAuthorizationId()).isEqualTo(TEST_AUTHORIZATION_ID);

        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(SparkassenConstants.ErrorMessages.MISSING_SCA_METHOD_DETAILS);
        verifyCreateConsentCalled();
        verifyInitializeAuthorizationCalled();
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationHelper);
    }

    @Test
    public void shouldThrowHttpResponseExceptionWhenSelectAuthorizationMethodCallFails()
            throws AuthenticationException {

        // given
        whenCreateConsentReturn(CONSENT_RESPONSE_OK);
        whenInitializeAuthorizationReturn(INIT_AUTH_RESPONSE_OK_TWO_METHODS);
        whenSelectAuthorizationMethodThrow(HTTP_RESPONSE_EXCEPTION);
        whenSupplementalInformationHelperReturn(SUPPLEMENTAL_RESPONSE_OK);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.authenticate(OK_CREDENTIALS));

        // then
        assertThat(persistentStorage.getConsentId()).isEqualTo(TEST_CONSENT_ID);
        assertThat(persistentStorage.getAuthorizationId()).isEqualTo(TEST_AUTHORIZATION_ID);
        assertThat(throwable).isEqualTo(HTTP_RESPONSE_EXCEPTION);
        verifyCreateConsentCalled();
        verifyInitializeAuthorizationCalled();
        verifySelectAuthorizationMethodCalled();
        verifyAskSupplementalInformationCalled(1);
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationHelper);
    }

    @Test
    public void shouldThrowIllegalStateExceptionWhenSelectAuthMethodResponseMissingCrucialData()
            throws AuthenticationException {

        // given
        whenCreateConsentReturn(CONSENT_RESPONSE_OK);
        whenInitializeAuthorizationReturn(INIT_AUTH_RESPONSE_OK_TWO_METHODS);
        whenSelectAuthorizationMethodReturn(SELECT_AUTH_METHOD_NO_CHALLENGE_DATA);
        whenSupplementalInformationHelperReturn(SUPPLEMENTAL_RESPONSE_OK);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.authenticate(OK_CREDENTIALS));

        // then
        assertThat(persistentStorage.getConsentId()).isEqualTo(TEST_CONSENT_ID);
        assertThat(persistentStorage.getAuthorizationId()).isEqualTo(TEST_AUTHORIZATION_ID);
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(SparkassenConstants.ErrorMessages.MISSING_SCA_METHOD_DETAILS);
        verifyCreateConsentCalled();
        verifyInitializeAuthorizationCalled();
        verifySelectAuthorizationMethodCalled();
        verifyAskSupplementalInformationCalled(1);
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationHelper);
    }

    @Test
    public void shouldThrowSuppInfoExceptionWhenNoSCAMethodProvidedByUser()
            throws AuthenticationException {

        // given
        whenCreateConsentReturn(CONSENT_RESPONSE_OK);
        whenInitializeAuthorizationReturn(INIT_AUTH_RESPONSE_OK_TWO_METHODS);
        whenSupplementalInformationHelperThrow(SUPPLEMENTAL_INFO_EXCEPTION);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.authenticate(OK_CREDENTIALS));

        // then
        assertThat(persistentStorage.getConsentId()).isEqualTo(TEST_CONSENT_ID);
        assertThat(persistentStorage.getAuthorizationId()).isEqualTo(TEST_AUTHORIZATION_ID);
        assertThat(throwable).isInstanceOf(SupplementalInfoException.class);
        verifyCreateConsentCalled();
        verifyInitializeAuthorizationCalled();
        verifyAskSupplementalInformationCalled(1);
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationHelper);
    }

    @Test
    public void shouldThrowSuppInfoExceptionWhenNoOTPProvidedByUser()
            throws AuthenticationException {
        // given
        whenCreateConsentReturn(CONSENT_RESPONSE_OK);
        whenInitializeAuthorizationReturn(INIT_AUTH_RESPONSE_OK_ONE_METHOD);
        whenSupplementalInformationHelperThrow(SUPPLEMENTAL_INFO_EXCEPTION);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.authenticate(OK_CREDENTIALS));

        // then
        assertThat(persistentStorage.getConsentId()).isEqualTo(TEST_CONSENT_ID);
        assertThat(persistentStorage.getAuthorizationId()).isEqualTo(TEST_AUTHORIZATION_ID);
        assertThat(throwable).isInstanceOf(SupplementalInfoException.class);
        verifyCreateConsentCalled();
        verifyInitializeAuthorizationCalled();
        verifyAskSupplementalInformationCalled(1);
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationHelper);
    }

    @Test
    public void shouldThrowHttpResponseExceptionWhenFinalizeAuthorizationCallFails()
            throws AuthenticationException {
        // given
        whenCreateConsentReturn(CONSENT_RESPONSE_OK);
        whenInitializeAuthorizationReturn(INIT_AUTH_RESPONSE_OK_ONE_METHOD);
        whenFinalizeAuthorizationThrow(HTTP_RESPONSE_EXCEPTION);
        whenSupplementalInformationHelperReturn(SUPPLEMENTAL_RESPONSE_OK);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.authenticate(OK_CREDENTIALS));

        // then
        assertThat(persistentStorage.getConsentId()).isEqualTo(TEST_CONSENT_ID);
        assertThat(persistentStorage.getAuthorizationId()).isEqualTo(TEST_AUTHORIZATION_ID);
        assertThat(throwable).isEqualTo(HTTP_RESPONSE_EXCEPTION);
        verifyCreateConsentCalled();
        verifyInitializeAuthorizationCalled();
        verifyFinalizeAuthorizationCalled();
        verifyAskSupplementalInformationCalled(1);
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationHelper);
    }

    @Test
    public void shouldThrowLoginExceptionWhenFinalizeAuthorizationResponseHasFailedStatus()
            throws AuthenticationException {
        // given
        whenCreateConsentReturn(CONSENT_RESPONSE_OK);
        whenInitializeAuthorizationReturn(INIT_AUTH_RESPONSE_OK_ONE_METHOD);
        whenFinalizeAuthorizationReturn(FINALIZE_AUTH_FAILED);
        whenSupplementalInformationHelperReturn(SUPPLEMENTAL_RESPONSE_OK);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.authenticate(OK_CREDENTIALS));

        // then
        assertThat(persistentStorage.getConsentId()).isEqualTo(TEST_CONSENT_ID);
        assertThat(persistentStorage.getAuthorizationId()).isEqualTo(TEST_AUTHORIZATION_ID);
        assertThat(throwable).isInstanceOf(LoginException.class);
        verifyCreateConsentCalled();
        verifyInitializeAuthorizationCalled();
        verifyFinalizeAuthorizationCalled();
        verifyAskSupplementalInformationCalled(1);
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationHelper);
    }

    @Test
    public void shouldThrowLoginExceptionWhenFinalizeAuthorizationResponseHasUnsupportedStatus()
            throws AuthenticationException {
        // given
        whenCreateConsentReturn(CONSENT_RESPONSE_OK);
        whenInitializeAuthorizationReturn(INIT_AUTH_RESPONSE_OK_ONE_METHOD);
        whenFinalizeAuthorizationReturn(FINALIZE_AUTH_OTHER);
        whenSupplementalInformationHelperReturn(SUPPLEMENTAL_RESPONSE_OK);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.authenticate(OK_CREDENTIALS));

        // then
        assertThat(persistentStorage.getConsentId()).isEqualTo(TEST_CONSENT_ID);
        assertThat(persistentStorage.getAuthorizationId()).isEqualTo(TEST_AUTHORIZATION_ID);
        assertThat(throwable).isInstanceOf(LoginException.class);
        verifyCreateConsentCalled();
        verifyInitializeAuthorizationCalled();
        verifyFinalizeAuthorizationCalled();
        verifyAskSupplementalInformationCalled(1);
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationHelper);
    }

    @Test
    public void shouldCompleteAuthenticationWhenWithoutSelectingSCAMethod()
            throws AuthenticationException, AuthorizationException {
        // given
        whenCreateConsentReturn(CONSENT_RESPONSE_OK);
        whenInitializeAuthorizationReturn(INIT_AUTH_RESPONSE_OK_ONE_METHOD);
        whenFinalizeAuthorizationReturn(FINALIZE_AUTH_OK);
        whenSupplementalInformationHelperReturn(SUPPLEMENTAL_RESPONSE_OK);

        // when
        authenticator.authenticate(OK_CREDENTIALS);

        // then
        assertThat(persistentStorage.getConsentId()).isEqualTo(TEST_CONSENT_ID);
        assertThat(persistentStorage.getAuthorizationId()).isEqualTo(TEST_AUTHORIZATION_ID);
        verifyCreateConsentCalled();
        verifyInitializeAuthorizationCalled();
        verifyFinalizeAuthorizationCalled();
        verifyAskSupplementalInformationCalled(1);
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationHelper);
    }

    @Test
    public void shouldCompleteAuthenticationWhenWithSelectingSCAMethod()
            throws AuthenticationException, AuthorizationException {
        // given
        whenCreateConsentReturn(CONSENT_RESPONSE_OK);
        whenInitializeAuthorizationReturn(INIT_AUTH_RESPONSE_OK_TWO_METHODS);
        whenSelectAuthorizationMethodReturn(SELECT_AUTH_METHOD_OK);
        whenFinalizeAuthorizationReturn(FINALIZE_AUTH_OK);
        whenSupplementalInformationHelperReturn(SUPPLEMENTAL_RESPONSE_OK);

        // when
        authenticator.authenticate(OK_CREDENTIALS);

        // then
        assertThat(persistentStorage.getConsentId()).isEqualTo(TEST_CONSENT_ID);
        assertThat(persistentStorage.getAuthorizationId()).isEqualTo(TEST_AUTHORIZATION_ID);
        verifyCreateConsentCalled();
        verifyInitializeAuthorizationCalled();
        verifySelectAuthorizationMethodCalled();
        verifyFinalizeAuthorizationCalled();
        verifyAskSupplementalInformationCalled(2);
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationHelper);
    }

    private void whenCreateConsentReturn(ConsentResponse createConsentResult)
            throws LoginException {
        when(apiClient.createConsent()).thenReturn(createConsentResult);
    }

    private void whenCreateConsentThrow(HttpResponseException httpResponseException)
            throws LoginException {
        when(apiClient.createConsent()).thenThrow(httpResponseException);
    }

    private void verifyCreateConsentCalled() throws LoginException {
        verify(apiClient).createConsent();
    }

    private void whenInitializeAuthorizationReturn(
            AuthenticationMethodResponse initializeAuthorizationResult)
            throws AuthenticationException {
        when(apiClient.initializeAuthorization(TEST_AUTH_URL, OK_USERNAME, OK_PASSWORD))
                .thenReturn(initializeAuthorizationResult);
    }

    private void whenInitializeAuthorizationThrow(HttpResponseException httpResponseException)
            throws AuthenticationException {
        when(apiClient.initializeAuthorization(TEST_AUTH_URL, OK_USERNAME, OK_PASSWORD))
                .thenThrow(httpResponseException);
    }

    private void whenWrongCredentialsThrow(LoginException loginException)
            throws AuthenticationException {
        when(apiClient.initializeAuthorization(TEST_AUTH_URL, OK_USERNAME, OK_PASSWORD))
                .thenThrow(loginException);
    }

    private void verifyInitializeAuthorizationCalled() throws AuthenticationException {
        verify(apiClient).initializeAuthorization(TEST_AUTH_URL, OK_USERNAME, OK_PASSWORD);
    }

    private void whenSelectAuthorizationMethodReturn(
            AuthenticationMethodResponse selectAuthorizationMethodResult) {
        when(apiClient.selectAuthorizationMethod(
                        TEST_CONSENT_ID, TEST_AUTHORIZATION_ID, TEST_SCA_METHOD_ID))
                .thenReturn(selectAuthorizationMethodResult);
    }

    private void whenSelectAuthorizationMethodThrow(HttpResponseException httpResponseException) {
        when(apiClient.selectAuthorizationMethod(
                        TEST_CONSENT_ID, TEST_AUTHORIZATION_ID, TEST_SCA_METHOD_ID))
                .thenThrow(httpResponseException);
    }

    private void verifySelectAuthorizationMethodCalled() {
        verify(apiClient)
                .selectAuthorizationMethod(
                        TEST_CONSENT_ID, TEST_AUTHORIZATION_ID, TEST_SCA_METHOD_ID);
    }

    private void whenFinalizeAuthorizationReturn(
            FinalizeAuthorizationResponse finalizeAuthorizationResult) {
        when(apiClient.finalizeAuthorization(TEST_CONSENT_ID, TEST_AUTHORIZATION_ID, TEST_OTP))
                .thenReturn(finalizeAuthorizationResult);
    }

    private void whenFinalizeAuthorizationThrow(HttpResponseException httpResponseException) {
        when(apiClient.finalizeAuthorization(TEST_CONSENT_ID, TEST_AUTHORIZATION_ID, TEST_OTP))
                .thenThrow(httpResponseException);
    }

    private void verifyFinalizeAuthorizationCalled() {
        verify(apiClient).finalizeAuthorization(TEST_CONSENT_ID, TEST_AUTHORIZATION_ID, TEST_OTP);
    }

    private void whenSupplementalInformationHelperReturn(
            Map<String, String> askSupplementalInformationResult) throws SupplementalInfoException {
        when(supplementalInformationHelper.askSupplementalInformation(any()))
                .thenReturn(askSupplementalInformationResult);
    }

    private void whenSupplementalInformationHelperThrow(
            SupplementalInfoException supplementalInfoException) throws SupplementalInfoException {
        when(supplementalInformationHelper.askSupplementalInformation(any()))
                .thenThrow(supplementalInfoException);
    }

    private void verifyAskSupplementalInformationCalled(int times)
            throws SupplementalInfoException {
        verify(supplementalInformationHelper, times(times)).askSupplementalInformation(any());
    }
}
