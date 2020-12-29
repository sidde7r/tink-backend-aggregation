package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationProgressiveController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class RabobankAuthenticationController extends OAuth2AuthenticationProgressiveController {

    private final RabobankAuthenticator authenticator;

    public RabobankAuthenticationController(
            PersistentStorage persistentStorage,
            RabobankAuthenticator authenticator,
            Credentials credentials,
            StrongAuthenticationState strongAuthenticationState) {
        super(persistentStorage, authenticator, credentials, strongAuthenticationState);
        this.authenticator = authenticator;
    }

    @Override
    public void autoAuthenticate() {
        super.autoAuthenticate();
        authenticator.checkConsentStatus();
    }
}
