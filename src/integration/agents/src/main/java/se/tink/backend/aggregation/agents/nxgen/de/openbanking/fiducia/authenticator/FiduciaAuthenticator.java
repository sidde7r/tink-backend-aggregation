package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.rpc.AuthorizationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class FiduciaAuthenticator implements MultiFactorAuthenticator, AutoAuthenticator {

    private final FiduciaApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;
    private final SupplementalInformationHelper supplementalInformationHelper;

    public FiduciaAuthenticator(
            FiduciaApiClient apiClient,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage,
            SupplementalInformationHelper supplementalInformationHelper) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    @Override
    public void authenticate(Credentials credentials) throws SupplementalInfoException {
        String psuId = credentials.getField(CredentialKeys.PSU_ID);
        sessionStorage.put(StorageKeys.PSU_ID, psuId);

        String consentId = apiClient.createConsent();

        String password = credentials.getField(CredentialKeys.PASSWORD);
        apiClient.authorizeConsent(consentId, password);

        AuthorizationResponse authorizationResponse = apiClient.getAuthorizationId(consentId);

        String otpCode = supplementalInformationHelper.waitForOtpInput();

        apiClient.authorizeWithOtpCode(consentId, authorizationResponse, otpCode);

        persistentStorage.put(StorageKeys.CONSENT_ID, consentId);
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }

    @Override
    public void autoAuthenticate() throws SessionException, LoginException, AuthorizationException {
        String consentId =
                persistentStorage
                        .get(StorageKeys.CONSENT_ID, String.class)
                        .orElseThrow(SessionError.SESSION_EXPIRED::exception);

        if (!apiClient.getConsentStatus(consentId).isAcceptedStatus()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
