package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoConstants.QueryParamKeys.AUTHORIZATION_CODE;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoConstants.Urls.NORDEA_REDIRECT_BACK_TO_MOBILE_APP_URL;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoStorage;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.rpc.OauthTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.client.AuthenticationClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeAuthenticationResult;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@Slf4j
@RequiredArgsConstructor
public class NordeaNoAuthenticator implements BankIdIframeAuthenticator {

    private final AuthenticationClient authenticationClient;
    private final NordeaNoStorage storage;

    @Override
    public String getSubstringOfUrlIndicatingAuthenticationFinish() {
        return NORDEA_REDIRECT_BACK_TO_MOBILE_APP_URL;
    }

    @Override
    public void handleBankIdAuthenticationResult(
            BankIdIframeAuthenticationResult authenticationResult) {
        String redirectUrl =
                authenticationResult
                        .getProxyResponseFromAuthFinishUrl()
                        .getResponse()
                        .headers()
                        .get("Location");

        if (redirectUrl == null) {
            throw new IllegalStateException("Cannot find Location header in proxy response");
        }

        String authorizationCode = extractAuthorizationCode(redirectUrl);
        finishOAuth2Authentication(authorizationCode);
    }

    private String extractAuthorizationCode(String redirectUrl) {
        List<NameValuePair> params =
                URLEncodedUtils.parse(URI.create(redirectUrl), StandardCharsets.UTF_8);
        return params.stream()
                .filter(x -> AUTHORIZATION_CODE.equalsIgnoreCase(x.getName()))
                .findFirst()
                .map(NameValuePair::getValue)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Cannot extract authorization code from proxy response"));
    }

    private void finishOAuth2Authentication(String authorizationCode) {
        String codeVerifier = storage.retrieveCodeVerifier();

        OauthTokenResponse oauthTokenResponse =
                authenticationClient.getOathToken(authorizationCode, codeVerifier);
        OAuth2Token oauthToken =
                oauthTokenResponse
                        .toOauthToken()
                        .orElseThrow(LoginError.CREDENTIALS_VERIFICATION_ERROR::exception);
        storage.storeOauthToken(oauthToken);
    }
}
