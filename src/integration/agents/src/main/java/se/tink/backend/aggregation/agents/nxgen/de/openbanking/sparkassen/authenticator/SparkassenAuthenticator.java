package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.PathVariables;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.FinalizeAuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.InitAuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.ScaStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.SelectAuthenticationMethodResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class SparkassenAuthenticator implements AutoAuthenticator {

    private final SparkassenApiClient apiClient;
    private final PersistentStorage persistentStorage;

    public SparkassenAuthenticator(
            SparkassenApiClient apiClient, PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    public InitAuthorizationResponse init(String username, String password)
            throws AuthenticationException, AuthorizationException {

        ConsentResponse consentResponse = apiClient.createConsent();
        persistentStorage.put(
                SparkassenConstants.StorageKeys.CONSENT_ID, consentResponse.getConsentId());

        // TODO Response contains a list of available authentication methods
        //      This would allows a user with multiple otp devices/apps to chose which one to use
        //      Method description contains a description, set at time of device/app registration
        InitAuthorizationResponse initAuthorizationResponse =
                apiClient.initializeAuthorization(
                        consentResponse.getLinks().getStartAuthorisationWithPsuAuthenticationUrl(),
                        username,
                        password);
        persistentStorage.put(
                SparkassenConstants.StorageKeys.AUTHORIZATION_ID,
                initAuthorizationResponse.getAuthorisationId());

        return initAuthorizationResponse;
    }

    public SelectAuthenticationMethodResponse selectScaMethod(String methodId) {
        return apiClient.updateAuthorisationForScaMethod(methodId);
    }

    public FinalizeAuthorizationResponse authenticateWithOtp(String otp)
            throws AuthenticationException, AuthorizationException {

        return apiClient.finalizeAuthorization(
                Urls.FINALIZE_AUTHORIZATION
                        .parameter(
                                PathVariables.CONSENT_ID,
                                persistentStorage.get(SparkassenConstants.StorageKeys.CONSENT_ID))
                        .parameter(
                                PathVariables.AUTHORIZATION_ID,
                                persistentStorage.get(
                                        SparkassenConstants.StorageKeys.AUTHORIZATION_ID)),
                otp);
    }

    public ScaStatusResponse checkStatus(String url) {
        return apiClient.getAuthorisation(new URL(url));
    }

    @Override
    public void autoAuthenticate()
            throws SessionException, BankServiceException, AuthorizationException {
        throw SessionError.SESSION_EXPIRED.exception();
    }
}
