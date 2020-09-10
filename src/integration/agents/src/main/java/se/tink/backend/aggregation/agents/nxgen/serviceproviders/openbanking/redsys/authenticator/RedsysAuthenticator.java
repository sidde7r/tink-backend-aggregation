package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.authenticator;

import java.util.Date;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.StorageKeys;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class RedsysAuthenticator implements OAuth2Authenticator {
    private static final Logger log = LoggerFactory.getLogger(RedsysAuthenticator.class);
    private final RedsysApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final String codeVerifier;

    public RedsysAuthenticator(RedsysApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.codeVerifier = generateCodeVerifier();
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return apiClient.getAuthorizeUrl(state, generateCodeChallenge(this.codeVerifier));
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws AuthenticationException {
        final OAuth2Token token = apiClient.getToken(code, this.codeVerifier);
        debugLogToken("Got token", token);
        return token;
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) throws SessionException {
        try {
            log.info("Using refresh token: {}", Hash.sha256AsHex(refreshToken));
            final OAuth2Token oAuth2Token = apiClient.refreshToken(refreshToken);
            debugLogToken("Refreshed token", oAuth2Token);
            return oAuth2Token;
        } catch (HttpResponseException hre) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        debugLogToken("Using token", accessToken);
        sessionStorage.put(StorageKeys.OAUTH_TOKEN, accessToken);
    }

    private String generateCodeChallenge(String codeVerifier) {
        byte[] digest = Hash.sha256(codeVerifier);
        return Base64.encodeBase64URLSafeString(digest);
    }

    private String generateCodeVerifier() {
        return RandomUtils.generateRandomBase64UrlEncoded(48);
    }

    private static void debugLogToken(String tag, OAuth2Token token) {
        log.info(
                "{}: type={}, access={}, refresh={}, validUntil={}",
                tag,
                token.getTokenType(),
                Hash.sha256AsHex(token.getAccessToken()),
                token.getRefreshToken().map(Hash::sha256AsHex).orElse("null"),
                new Date(token.getAccessExpireEpoch() * 1000));
    }
}
