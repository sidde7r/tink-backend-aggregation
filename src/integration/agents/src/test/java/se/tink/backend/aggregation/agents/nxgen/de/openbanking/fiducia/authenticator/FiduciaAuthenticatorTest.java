package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.TestDataReader;
import se.tink.backend.aggregation.agents.utils.authentication.AuthenticationType;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationStatusResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ScaMethodEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

@RunWith(JUnitParamsRunner.class)
public class FiduciaAuthenticatorTest {

    private static final String USERNAME = StringUtils.repeat("A", 30);
    private static final String USERNAME_LONGER_THAN_ALLOWED = StringUtils.repeat("1", 31);
    private static final String PASSWORD = "password";
    private static final String CONSENT_ID = "consentId";
    private static final String OTP_CODE = "123456";
    private static final String STARTCODE = "555777999666";
    private static final String SCA_METHOD_ID_CHIP_TAN = "962";
    private static final String AUTH_START_PATH = "/v1/consents/dummy_consent_id/authorisations";
    private static final String AUTH_PATH =
            "/v1/consents/dummy_consent_id/authorisations/dummy_authorization_id";
    private static final String AUTH_PATH_PAYMENT =
            "/v1/payments/pain.001-sepa-credit-transfers/asdf/authorisations";
    private static final String CONSENT_VALID_UNTIL = "2021-03-17";

    private FiduciaAuthenticator authenticator;
    private FiduciaAuthenticator paymentAuthenticator;
    private FiduciaApiClient apiClient;
    private PersistentStorage persistentStorage;
    private SupplementalInformationController supplementalInformationController;

    private Credentials credentials;

    @Captor ArgumentCaptor<Field> fieldCaptor;

    @Before
    public void setup() {
        apiClient = mock(FiduciaApiClient.class);
        persistentStorage = mock(PersistentStorage.class);
        supplementalInformationController = mock(SupplementalInformationController.class);
        Catalog catalog = mock(Catalog.class);

        credentials = new Credentials();

        authenticator =
                new FiduciaAuthenticator(
                        credentials,
                        apiClient,
                        persistentStorage,
                        supplementalInformationController,
                        new FiduciaEmbeddedFieldBuilder(catalog, new FiduciaIconUrlMapper()));

        paymentAuthenticator =
                new FiduciaAuthenticator(
                        credentials,
                        apiClient,
                        persistentStorage,
                        supplementalInformationController,
                        new FiduciaPaymentsEmbeddedFieldBuilder(
                                catalog, new FiduciaIconUrlMapper()));

        when(catalog.getString(any(LocalizableKey.class))).thenReturn("");

        fieldCaptor = ArgumentCaptor.forClass(Field.class);
    }

