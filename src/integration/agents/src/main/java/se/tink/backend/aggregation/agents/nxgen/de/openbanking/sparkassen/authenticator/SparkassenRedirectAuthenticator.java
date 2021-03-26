package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator;

import com.google.common.base.Strings;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenStorage;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.libraries.i18n.LocalizableKey;

@RequiredArgsConstructor
public class SparkassenRedirectAuthenticator
        implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {

    private final OAuth2AuthenticationController oAuth2AuthenticationController;
    private final SparkassenApiClient apiClient;
    private final SparkassenStorage storage;
    private final Credentials credentials;

    @Override
    public void autoAuthenticate() {

        // Provide backwards compatibility for credentials created in embedded fashion.
        if (storage.getToken().isPresent()) {
            oAuth2AuthenticationController.autoAuthenticate();
        }

        String consentId = storage.getConsentId();
        if (Strings.isNullOrEmpty(consentId)) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        ConsentDetailsResponse consentDetails = apiClient.getConsentDetails(consentId);
        if (consentDetails.isExpired()) {
            throw SessionError.CONSENT_EXPIRED.exception();
        }
        if (consentDetails.isRevokedByPsu()) {
            throw SessionError.CONSENT_REVOKED_BY_USER.exception();
        }
        if (!consentDetails.isValid()) {
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
        ConsentDetailsResponse consentDetails = apiClient.getConsentDetails(storage.getConsentId());
        credentials.setSessionExpiryDate(consentDetails.getValidUntil());
        return response;
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status) {
        return Optional.empty();
    }
}
