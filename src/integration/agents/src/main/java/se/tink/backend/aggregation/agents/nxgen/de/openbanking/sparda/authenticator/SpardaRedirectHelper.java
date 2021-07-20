package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.SpardaConstants.SANDBOX_CODE_CHALLENGE;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.SpardaConstants;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.SpardaStorage;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.client.SpardaAuthApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.client.SpardaTokenApiClient;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AccessEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
@Slf4j
public class SpardaRedirectHelper implements OAuth2Authenticator {

    private static final String NOT_OK_KEY = "nok";
    private static final String NOT_OK_VALUE = "true";
    private static final String STATE = "state";

    private final SpardaStorage storage;
    private final SpardaAuthApiClient authApiClient;
    private final SpardaTokenApiClient tokenApiClient;
    private final String clientId;
    private final String redirectUrl;
    private final StrongAuthenticationState strongAuthenticationState;
    private final LocalDateTimeSource localDateTimeSource;
    private final RandomValueGenerator randomValueGenerator;

    @Override
    public URL buildAuthorizeUrl(String state) {
        ConsentRequest consentRequest = buildConsentRequest();
        URL redirectUrlWithState = buildRedirectWithState();
        URL redirectUrlNotOk = redirectUrlWithState.queryParam(NOT_OK_KEY, NOT_OK_VALUE);

        ConsentResponse consent =
                authApiClient.createConsent(consentRequest, redirectUrlWithState, redirectUrlNotOk);
        storage.saveConsentId(consent.getConsentId());

        String codeVerifier = generateCodeVerifier();
        storage.saveCodeVerifier(codeVerifier);

        String codeChallenge = Psd2Headers.generateCodeChallenge(codeVerifier);
        return urlWithCodeChallenge(consent.getLinks().getScaRedirect(), codeChallenge);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) {
        return tokenApiClient
                .exchangeCodeForToken(
                        code,
                        buildRedirectWithState().toString(),
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

    private URL buildRedirectWithState() {
        return new URL(redirectUrl).queryParam(STATE, strongAuthenticationState.getState());
    }

    private ConsentRequest buildConsentRequest() {
        AccessEntity accessEntity =
                AccessEntity.builder().allPsd2(AccessEntity.ALL_ACCOUNTS).build();
        return ConsentRequest.buildTypicalRecurring(
                accessEntity, localDateTimeSource.now().toLocalDate().plusDays(90).toString());
    }

    private String generateCodeVerifier() {
        return randomValueGenerator.generateRandomAlphanumeric(60);
    }

    private URL urlWithCodeChallenge(String url, String codeChallenge) {
        return new URL(
                url.replace(
                        SpardaConstants.SANDBOX ? SANDBOX_CODE_CHALLENGE : "{code_challenge}",
                        codeChallenge));
    }
}