    private void beforeFullAuth() {
        insertDummyCredentials();
        when(apiClient.createConsent(USERNAME))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.CONSENT_CREATED, ConsentResponse.class));
    }

    private void insertDummyCredentials() {
        credentials.setField(CredentialKeys.PSU_ID, USERNAME);
        credentials.setField(Field.Key.PASSWORD, PASSWORD);
    }

    @Test
    public void authenticateShouldThrowExceptionIfPsuIsInvalid() {
        // given
        credentials.setField(CredentialKeys.PSU_ID, USERNAME_LONGER_THAN_ALLOWED);
        credentials.setField(Field.Key.PASSWORD, PASSWORD);

        // when
        Throwable t = catchThrowable(() -> authenticator.authenticate(credentials));

        // then
        assertThat(t)
                .isInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CREDENTIALS");
    }

    @Test
    public void authenticateShouldInvokeApiClientAndSaveDataInStorage()
            throws SupplementalInfoException {
        // given
        beforeFullAuth();
        AuthorizationResponse authorizationResponse =
                TestDataReader.readFromFile(
                        TestDataReader.SCA_RESPONSE_SELECTED, AuthorizationResponse.class);
        when(apiClient.authorizeWithPassword(AUTH_START_PATH, PASSWORD))
                .thenReturn(authorizationResponse);
        when(apiClient.authorizeWithOtp(AUTH_PATH, OTP_CODE))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.SCA_FINALISED, AuthorizationStatusResponse.class));
        when(apiClient.getConsentDetails(CONSENT_ID))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.CONSENT_DETAILS_VALID,
                                ConsentDetailsResponse.class));
        whenSupplementalInformationControllerReturn(authorizationResponse);

        // when
        authenticator.authenticate(credentials);

        // then
        verify(persistentStorage).put(StorageKeys.CONSENT_ID, CONSENT_ID);
        verify(apiClient).createConsent(USERNAME);
        verify(apiClient).authorizeWithPassword(AUTH_START_PATH, PASSWORD);
        verify(apiClient).authorizeWithOtp(AUTH_PATH, OTP_CODE);
        verify(apiClient).getConsentDetails(CONSENT_ID);
        verifyNoMoreInteractions(apiClient);

        // and verify supplement interactions
        verify(supplementalInformationController)
                .askSupplementalInformationSync(fieldCaptor.capture());
        List<Field> allValues = fieldCaptor.getAllValues();
        assertThat(allValues).hasSize(1);
        assertThat(allValues.get(0).getName()).isEqualTo("pushTan");

        assertThat(credentials.getSessionExpiryDate()).isEqualTo(parseIsoDate(CONSENT_VALID_UNTIL));
    }

    @Test
    public void authenticateShouldInvokeApiClientAndSaveDataInStorageWithChipTanSelected()
            throws SupplementalInfoException {
        // given
        beforeFullAuth();
        when(apiClient.authorizeWithPassword(AUTH_START_PATH, PASSWORD))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.SCA_RESPONSE_MULTIPLE, AuthorizationResponse.class));

        AuthorizationResponse authorizationResponse =
                TestDataReader.readFromFile(
                        TestDataReader.SCA_RESPONSE_SELECTED_CHIP_TAN, AuthorizationResponse.class);
        whenSupplementalInformationControllerReturn(authorizationResponse);
        when(apiClient.selectAuthMethod(AUTH_PATH, SCA_METHOD_ID_CHIP_TAN))
                .thenReturn(authorizationResponse);

        when(apiClient.authorizeWithOtp(AUTH_PATH, OTP_CODE))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.SCA_FINALISED, AuthorizationStatusResponse.class));
        when(apiClient.getConsentDetails(CONSENT_ID))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.CONSENT_DETAILS_VALID,
                                ConsentDetailsResponse.class));

        // when
        authenticator.authenticate(credentials);

        // then
        verify(persistentStorage).put(StorageKeys.CONSENT_ID, CONSENT_ID);
        verify(apiClient).createConsent(USERNAME);
        verify(apiClient).authorizeWithPassword(AUTH_START_PATH, PASSWORD);
        verify(apiClient).authorizeWithOtp(AUTH_PATH, OTP_CODE);
        verify(apiClient).selectAuthMethod(AUTH_PATH, SCA_METHOD_ID_CHIP_TAN);
        verify(apiClient).getConsentDetails(CONSENT_ID);
        verifyNoMoreInteractions(apiClient);

        // and verify supplement interactions
        verify(supplementalInformationController, times(2))
                .askSupplementalInformationSync(fieldCaptor.capture());
        List<Field> allValues = fieldCaptor.getAllValues();
        assertThat(allValues).hasSize(3);
        assertThat(allValues.get(0).getName()).isEqualTo("selectAuthMethodField");
        assertThat(allValues.get(1).getName()).isEqualTo("startcodeField");
        assertThat(allValues.get(1).getValue()).isEqualTo(STARTCODE);
        assertThat(allValues.get(2).getName()).isEqualTo("chipTan");

        assertThat(credentials.getSessionExpiryDate()).isEqualTo(parseIsoDate(CONSENT_VALID_UNTIL));
    }

    @Test
    public void authenticatePaymentShouldCompleteWithChipTanSelected()
            throws SupplementalInfoException {
        // given
        insertDummyCredentials();

        when(apiClient.authorizeWithPassword(AUTH_PATH_PAYMENT, PASSWORD))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.SCA_RESPONSE_MULTIPLE, AuthorizationResponse.class));

        AuthorizationResponse authorizationResponse =
                TestDataReader.readFromFile(
                        TestDataReader.SCA_RESPONSE_SELECTED_CHIP_TAN, AuthorizationResponse.class);

        whenSupplementalInformationControllerReturn(authorizationResponse);
        when(apiClient.selectAuthMethod(AUTH_PATH, SCA_METHOD_ID_CHIP_TAN))
                .thenReturn(authorizationResponse);

        when(apiClient.authorizeWithOtp(AUTH_PATH, OTP_CODE))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.SCA_FINALISED, AuthorizationStatusResponse.class));

        // when
        paymentAuthenticator.authenticatePayment(
                TestDataReader.readFromFile(
                                TestDataReader.PAYMENT_INITIALIZED, CreatePaymentResponse.class)
                        .getLinks());

        // then
        verify(apiClient).authorizeWithPassword(AUTH_PATH_PAYMENT, PASSWORD);
        verify(apiClient).selectAuthMethod(AUTH_PATH, SCA_METHOD_ID_CHIP_TAN);
        verify(apiClient).authorizeWithOtp(AUTH_PATH, OTP_CODE);
        verifyNoMoreInteractions(apiClient);

        // and verify supplement interactions
        verify(supplementalInformationController, times(2))
                .askSupplementalInformationSync(fieldCaptor.capture());
        List<Field> allValues = fieldCaptor.getAllValues();
        assertThat(allValues).hasSize(5);
        assertThat(allValues.get(0).getName()).isEqualTo("selectAuthMethodField");
        assertThat(allValues.get(1).getName()).isEqualTo("TEMPLATE");
        assertThat(allValues.get(1).getValue()).isEqualTo("CARD_READER");
        assertThat(allValues.get(2).getName()).isEqualTo("instruction");
        assertThat(allValues.get(2).getValue()).isEqualTo(STARTCODE);
        assertThat(allValues.get(3).getName()).isEqualTo("chipTan");
        assertThat(allValues.get(4).getName()).isEqualTo("instructionList");
        assertThat(allValues.get(4).getValue())
                .isEqualTo(
                        "[\"Stecken Sie Ihre Chipkarte in den TAN-Generator und drücken \\\"TAN\\\"\",\"Geben Sie den Startcode \\\"555777999666\\\" ein und drücken \\\"OK\\\"\",\"Prüfen Sie die Anzeige auf dem Leserdisplay und drücken \\\"OK\\\"\",\"Bitte geben Sie die auf Ihrem TAN-Generator angezeigte TAN hier ein und bestätigen Sie diese mit \\\"OK\\\"\"]");
    }

    @Test
    public void autoAuthenticateShouldPassIfConsentIsValid()
            throws LoginException, AuthorizationException, SessionException {
        // given
        String consentId = "consentId";
        when(persistentStorage.get(StorageKeys.CONSENT_ID, String.class))
                .thenReturn(Optional.of(consentId));
        when(apiClient.getConsentDetails(CONSENT_ID))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.CONSENT_DETAILS_VALID,
                                ConsentDetailsResponse.class));

        // when
        authenticator.autoAuthenticate();

        // then
        verify(persistentStorage).get(StorageKeys.CONSENT_ID, String.class);
        verify(apiClient).getConsentDetails(consentId);

        assertThat(credentials.getSessionExpiryDate()).isEqualTo(parseIsoDate(CONSENT_VALID_UNTIL));
    }

    @Test
    public void autoAuthenticateShouldThrowExceptionIfThereIsNoConsent() {
        // given
        when(persistentStorage.get(StorageKeys.CONSENT_ID, String.class))
                .thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> authenticator.autoAuthenticate());

        // then
        assertThat(thrown)
                .isInstanceOf(SessionException.class)
                .hasMessage("Cause: SessionError.SESSION_EXPIRED");
        verify(persistentStorage).get(StorageKeys.CONSENT_ID, String.class);

        assertThat(credentials.getSessionExpiryDate()).isNull();
    }

    @Test
    @Parameters({"expired", "received", "revokedByPsu", "!%@^!"})
    public void autoAuthenticateShouldThrowExceptionIfConsentIsInvalid(
            String invalidConsentStatus) {
        // given
        String consentId = "consentId";
        when(persistentStorage.get(StorageKeys.CONSENT_ID, String.class))
                .thenReturn(Optional.of(consentId));
        when(apiClient.getConsentDetails(consentId))
                .thenReturn(consentDetailsResponse(CONSENT_ID, invalidConsentStatus, "2021-11-19"));

        // when
        Throwable thrown = catchThrowable(() -> authenticator.autoAuthenticate());

        // then
        assertThat(thrown)
                .isInstanceOf(SessionException.class)
                .hasMessage("Cause: SessionError.SESSION_EXPIRED");
        verify(persistentStorage).get(StorageKeys.CONSENT_ID, String.class);
        verify(apiClient).getConsentDetails(consentId);

        assertThat(credentials.getSessionExpiryDate()).isNull();
    }

    private void whenSupplementalInformationControllerReturn(AuthorizationResponse scaResponse) {
        Map<String, String> supplementalInformation = new HashMap<>();
        supplementalInformation.put(getFieldName(scaResponse), OTP_CODE);
        supplementalInformation.put("selectAuthMethodField", "2");

        when(supplementalInformationController.askSupplementalInformationSync(any()))
                .thenReturn(supplementalInformation);
    }

    private String getFieldName(AuthorizationResponse scaResponse) {
        ScaMethodEntity chosenScaMethod = scaResponse.getChosenScaMethod();
        if (chosenScaMethod != null) {
            Optional<AuthenticationType> authenticationType =
                    AuthenticationType.fromString(chosenScaMethod.getAuthenticationType());
            if (authenticationType.isPresent()) {
                switch (authenticationType.get()) {
                    case CHIP_OTP:
                        return "chipTan";
                    case SMS_OTP:
                        return "smsTan";
                    case PUSH_OTP:
                        return "pushTan";
                }
            }
        }
        return "tanField";
    }

    @SuppressWarnings("SameParameterValue")
    private ConsentDetailsResponse consentDetailsResponse(
            String consentId, String consentStatus, String validUntil) {
        return new ConsentDetailsResponse(null, consentStatus, consentId, validUntil);
    }

    @SuppressWarnings("SameParameterValue")
    private static Date parseIsoDate(String date) {
        return Date.from(
                LocalDate.parse(date, DateTimeFormatter.ISO_DATE)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant());
    }
}
