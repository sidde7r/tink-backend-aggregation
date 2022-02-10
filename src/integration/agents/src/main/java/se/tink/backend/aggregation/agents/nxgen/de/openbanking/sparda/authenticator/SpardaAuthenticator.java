package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.authenticator;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.SpardaStorage;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.client.SpardaAuthApiClient;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

@RequiredArgsConstructor
public class SpardaAuthenticator implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {

    private final SpardaAuthApiClient apiClient;
    private final SpardaStorage storage;

    private final OAuth2AuthenticationController oAuth2AuthenticationController;

    private final Credentials credentials;

    @Override
    public void autoAuthenticate() {
        oAuth2AuthenticationController.autoAuthenticate();

        String consentId = storage.getConsentId();
        if (consentId == null || !apiClient.fetchConsentDetails(consentId).isValid()) {
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
        if (response.getStatus() == ThirdPartyAppStatus.DONE) {
            return ThirdPartyAppResponseImpl.create(checkResultingConsent());
        }
        return response;
    }

    private ThirdPartyAppStatus checkResultingConsent() {
        ConsentDetailsResponse consentDetails =
                apiClient.fetchConsentDetails(storage.getConsentId());
        if (consentDetails.isValid()) {
            credentials.setSessionExpiryDate(consentDetails.getValidUntil());
            return ThirdPartyAppStatus.DONE;
        } else {
            return ThirdPartyAppStatus.AUTHENTICATION_ERROR;
        }
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status) {
        return Optional.empty();
    }
}
