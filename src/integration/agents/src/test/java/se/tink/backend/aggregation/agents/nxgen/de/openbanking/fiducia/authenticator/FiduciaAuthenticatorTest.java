package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
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
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.rpc.AuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.rpc.ConsentStatus;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class FiduciaAuthenticatorTest {

    private FiduciaAuthenticator authenticator;
    private FiduciaApiClient apiClient;
    private PersistentStorage persistentStorage;
    private SessionStorage sessionStorage;
    private SupplementalInformationHelper supplementalInformationHelper;

    @Before
    public void setup() {
        apiClient = mock(FiduciaApiClient.class);
        persistentStorage = mock(PersistentStorage.class);
        sessionStorage = mock(SessionStorage.class);
        supplementalInformationHelper = mock(SupplementalInformationHelper.class);

        authenticator =
                new FiduciaAuthenticator(
                        apiClient,
                        persistentStorage,
                        sessionStorage,
                        supplementalInformationHelper);
    }

    @Test
    public void authenticateShouldInvokeApiClientAndSaveDataInStorage()
            throws SupplementalInfoException {
        // given
        String consentId = "consentId";
        String otpCode = "123456";
        AuthorizationResponse authorizationResponse =
                new AuthorizationResponse(Collections.singletonList("1"));
        when(apiClient.createConsent()).thenReturn(consentId);
        when(apiClient.getAuthorizationId(consentId)).thenReturn(authorizationResponse);
        when(supplementalInformationHelper.waitForOtpInput()).thenReturn(otpCode);

        Credentials credentials = new Credentials();
        String psuId = "psuId";
        String password = "password";
        credentials.setFields(
                ImmutableMap.of(CredentialKeys.PSU_ID, psuId, CredentialKeys.PASSWORD, password));

        // when
        authenticator.authenticate(credentials);

        // then
        verify(sessionStorage, times(1)).put(CredentialKeys.PSU_ID, psuId);
        verify(persistentStorage, times(1)).put(StorageKeys.CONSENT_ID, consentId);
        verify(apiClient, times(1)).createConsent();
        verify(apiClient, times(1)).authorizeConsent(consentId, password);
        verify(apiClient, times(1)).getAuthorizationId(consentId);
        verify(apiClient, times(1)).authorizeWithOtpCode(consentId, authorizationResponse, otpCode);
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
        verify(persistentStorage, times(1)).get(StorageKeys.CONSENT_ID, String.class);
        verify(apiClient, times(1)).getConsentStatus(consentId);
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
        verify(persistentStorage, times(1)).get(StorageKeys.CONSENT_ID, String.class);
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
        verify(persistentStorage, times(1)).get(StorageKeys.CONSENT_ID, String.class);
        verify(apiClient, times(1)).getConsentStatus(consentId);
    }
}
