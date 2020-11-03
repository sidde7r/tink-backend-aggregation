package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator;

import lombok.AllArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

@AllArgsConstructor
public class DemobankMultiRedirectAuthenticator implements TypedAuthenticator {

    private final DemobankApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final String callbackUri;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final CredentialsRequest request;
    private final StrongAuthenticationState strongAuthenticationState;

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {

        constructRedirectAuthenticator("read").authenticate(credentials);
        // Check for multi redirect
        if (apiClient.fetchUser().isMultiRedirect()) {
            constructRedirectAuthenticator("accounts:read").authenticate(credentials);
        }
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.THIRD_PARTY_APP;
    }

    private Authenticator constructRedirectAuthenticator(String scopes) {

        DemobankRedirectAuthenticator demobankRedirectAuthenticator =
                new DemobankRedirectAuthenticator(apiClient, callbackUri, scopes);

        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        demobankRedirectAuthenticator,
                        request.getCredentials(),
                        strongAuthenticationState);

        return new ThirdPartyAppAuthenticationController<>(
                controller, supplementalInformationHelper);
    }
}
