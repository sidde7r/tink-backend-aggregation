package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class AbnAmroOAuth2AuthenticationController extends OAuth2AuthenticationController {

    private final AbnAmroAuthenticator authenticator;

    public AbnAmroOAuth2AuthenticationController(
            PersistentStorage persistentStorage,
            SupplementalInformationHelper supplementalInformationHelper,
            AbnAmroAuthenticator authenticator,
            Credentials credentials,
            StrongAuthenticationState strongAuthenticationState) {
        super(
                persistentStorage,
                supplementalInformationHelper,
                authenticator,
                credentials,
                strongAuthenticationState);
        this.authenticator = authenticator;
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        super.autoAuthenticate();
        authenticator.checkIfConsentValidOrThrowException();
    }
}
