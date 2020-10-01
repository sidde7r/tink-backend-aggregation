package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.authenticator;

import com.nimbusds.jwt.JWTParser;
import java.text.ParseException;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.BoursoramaConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client.BoursoramaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.configuration.BoursoramaConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@AllArgsConstructor
public class BoursoramaAuthenticator implements OAuth2Authenticator {

    private static final String USER_HASH_CLAIM = "userHash";

    private final BoursoramaApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final AgentConfiguration<BoursoramaConfiguration> configuration;

    @Override
    @SneakyThrows
    public URL buildAuthorizeUrl(String state) {
        return new URL("https://clients.boursorama.com/oauth/v2/authorisation/{tppId}")
                .parameter(
                        "tppId",
                        CertificateUtils.getOrganizationIdentifier(configuration.getQwac()))
                .queryParam("state", state)
                .queryParam("successRedirect", configuration.getRedirectUrl())
                .queryParam("errorRedirect", configuration.getRedirectUrl());
    }

    @Override
    @SneakyThrows
    public OAuth2Token exchangeAuthorizationCode(String code) {
        TokenResponse tokenResponse =
                apiClient.exchangeAuthorizationCode(
                        new TokenRequest(
                                "authorization_code",
                                code,
                                CertificateUtils.getOrganizationIdentifier(
                                        configuration.getQwac())));

        return OAuth2Token.create(
                tokenResponse.getTokenType(),
                tokenResponse.getAccessToken(),
                tokenResponse.getRefreshToken(),
                tokenResponse.getExpiresIn());
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) {
        TokenResponse tokenResponse = apiClient.refreshToken(new RefreshTokenRequest(refreshToken));

        return OAuth2Token.createBearer(
                tokenResponse.getAccessToken(),
                tokenResponse.getRefreshToken(),
                tokenResponse.getExpiresIn());
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        sessionStorage.put(BoursoramaConstants.USER_HASH, extractUserHash(accessToken));
        sessionStorage.put(BoursoramaConstants.OAUTH_TOKEN, accessToken);
    }

    private String extractUserHash(OAuth2Token accessToken) {
        try {
            return (String)
                    JWTParser.parse(accessToken.getAccessToken())
                            .getJWTClaimsSet()
                            .getClaim(USER_HASH_CLAIM);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
