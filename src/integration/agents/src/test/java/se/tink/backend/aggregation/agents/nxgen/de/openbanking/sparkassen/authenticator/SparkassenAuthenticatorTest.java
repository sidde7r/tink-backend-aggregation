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
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.CONSENT_DETAILS_RESPONSE_EXPIRED;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.CONSENT_DETAILS_RESPONSE_VALID;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.CONSENT_RESPONSE_OK;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.FINALIZE_AUTH_FAILED;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.FINALIZE_AUTH_OK;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.FINALIZE_AUTH_OTHER;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.HTTP_RESPONSE_EXCEPTION;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.INIT_AUTH_RESPONSE_NO_METHOD;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.INIT_AUTH_RESPONSE_OK_ONE_METHOD;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.INIT_AUTH_RESPONSE_OK_TWO_METHODS;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.LOGIN_EXCEPTION;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.LOGIN_EXCEPTION_INCORRECT_CHALLENGE;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.PASSWORD;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.SELECT_AUTH_METHOD_NO_CHALLENGE_DATA;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.SELECT_AUTH_METHOD_OK;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.SUPPLEMENTAL_INFO_EXCEPTION;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.TEST_AUTH_SINGLE_URL;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.TEST_AUTH_URL;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.TEST_CONSENT_ID;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.TEST_OTP;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.TEST_SCA_METHOD_ID;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.USERNAME;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenStorage;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.detail.ScaMethodFilter;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.detail.SparkassenDecoupledFieldBuilder;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.detail.SparkassenEmbeddedFieldBuilder;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.detail.SparkassenIconUrlMapper;
import se.tink.backend.aggregation.agents.utils.authentication.AuthenticationType;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationStatusResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ScaMethodEntity;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

@RunWith(JUnitParamsRunner.class)
public class SparkassenAuthenticatorTest {

    private SupplementalInformationController supplementalInformationController;
    private SparkassenApiClient apiClient;
    private SparkassenStorage storage;

    private SparkassenAuthenticator authenticator;
    private Credentials credentials;

    @Before
    public void setup() {
        Catalog catalog = mock(Catalog.class);
        supplementalInformationController = mock(SupplementalInformationController.class);
        apiClient = mock(SparkassenApiClient.class);
        storage = new SparkassenStorage(new PersistentStorage());

        credentials = new Credentials();
        credentials.setType(CredentialsTypes.PASSWORD);
        credentials.setField(Field.Key.USERNAME, USERNAME);
        credentials.setField(Field.Key.PASSWORD, PASSWORD);
        when(catalog.getString(any(LocalizableKey.class))).thenReturn("");
        authenticator =
                new SparkassenAuthenticator(
                        apiClient,
                        supplementalInformationController,
                        storage,
                        credentials,
                        new SparkassenEmbeddedFieldBuilder(catalog, new SparkassenIconUrlMapper()),
                        new SparkassenDecoupledFieldBuilder(catalog),
                        new ScaMethodFilter());
    }

    @Test
    public void shouldThrowSessionExceptionWhenNoConsentStored() {
        // given

        // when
        Throwable throwable = catchThrowable(authenticator::autoAuthenticate);

        // then
        assertThat(throwable).isInstanceOf(SessionException.class);
    }

    @Test
    public void shouldThrowSessionExceptionWhenConsentNotValid() {
        // given
        storage.saveConsentId(TEST_CONSENT_ID);
        when(apiClient.getConsentDetails(any())).thenReturn(CONSENT_DETAILS_RESPONSE_EXPIRED);

        // when
        Throwable throwable = catchThrowable(authenticator::autoAuthenticate);

        // then
        verify(apiClient).getConsentDetails(any());
        verifyNoMoreInteractions(apiClient);
        assertThat(throwable).isInstanceOf(SessionException.class);
    }

