package se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.authenticator;

import java.util.Optional;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.authenticator.rpc.ConsentDetailsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.libraries.i18n.LocalizableKey;

@AllArgsConstructor
public class NorwegianOAuth2AuthenticatorController
        implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {

    private final OAuth2AuthenticationController oAuth2AuthenticationController;
    private final NorwegianAuthenticator authenticator;

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        oAuth2AuthenticationController.autoAuthenticate();

        ConsentDetailsResponse consentDetails = authenticator.getPersistedConsentDetails();
        if (consentDetails == null) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        if (consentDetails.isExpired()) {
            throw SessionError.CONSENT_EXPIRED.exception();
        }
        if (consentDetails.isRevokedByPsu()) {
            throw SessionError.CONSENT_REVOKED_BY_USER.exception();
        }
        if (!consentDetails.isValid()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        authenticator.storeConsentValidUntil(consentDetails.getValidUntil().toLocalDate());
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
        authenticator.storeConsentValidUntil(
                authenticator.getPersistedConsentDetails().getValidUntil().toLocalDate());
        return response;
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status) {
        return oAuth2AuthenticationController.getUserErrorMessageFor(status);
    }
}
