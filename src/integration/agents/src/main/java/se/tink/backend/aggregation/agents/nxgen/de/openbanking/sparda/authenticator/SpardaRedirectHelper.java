package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.SpardaConstants.NOT_OK_KEY;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.SpardaConstants.NOT_OK_VALUE;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.SpardaStorage;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.client.SpardaAuthApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.client.SpardaTokenApiClient;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessType;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
@Slf4j
public class SpardaRedirectHelper implements OAuth2Authenticator {

    private final SpardaStorage storage;
    private final SpardaAuthApiClient authApiClient;
    private final SpardaTokenApiClient tokenApiClient;
    private final String clientId;
    private final String redirectUrl;
    private final LocalDateTimeSource localDateTimeSource;
    private final SpardaRedirectUrlBuilder redirectUrlBuilder;

    @Override
    public URL buildAuthorizeUrl(String state) {
        ConsentRequest consentRequest = buildConsentRequest();
        URL redirectUrlWithState = redirectUrlBuilder.buildRedirectWithState(redirectUrl);
        URL redirectUrlNotOk = redirectUrlBuilder.buildRedirectUrlNotOk(redirectUrl);

        ConsentResponse consent =
                authApiClient.createConsent(consentRequest, redirectUrlWithState, redirectUrlNotOk);
        storage.saveConsentId(consent.getConsentId());

        return redirectUrlBuilder.buildUrlWithCodeChallenge(consent.getLinks().getScaRedirect());
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) {
        return tokenApiClient
                .exchangeCodeForToken(
                        code,
                        redirectUrlBuilder.buildRedirectWithState(redirectUrl).toString(),
                        clientId,
                        storage.getCodeVerifier())
                .toTinkToken();
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) {
        return tokenApiClient.refreshToken(refreshToken, clientId).toTinkToken();
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        storage.saveToken(accessToken);
    }

    @Override
    public void handleSpecificCallbackDataError(Map<String, String> callbackData) {
        if (NOT_OK_VALUE.equalsIgnoreCase(callbackData.get(NOT_OK_KEY))) {
            throw ThirdPartyAppError.CANCELLED.exception("Not-ok callback received!");
        }

        log.info("Received callback with unhandled error? Keys: {}", callbackData.keySet());
    }

    private ConsentRequest buildConsentRequest() {
        AccessEntity accessEntity = AccessEntity.builder().allPsd2(AccessType.ALL_ACCOUNTS).build();
        return ConsentRequest.buildTypicalRecurring(accessEntity, localDateTimeSource);
    }
}