    @Test
    public void shouldCompleteAutoAuthenticationWhenConsentStillValid() {
        // given
        storage.saveConsentId(TEST_CONSENT_ID);
        when(apiClient.getConsentDetails(TEST_CONSENT_ID))
                .thenReturn(CONSENT_DETAILS_RESPONSE_VALID);

        // when
        authenticator.autoAuthenticate();

        // then
        verify(apiClient).getConsentDetails(any());
        verifyNoMoreInteractions(apiClient);
    }

    @Test
    public void shouldRethrowExceptionWhenCheckingConsentValidtyThrowsException() {
        // given
        storage.saveConsentId(TEST_CONSENT_ID);
        RuntimeException exception = new RuntimeException();
        when(apiClient.getConsentDetails(TEST_CONSENT_ID)).thenThrow(exception);

        // when
        Throwable throwable = catchThrowable(authenticator::autoAuthenticate);

        // then
        verify(apiClient).getConsentDetails(any());
        verifyNoMoreInteractions(apiClient);
        assertThat(throwable).isEqualTo(exception);
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
    public void shouldThrowHttpResponseExceptionWhenCreateConsentCallFails() {
        // given
        whenCreateConsentThrow(HTTP_RESPONSE_EXCEPTION);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(throwable).isEqualTo(HTTP_RESPONSE_EXCEPTION);
        verifyCreateConsentCalled();
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationController);
    }

