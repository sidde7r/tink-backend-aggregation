package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;

public class Xs2aDevelopersOAuth2AuthenticatorController
        extends ThirdPartyAppAuthenticationController {

    private final OAuth2AuthenticationController oAuth2AuthenticationController;
    private final Xs2aDevelopersAuthenticator xs2ADevelopersAuthenticator;

    public Xs2aDevelopersOAuth2AuthenticatorController(
            OAuth2AuthenticationController authenticator,
            SupplementalInformationHelper supplementalInformationHelper,
            Xs2aDevelopersAuthenticator xs2ADevelopersAuthenticator) {
        super(authenticator, supplementalInformationHelper);
        this.oAuth2AuthenticationController = authenticator;
        this.xs2ADevelopersAuthenticator = xs2ADevelopersAuthenticator;
    }

    public void autoAuthenticate() throws SessionException, BankServiceException {
        if (xs2ADevelopersAuthenticator.getConsentStatus().isValid()) {
            oAuth2AuthenticationController.autoAuthenticate();
        } else {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
