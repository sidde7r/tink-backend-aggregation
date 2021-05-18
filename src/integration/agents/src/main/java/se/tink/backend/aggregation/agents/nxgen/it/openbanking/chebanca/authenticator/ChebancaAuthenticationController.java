package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;

public class ChebancaAuthenticationController extends ThirdPartyAppAuthenticationController
        implements AutoAuthenticator {
    private final ChebancaConsentManager consentManager;
    private final OAuth2AuthenticationController oAuth2AuthenticationController;
    private final ChebancaBgAutoAuthenticator backgroundAutoAuthenticator;
    private final boolean isUserPresent;

    public ChebancaAuthenticationController(
            OAuth2AuthenticationController oAuth2AuthenticationController,
            ChebancaConsentManager consentManager,
            SupplementalInformationHelper supplementalInformationHelper,
            ChebancaBgAutoAuthenticator bgAutoAuthenticator,
            boolean isUserPresent) {
        super(oAuth2AuthenticationController, supplementalInformationHelper);
        this.oAuth2AuthenticationController = oAuth2AuthenticationController;
        this.consentManager = consentManager;
        this.isUserPresent = isUserPresent;
        this.backgroundAutoAuthenticator = bgAutoAuthenticator;
    }

    @Override
    public void authenticate(Credentials credentials) {
        super.authenticate(credentials);
        consentManager.processConsent();
    }

    @Override
    public void autoAuthenticate() {
        if (!consentManager.isConsentValid()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        if (isUserPresent) {
            oAuth2AuthenticationController.autoAuthenticate();
        } else {
            backgroundAutoAuthenticator.autoAuthenticate();
        }
    }
}
