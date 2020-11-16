package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.libraries.i18n.LocalizableKey;

public class Xs2aDevelopersOAuth2AuthenticatorController
        implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {

    private final OAuth2AuthenticationController oAuth2AuthenticationController;
    private final Xs2aDevelopersAuthenticator xs2aDevelopersAuthenticator;

    public Xs2aDevelopersOAuth2AuthenticatorController(
            OAuth2AuthenticationController oAuth2AuthenticationController,
            Xs2aDevelopersAuthenticator xs2aDevelopersAuthenticator) {
        this.oAuth2AuthenticationController = oAuth2AuthenticationController;
        this.xs2aDevelopersAuthenticator = xs2aDevelopersAuthenticator;
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        if (xs2aDevelopersAuthenticator.isPersistedConsentValid()) {
            oAuth2AuthenticationController.autoAuthenticate();
            xs2aDevelopersAuthenticator.storeConsentDetails();
        } else {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    @Override
    public ThirdPartyAppResponse<String> init() {
        return oAuth2AuthenticationController.init();
    }

    @Override
    public ThirdPartyAppAuthenticationPayload getAppPayload() {
        return oAuth2AuthenticationController.getAppPayload();
    }

    @Override
    public ThirdPartyAppResponse<String> collect(String reference) {
        ThirdPartyAppResponse<String> response = oAuth2AuthenticationController.collect(reference);
        xs2aDevelopersAuthenticator.storeConsentDetails();
        return response;
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status) {
        return oAuth2AuthenticationController.getUserErrorMessageFor(status);
    }
}
