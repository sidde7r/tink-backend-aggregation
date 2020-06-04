package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.authenticator;

import com.nimbusds.jwt.JWTParser;
import java.text.ParseException;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.BoursoramaConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client.BoursoramaApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@AllArgsConstructor
public class BoursoramaAuthenticator implements OAuth2Authenticator {

    private static final String USER_HASH_CLAIM = "userHash";

    private final BoursoramaApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final String redirectUrl;

    @Override
    public URL buildAuthorizeUrl(String state) {
        //                return new URL("https://clients.boursorama.com/oauth/v2/authorisation/" +
        //         configuration.getClientId())
        //                        .queryParam(
        //                                "successRedirect",
        //                                redirectUrl + "?state=" + state)
        //                        .queryParam(
        //                                "errorRedirect",
        //                                redirectUrl  + "?state=" + state);
        return new URL("https://127.0.0.1:7357/api/v1/thirdparty/callback?code=123&state=" + state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) {
        //                TokenResponse tokenResponse =
        //                        apiClient.exchangeAuthorizationCode(
        //                                new TokenRequest("tokenResponse", code,
        // "PSDSE-FINA-44059"));
        //
        //                return OAuth2Token.create(
        //                        tokenResponse.getTokenType(),
        //                        tokenResponse.getAccessToken().getParsedString(),
        //                        tokenResponse.getRefreshToken(),
        //                        tokenResponse.getExpiresIn());

        String sandboxToken =
                "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiIsImtpZCI6IkhTMjU2In0.eyJqdGkiOiJjZTdiNTVmNzlmY2ZlIiwic3ViIjoiZHNwMiIsImF1ZCI6Ii4qXFwuYm91cnNvcmFtYVxcLmNvbSIsImV4cCI6MTg2NDIyNDY1NCwiaWF0IjoxNTQ4ODY0NjU0LCJuYmYiOjE1NDg4NjQ2NTQsInNlc3Npb24iOnsidXNlcklkIjoiMDAwMDAwMDAiLCJsZXZlbCI6IkNVU1RPTUVSIn0sImlzcyI6IkFkbWluIEpXVCBCb3Vyc29yYW1hIiwidXNlckhhc2giOiI3MDM1MmY0MTA2MWVkYTQiLCJvcmciOiJCMTkiLCJvYXV0aCI6ImM2OTdjOWUxZTUxZjg4Y2U2NWJjOGM4NWNmMjhkMDcyYWNmMDQyNTQifQ.3sewgdSK4OJfcsrVK2eqa8FF2jvDfdpiyBuIOh0CMRI";

        return OAuth2Token.createBearer(sandboxToken, "", 99999999999L);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) {
        TokenResponse tokenResponse = apiClient.refreshToken(new RefreshTokenRequest(refreshToken));

        return OAuth2Token.createBearer(
                tokenResponse.getAccessToken().getParsedString(),
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
