package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.authenticator;

import org.apache.commons.codec.binary.Base64;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.consent.RedsysConsentController;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class RedsysAuthenticator implements OAuth2Authenticator {
    private final RedsysApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final String codeVerifier;
    private final RedsysConsentController consentController;

    public RedsysAuthenticator(
            RedsysApiClient apiClient,
            SessionStorage sessionStorage,
            RedsysConsentController consentController) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.codeVerifier = generateCodeVerifier();
        this.consentController = consentController;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return apiClient.getAuthorizeUrl(state, generateCodeChallenge(this.codeVerifier));
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws AuthenticationException {
        final OAuth2Token token = apiClient.getToken(code, this.codeVerifier);
        sessionStorage.put(StorageKeys.OAUTH_TOKEN, token);
        if (!consentController.requestConsent()) {
            // Consent was not given
            sessionStorage.remove(StorageKeys.OAUTH_TOKEN);
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        return token;
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) throws SessionException {
        try {
            return apiClient.refreshToken(refreshToken);
        } catch (HttpResponseException hre) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        sessionStorage.put(StorageKeys.OAUTH_TOKEN, accessToken);
    }

    private String generateCodeChallenge(String codeVerifier) {
        byte[] digest = Hash.sha256(codeVerifier);
        return Base64.encodeBase64URLSafeString(digest);
    }

    private String generateCodeVerifier() {
        return RandomUtils.generateRandomBase64UrlEncoded(48);
    }
}