    @Test
    public void shouldThrowHttpResponseExceptionWhenInitializeAuthorizationCallFails() {
        // given
        whenCreateConsentReturn(CONSENT_RESPONSE_OK);
        whenInitializeAuthorizationThrow(HTTP_RESPONSE_EXCEPTION);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(storage.getConsentId()).isEqualTo(TEST_CONSENT_ID);
        assertThat(throwable).isEqualTo(HTTP_RESPONSE_EXCEPTION);
        verifyCreateConsentCalled();
        verifyInitializeAuthorizationCalled();
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationController);
    }

    @Test
    public void shouldThrowLoginExceptionWhenWrongCredentialsProvided() {
        // given
        whenCreateConsentReturn(CONSENT_RESPONSE_OK);
        whenWrongCredentialsThrow(LOGIN_EXCEPTION);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(storage.getConsentId()).isEqualTo(TEST_CONSENT_ID);
        assertThat(throwable).isEqualTo(LOGIN_EXCEPTION);
        verifyCreateConsentCalled();
        verifyInitializeAuthorizationCalled();
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationController);
    }

    @Test
    public void shouldThrowIllegalStateExceptionWhenCreateConsentResponseHasNoSCAMethods() {
        // given
        whenCreateConsentReturn(CONSENT_RESPONSE_OK);
        whenInitializeAuthorizationReturn(INIT_AUTH_RESPONSE_NO_METHOD);
        // when
        Throwable throwable = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(storage.getConsentId()).isEqualTo(TEST_CONSENT_ID);

        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Unexpected ScaStatus during authorization whatever");
        verifyCreateConsentCalled();
        verifyInitializeAuthorizationCalled();
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationController);
    }

    @Test
    @Parameters(value = {"kopytko", "0", "-10", "500"})
    public void shouldThrowSupplementalExceptionWhenUserSelectsMethodWrongly(String selectedValue) {

        // given
        whenCreateConsentReturn(CONSENT_RESPONSE_OK);
        whenInitializeAuthorizationReturn(INIT_AUTH_RESPONSE_OK_TWO_METHODS);
        whenSupplementalInformationControllerReturn(INIT_AUTH_RESPONSE_OK_TWO_METHODS);

        Map<String, String> supplementalInformation = new HashMap<>();
        supplementalInformation.put("selectAuthMethodField", selectedValue);
        when(supplementalInformationController.askSupplementalInformationSync(any()))
                .thenReturn(supplementalInformation);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(storage.getConsentId()).isEqualTo(TEST_CONSENT_ID);
        assertThat(throwable)
                .isInstanceOf(SupplementalInfoException.class)
                .hasMessage("Could not map user input to list of available options.");
        verifyCreateConsentCalled();
        verifyInitializeAuthorizationCalled();
        verifyAskSupplementalInformationCalled(1);
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationController);
    }

    @Test
    public void shouldThrowHttpResponseExceptionWhenSelectAuthorizationMethodCallFails() {

        // given
        whenCreateConsentReturn(CONSENT_RESPONSE_OK);
        whenInitializeAuthorizationReturn(INIT_AUTH_RESPONSE_OK_TWO_METHODS);
        whenSelectAuthorizationMethodThrow(HTTP_RESPONSE_EXCEPTION);
        whenSupplementalInformationControllerReturn(INIT_AUTH_RESPONSE_OK_TWO_METHODS);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(storage.getConsentId()).isEqualTo(TEST_CONSENT_ID);
        assertThat(throwable).isEqualTo(HTTP_RESPONSE_EXCEPTION);
        verifyCreateConsentCalled();
        verifyInitializeAuthorizationCalled();
        verifySelectAuthorizationMethodCalled();
        verifyAskSupplementalInformationCalled(1);
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationController);
    }

    @Test
    public void shouldThrowIllegalStateExceptionWhenSelectAuthMethodResponseMissingCrucialData() {

        // given
        whenCreateConsentReturn(CONSENT_RESPONSE_OK);
        whenInitializeAuthorizationReturn(INIT_AUTH_RESPONSE_OK_TWO_METHODS);
        whenSelectAuthorizationMethodReturn(SELECT_AUTH_METHOD_NO_CHALLENGE_DATA);
        whenSupplementalInformationControllerReturn(SELECT_AUTH_METHOD_NO_CHALLENGE_DATA);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(storage.getConsentId()).isEqualTo(TEST_CONSENT_ID);
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(SparkassenConstants.ErrorMessages.MISSING_SCA_METHOD_DETAILS);
        verifyCreateConsentCalled();
        verifyInitializeAuthorizationCalled();
        verifySelectAuthorizationMethodCalled();
        verifyAskSupplementalInformationCalled(1);
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationController);
    }

    @Test
    public void shouldThrowSuppInfoExceptionWhenNoSCAMethodProvidedByUser() {

        // given
        whenCreateConsentReturn(CONSENT_RESPONSE_OK);
        whenInitializeAuthorizationReturn(INIT_AUTH_RESPONSE_OK_TWO_METHODS);
        whenSupplementalInformationControllerThrow(SUPPLEMENTAL_INFO_EXCEPTION);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(storage.getConsentId()).isEqualTo(TEST_CONSENT_ID);
        assertThat(throwable).isInstanceOf(SupplementalInfoException.class);
        verifyCreateConsentCalled();
        verifyInitializeAuthorizationCalled();
        verifyAskSupplementalInformationCalled(1);
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationController);
    }

    @Test
    public void shouldThrowSuppInfoExceptionWhenNoOTPProvidedByUser() {
        // given
        whenCreateConsentReturn(CONSENT_RESPONSE_OK);
        whenInitializeAuthorizationReturn(INIT_AUTH_RESPONSE_OK_ONE_METHOD);
        whenSupplementalInformationControllerThrow(SUPPLEMENTAL_INFO_EXCEPTION);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(storage.getConsentId()).isEqualTo(TEST_CONSENT_ID);
        assertThat(throwable).isInstanceOf(SupplementalInfoException.class);
        verifyCreateConsentCalled();
        verifyInitializeAuthorizationCalled();
        verifyAskSupplementalInformationCalled(1);
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationController);
    }

    @Test
    public void shouldThrowHttpResponseExceptionWhenFinalizeAuthorizationCallFails() {
        // given
        whenCreateConsentReturn(CONSENT_RESPONSE_OK);
        whenInitializeAuthorizationReturn(INIT_AUTH_RESPONSE_OK_ONE_METHOD);
        whenFinalizeAuthorizationThrow(LOGIN_EXCEPTION_INCORRECT_CHALLENGE);
        whenSupplementalInformationControllerReturn(INIT_AUTH_RESPONSE_OK_ONE_METHOD);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(storage.getConsentId()).isEqualTo(TEST_CONSENT_ID);
        assertThat(throwable).isEqualTo(LOGIN_EXCEPTION_INCORRECT_CHALLENGE);
        verifyCreateConsentCalled();
        verifyInitializeAuthorizationCalled();
        verifyFinalizeAuthorizationCalled();
        verifyAskSupplementalInformationCalled(1);
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationController);
    }

    @Test
    public void shouldThrowProperLoginExceptionWhenFinalizeAuthorizationCalledWithWrongOTP() {
        // given
        whenCreateConsentReturn(CONSENT_RESPONSE_OK);
        whenInitializeAuthorizationReturn(INIT_AUTH_RESPONSE_OK_ONE_METHOD);
        whenFinalizeAuthorizationThrow(LOGIN_EXCEPTION_INCORRECT_CHALLENGE);
        whenSupplementalInformationControllerReturn(INIT_AUTH_RESPONSE_OK_ONE_METHOD);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(storage.getConsentId()).isEqualTo(TEST_CONSENT_ID);
        assertThat(throwable).isEqualTo(LOGIN_EXCEPTION_INCORRECT_CHALLENGE);
        verifyCreateConsentCalled();
        verifyInitializeAuthorizationCalled();
        verifyFinalizeAuthorizationCalled();
        verifyAskSupplementalInformationCalled(1);
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationController);
    }

    @Test
    public void shouldThrowLoginExceptionWhenFinalizeAuthorizationResponseHasFailedStatus() {
        // given
        whenCreateConsentReturn(CONSENT_RESPONSE_OK);
        whenInitializeAuthorizationReturn(INIT_AUTH_RESPONSE_OK_ONE_METHOD);
        whenFinalizeAuthorizationReturn(FINALIZE_AUTH_FAILED);
        whenSupplementalInformationControllerReturn(INIT_AUTH_RESPONSE_OK_ONE_METHOD);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(storage.getConsentId()).isEqualTo(TEST_CONSENT_ID);
        assertThat(throwable).isInstanceOf(LoginException.class);
        verifyCreateConsentCalled();
        verifyInitializeAuthorizationCalled();
        verifyFinalizeAuthorizationCalled();
        verifyAskSupplementalInformationCalled(1);
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationController);
    }

    @Test
    public void shouldThrowLoginExceptionWhenFinalizeAuthorizationResponseHasUnsupportedStatus() {
        // given
        whenCreateConsentReturn(CONSENT_RESPONSE_OK);
        whenInitializeAuthorizationReturn(INIT_AUTH_RESPONSE_OK_ONE_METHOD);
        whenFinalizeAuthorizationReturn(FINALIZE_AUTH_OTHER);
        whenSupplementalInformationControllerReturn(INIT_AUTH_RESPONSE_OK_ONE_METHOD);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(storage.getConsentId()).isEqualTo(TEST_CONSENT_ID);
        assertThat(throwable).isInstanceOf(LoginException.class);
        verifyCreateConsentCalled();
        verifyInitializeAuthorizationCalled();
        verifyFinalizeAuthorizationCalled();
        verifyAskSupplementalInformationCalled(1);
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationController);
    }

    @Test
    public void shouldCompleteAuthenticationWhenWithoutSelectingSCAMethod() {
        // given
        whenCreateConsentReturn(CONSENT_RESPONSE_OK);
        whenInitializeAuthorizationReturn(INIT_AUTH_RESPONSE_OK_ONE_METHOD);
        whenFinalizeAuthorizationReturn(FINALIZE_AUTH_OK);
        whenSupplementalInformationControllerReturn(INIT_AUTH_RESPONSE_OK_ONE_METHOD);
        whenGetConsentDetailsReturn(CONSENT_DETAILS_RESPONSE_VALID);

        // when
        authenticator.authenticate(credentials);

        // then
        assertThat(storage.getConsentId()).isEqualTo(TEST_CONSENT_ID);
        verifyCreateConsentCalled();
        verifyInitializeAuthorizationCalled();
        verifyFinalizeAuthorizationCalled();
        verifyAskSupplementalInformationCalled(1);
        verifyGetConsentDetails();
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationController);
    }

    @Test
    public void shouldCompleteAuthenticationWhenWithSelectingSCAMethod() {
        // given
        whenCreateConsentReturn(CONSENT_RESPONSE_OK);
        whenInitializeAuthorizationReturn(INIT_AUTH_RESPONSE_OK_TWO_METHODS);
        whenSelectAuthorizationMethodReturn(SELECT_AUTH_METHOD_OK);
        whenFinalizeAuthorizationReturn(FINALIZE_AUTH_OK);
        whenSupplementalInformationControllerReturn(SELECT_AUTH_METHOD_OK);
        whenGetConsentDetailsReturn(CONSENT_DETAILS_RESPONSE_VALID);

        // when
        authenticator.authenticate(credentials);

        // then
        assertThat(storage.getConsentId()).isEqualTo(TEST_CONSENT_ID);
        verifyCreateConsentCalled();
        verifyInitializeAuthorizationCalled();
        verifySelectAuthorizationMethodCalled();
        verifyFinalizeAuthorizationCalled();
        verifyAskSupplementalInformationCalled(2);
        verifyGetConsentDetails();
        verifyNoMoreInteractions(apiClient);
        verifyNoMoreInteractions(supplementalInformationController);
    }

    @Test
    public void shouldSetSessionExpiryWhenUserIsAuthenticated() {
        // given
        whenCreateConsentReturn(CONSENT_RESPONSE_OK);
        whenInitializeAuthorizationReturn(INIT_AUTH_RESPONSE_OK_TWO_METHODS);
        whenSelectAuthorizationMethodReturn(SELECT_AUTH_METHOD_OK);
        whenFinalizeAuthorizationReturn(FINALIZE_AUTH_OK);
        whenSupplementalInformationControllerReturn(SELECT_AUTH_METHOD_OK);
        whenGetConsentDetailsReturn(CONSENT_DETAILS_RESPONSE_VALID);

        // when
        authenticator.authenticate(credentials);

        // then
        assertThat(credentials.getSessionExpiryDate()).isEqualTo("2030-01-01");
    }

    @Test
    public void shouldSetSessionExpiryWhenAutoAuthenticate() {
        // given
        storage.saveConsentId(TEST_CONSENT_ID);
        when(apiClient.getConsentDetails(any())).thenReturn(CONSENT_DETAILS_RESPONSE_VALID);

        Credentials credentials = new Credentials();
        credentials.setSessionExpiryDate(LocalDate.parse("2029-01-01"));
        Catalog catalog = mock(Catalog.class);
        authenticator =
                new SparkassenAuthenticator(
                        apiClient,
                        supplementalInformationController,
                        storage,
                        credentials,
                        new SparkassenEmbeddedFieldBuilder(catalog, new SparkassenIconUrlMapper()),
                        new SparkassenDecoupledFieldBuilder(catalog),
                        new ScaMethodFilter());

        // when
        authenticator.autoAuthenticate();

        // then
        assertThat(credentials.getSessionExpiryDate()).isEqualTo("2030-01-01");
    }

    private void whenCreateConsentReturn(ConsentResponse createConsentResult) {
        when(apiClient.createConsent()).thenReturn(createConsentResult);
    }

    private void whenGetConsentDetailsReturn(ConsentDetailsResponse consentDetailsResponse) {
        when(apiClient.getConsentDetails(anyString())).thenReturn(consentDetailsResponse);
    }

    private void whenCreateConsentThrow(HttpResponseException httpResponseException) {
        when(apiClient.createConsent()).thenThrow(httpResponseException);
    }

    private void verifyCreateConsentCalled() {
        verify(apiClient).createConsent();
    }

    private void verifyGetConsentDetails() {
        verify(apiClient).getConsentDetails(anyString());
    }

    private void whenInitializeAuthorizationReturn(
            AuthorizationResponse initializeAuthorizationResult) {
        when(apiClient.initializeAuthorization(TEST_AUTH_URL, USERNAME, PASSWORD))
                .thenReturn(initializeAuthorizationResult);
    }

    private void whenInitializeAuthorizationThrow(HttpResponseException httpResponseException) {
        when(apiClient.initializeAuthorization(TEST_AUTH_URL, USERNAME, PASSWORD))
                .thenThrow(httpResponseException);
    }

    private void whenWrongCredentialsThrow(LoginException loginException) {
        when(apiClient.initializeAuthorization(TEST_AUTH_URL, USERNAME, PASSWORD))
                .thenThrow(loginException);
    }

    private void verifyInitializeAuthorizationCalled() {
        verify(apiClient).initializeAuthorization(TEST_AUTH_URL, USERNAME, PASSWORD);
    }

    private void whenSelectAuthorizationMethodReturn(
            AuthorizationResponse selectAuthorizationMethodResult) {
        when(apiClient.selectAuthorizationMethod(TEST_AUTH_SINGLE_URL, TEST_SCA_METHOD_ID))
                .thenReturn(selectAuthorizationMethodResult);
    }

    private void whenSelectAuthorizationMethodThrow(HttpResponseException httpResponseException) {
        when(apiClient.selectAuthorizationMethod(TEST_AUTH_SINGLE_URL, TEST_SCA_METHOD_ID))
                .thenThrow(httpResponseException);
    }

    private void verifySelectAuthorizationMethodCalled() {
        verify(apiClient).selectAuthorizationMethod(TEST_AUTH_SINGLE_URL, TEST_SCA_METHOD_ID);
    }

    private void whenFinalizeAuthorizationReturn(
            AuthorizationStatusResponse finalizeAuthorizationResult) {
        when(apiClient.finalizeAuthorization(TEST_AUTH_SINGLE_URL, TEST_OTP))
                .thenReturn(finalizeAuthorizationResult);
    }

    private void whenFinalizeAuthorizationThrow(Throwable throwable) {
        when(apiClient.finalizeAuthorization(TEST_AUTH_SINGLE_URL, TEST_OTP)).thenThrow(throwable);
    }

    private void verifyFinalizeAuthorizationCalled() {
        verify(apiClient).finalizeAuthorization(TEST_AUTH_SINGLE_URL, TEST_OTP);
    }

    private void whenSupplementalInformationControllerReturn(
            AuthorizationResponse authorizationResponse) {
        Map<String, String> supplementalInformation = new HashMap<>();
        supplementalInformation.put(
                getFieldName(authorizationResponse.getChosenScaMethod()), TEST_OTP);
        supplementalInformation.put("selectAuthMethodField", "1");

        when(supplementalInformationController.askSupplementalInformationSync(any()))
                .thenReturn(supplementalInformation);
    }

    private String getFieldName(ScaMethodEntity scaMethodEntity) {
        if (scaMethodEntity != null) {
            AuthenticationType authenticationType =
                    AuthenticationType.fromString(scaMethodEntity.getAuthenticationType()).get();
            switch (authenticationType) {
                case CHIP_OTP:
                    return "chipTan";
                case SMS_OTP:
                    return "smsTan";
                case PUSH_OTP:
                    return "pushTan";
            }
        }
        return "tanField";
    }

    private void whenSupplementalInformationControllerThrow(
            SupplementalInfoException supplementalInfoException) {
        when(supplementalInformationController.askSupplementalInformationSync(any()))
                .thenThrow(supplementalInfoException);
    }

    private void verifyAskSupplementalInformationCalled(int times) {
        verify(supplementalInformationController, times(times))
                .askSupplementalInformationSync(any());
    }
}
