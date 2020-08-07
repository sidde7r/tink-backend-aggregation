package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.TargobankApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.TargobankConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.TargobankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.TargobankConstants.ScaStatuses;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.TargobankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.rpc.ChooseScaMethodResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.rpc.CreateAuthorisationResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.rpc.PasswordAuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.rpc.ScaConfirmResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class TargobankSandboxAuthenticator implements Authenticator {

    private final TargobankApiClient apiClient;
    private final PersistentStorage persistentStorage;

    public TargobankSandboxAuthenticator(
            TargobankApiClient apiClient, PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    // Flow: create consent -> create authorization -> password authentication -> choose sca method
    // -> sca authentication
    @Override
    public void authenticate(Credentials credentials) throws AuthorizationException {
        final ConsentResponse consent = apiClient.createConsent();
        CreateAuthorisationResponse createAuthorisationResponse = createAuthorization(consent);

        final String authorisationLink =
                createAuthorisationResponse.getLinks().getUpdatePsuAuthentication().getHref();

        PasswordAuthenticationResponse authenticationResponse =
                apiClient.authenticateAuthorisationsPassword(
                        authorisationLink, credentials.getField(CredentialKeys.PASSWORD));

        ChooseScaMethodResponse scaResponse =
                apiClient.chooseScaMethod(
                        authorisationLink, getFirstScaMethod(authenticationResponse));

        ScaConfirmResponse scaConfirmResponse =
                apiClient.confirmAuthentication(
                        authorisationLink,
                        scaResponse.getChosenScaMethod().getAuthenticationMethodId());

        putConsentInStorage(consent.getConsentId(), scaConfirmResponse.getScaStatus());
    }

    private CreateAuthorisationResponse createAuthorization(ConsentResponse consent) {
        return apiClient.createAuthorisations(
                consent.getLinks().getStartAuthorisationWithPsuAuthentication().getHref());
    }

    // Choosing one of 3 possible SCA method; Sandbox flow is same for all 3
    private String getFirstScaMethod(PasswordAuthenticationResponse authenticationResponse) {
        return authenticationResponse.getScaMethods().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.SCA_NOT_FOUND))
                .getAuthenticationMethodId();
    }

    private void putConsentInStorage(String consentId, String scaStatus)
            throws AuthorizationException {
        if (scaStatus.equals(ScaStatuses.FINALISED))
            persistentStorage.put(StorageKeys.CONSENT_ID, consentId);
        else throw new AuthorizationException(AuthorizationError.UNAUTHORIZED);
    }
}
