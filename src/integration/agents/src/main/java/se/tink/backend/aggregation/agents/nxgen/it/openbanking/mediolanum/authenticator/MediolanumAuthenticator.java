package se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.authenticator;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.MediolanumApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.MediolanumStorage;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.i18n.LocalizableKey;

@RequiredArgsConstructor
public class MediolanumAuthenticator
        implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {

    private final OAuth2AuthenticationController oAuth2AuthenticationController;
    private final MediolanumApiClient apiClient;
    private final MediolanumStorage storage;
    private final Credentials credentials;

    private final StrongAuthenticationState strongAuthenticationState;
    private final SupplementalInformationHelper supplementalInformationHelper;

    @Override
    public void autoAuthenticate() {
        oAuth2AuthenticationController.autoAuthenticate();

        ConsentDetailsResponse consent = apiClient.fetchConsentDetails(storage.getConsentId());
        if (consent.isExpired()) {
            throw SessionError.CONSENT_EXPIRED.exception();
        }
        if (consent.isRevokedByPsu()) {
            throw SessionError.CONSENT_REVOKED_BY_USER.exception();
        }
        if (!consent.isValid()) {
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
    public Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status) {
        return Optional.empty();
    }

    @Override
    public ThirdPartyAppResponse<String> collect(String reference) {
        ThirdPartyAppResponse<String> response = oAuth2AuthenticationController.collect(reference);
        if (response.getStatus() == ThirdPartyAppStatus.DONE) {
            createAndAuthorizeConsent();
        }
        return response;
    }

    private void createAndAuthorizeConsent() {
        ConsentResponse consent = apiClient.createConsent(strongAuthenticationState.getState());
        openAndWaitForCallback(consent);
        validateConsentAndStoreDetails(consent);
    }

    private void openAndWaitForCallback(ConsentResponse consent) {
        ThirdPartyAppAuthenticationPayload payload =
                ThirdPartyAppAuthenticationPayload.of(
                        new URL(consent.getLinks().getScaRedirect().getHref()));
        supplementalInformationHelper.openThirdPartyApp(payload);

        Map<String, String> callbackData =
                supplementalInformationHelper
                        .waitForSupplementalInformation(
                                strongAuthenticationState.getSupplementalKey(), 9, TimeUnit.MINUTES)
                        .orElseThrow(ThirdPartyAppError.TIMED_OUT::exception);

        if (callbackData.containsKey("nok")) {
            throw LoginError.DEFAULT_MESSAGE.exception(
                    "Callback received on NOK endpoint, aborting.");
        }
    }

    private void validateConsentAndStoreDetails(ConsentResponse consent) {
        ConsentDetailsResponse consentDetailsResponse =
                apiClient.fetchConsentDetails(consent.getConsentId());

        if (!consentDetailsResponse.isValid()) {
            throw LoginError.DEFAULT_MESSAGE.exception("Consent not valid after callback.");
        }

        storage.saveConsentId(consent.getConsentId());
        credentials.setSessionExpiryDate(
                consentDetailsResponse.getValidUntil(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
}
