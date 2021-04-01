package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;

public class Xs2aDevelopersRedirectAuthenticator extends ThirdPartyAppAuthenticationController {

    private final OAuth2AuthenticationController oAuth2AuthenticationController;
    private final Xs2aDevelopersAuthenticatorHelper xs2ADevelopersAuthenticatorHelper;

    public Xs2aDevelopersRedirectAuthenticator(
            OAuth2AuthenticationController authenticator,
            SupplementalInformationHelper supplementalInformationHelper,
            Xs2aDevelopersAuthenticatorHelper xs2ADevelopersAuthenticatorHelper) {
        super(authenticator, supplementalInformationHelper);
        this.oAuth2AuthenticationController = authenticator;
        this.xs2ADevelopersAuthenticatorHelper = xs2ADevelopersAuthenticatorHelper;
    }

    public void autoAuthenticate() throws SessionException, BankServiceException {
        if (xs2ADevelopersAuthenticatorHelper.getConsentStatus().isValid()) {
            oAuth2AuthenticationController.autoAuthenticate();
        } else {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
