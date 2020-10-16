package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.rpc.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.rpc.ScaResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.rpc.ScaStatusResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class FiduciaAuthenticatorTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/fiducia/resources";

    private static final String USERNAME = "psuId";
    private static final String PASSWORD = "password";
    private static final String CONSENT_ID = "consentId";
    private static final String OTP_CODE = "123456";
    private static final String SCA_METHOD_ID_CHIP_TAN = "962";
    private static final String AUTH_PATH =
            "/v1/consents/dummy_consent_id/authorisations/dummy_authorization_id";

    private FiduciaAuthenticator authenticator;
    private FiduciaApiClient apiClient;
    private PersistentStorage persistentStorage;
    private SessionStorage sessionStorage;

    @Before
    public void setup() {
        apiClient = mock(FiduciaApiClient.class);
        persistentStorage = mock(PersistentStorage.class);
        sessionStorage = mock(SessionStorage.class);
        SupplementalInformationHelper supplementalInformationHelper =
                mock(SupplementalInformationHelper.class);
        Catalog catalog = mock(Catalog.class);

        authenticator =
                new FiduciaAuthenticator(
                        apiClient,
                        persistentStorage,
                        sessionStorage,
                        supplementalInformationHelper,
                        catalog);

        when(catalog.getString(any(LocalizableKey.class))).thenReturn("");

        Map<String, String> suppData = new HashMap<>();
        suppData.put("tanField", OTP_CODE);
        suppData.put("selectAuthMethodField", "2");
        when(supplementalInformationHelper.askSupplementalInformation(any())).thenReturn(suppData);
    }

    @Test
    public void authenticateShouldInvokeApiClientAndSaveDataInStorage()
            throws SupplementalInfoException {
        // given
        Credentials credentials = new Credentials();
        credentials.setFields(
                ImmutableMap.of(
                        CredentialKeys.PSU_ID, USERNAME, CredentialKeys.PASSWORD, PASSWORD));

        when(apiClient.createConsent()).thenReturn(CONSENT_ID);
        when(apiClient.authorizeConsent(CONSENT_ID, PASSWORD))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "scaResponseSelected.json").toFile(),
                                ScaResponse.class));
        when(apiClient.authorizeWithOtpCode(AUTH_PATH, OTP_CODE))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "scaFinalised.json").toFile(),
                                ScaStatusResponse.class));

        // when
        authenticator.authenticate(credentials);

        // then
        verify(sessionStorage).put(CredentialKeys.PSU_ID, USERNAME);
        verify(persistentStorage).put(StorageKeys.CONSENT_ID, CONSENT_ID);
        verify(apiClient).createConsent();
        verify(apiClient).authorizeConsent(CONSENT_ID, PASSWORD);
        verify(apiClient).authorizeWithOtpCode(AUTH_PATH, OTP_CODE);
        verifyNoMoreInteractions(apiClient);
    }

    @Test
    public void authenticateShouldInvokeApiClientAndSaveDataInStorageWithChipTanSelected()
            throws SupplementalInfoException {
        // given
        Credentials credentials = new Credentials();
        credentials.setFields(
                ImmutableMap.of(
                        CredentialKeys.PSU_ID, USERNAME, CredentialKeys.PASSWORD, PASSWORD));

        when(apiClient.createConsent()).thenReturn(CONSENT_ID);
        when(apiClient.authorizeConsent(CONSENT_ID, PASSWORD))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "scaResponseMultiple.json").toFile(),
                                ScaResponse.class));
        when(apiClient.selectAuthMethod(AUTH_PATH, SCA_METHOD_ID_CHIP_TAN))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "scaResponseSelectedChipTan.json")
                                        .toFile(),
                                ScaResponse.class));
        when(apiClient.authorizeWithOtpCode(AUTH_PATH, OTP_CODE))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "scaFinalised.json").toFile(),
                                ScaStatusResponse.class));

        // when
        authenticator.authenticate(credentials);

        // then
        verify(sessionStorage).put(CredentialKeys.PSU_ID, USERNAME);
        verify(persistentStorage).put(StorageKeys.CONSENT_ID, CONSENT_ID);
        verify(apiClient).createConsent();
        verify(apiClient).authorizeConsent(CONSENT_ID, PASSWORD);
        verify(apiClient).authorizeWithOtpCode(AUTH_PATH, OTP_CODE);
        verify(apiClient).selectAuthMethod(AUTH_PATH, SCA_METHOD_ID_CHIP_TAN);
        verifyNoMoreInteractions(apiClient);
    }

    @Test
    public void autoAuthenticateShouldPassIfConsentIsValid()
            throws LoginException, AuthorizationException, SessionException {
        // given
        String consentId = "consentId";
        when(persistentStorage.get(StorageKeys.CONSENT_ID, String.class))
                .thenReturn(Optional.of(consentId));
        when(apiClient.getConsentStatus(consentId)).thenReturn(ConsentStatus.VALID);

        // when
        authenticator.autoAuthenticate();

        // then
        verify(persistentStorage).get(StorageKeys.CONSENT_ID, String.class);
        verify(apiClient).getConsentStatus(consentId);
    }

    @Test
    public void autoAuthenticateShouldThrowExceptionIfThereIsNoConsent() {
        // given
        when(persistentStorage.get(StorageKeys.CONSENT_ID, String.class))
                .thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> authenticator.autoAuthenticate());

        // then
        Assertions.assertThat(thrown)
                .isInstanceOf(SessionException.class)
                .hasMessage("Cause: SessionError.SESSION_EXPIRED");
        verify(persistentStorage).get(StorageKeys.CONSENT_ID, String.class);
    }

    @Test
    public void autoAuthenticateShouldThrowExceptionIfConsentIsInvalid() {
        // given
        String consentId = "consentId";
        when(persistentStorage.get(StorageKeys.CONSENT_ID, String.class))
                .thenReturn(Optional.of(consentId));
        when(apiClient.getConsentStatus(consentId)).thenReturn(ConsentStatus.EXPIRED);

        // when
        Throwable thrown = catchThrowable(() -> authenticator.autoAuthenticate());

        // then
        Assertions.assertThat(thrown)
                .isInstanceOf(SessionException.class)
                .hasMessage("Cause: SessionError.SESSION_EXPIRED");
        verify(persistentStorage).get(StorageKeys.CONSENT_ID, String.class);
        verify(apiClient).getConsentStatus(consentId);
    }
}
