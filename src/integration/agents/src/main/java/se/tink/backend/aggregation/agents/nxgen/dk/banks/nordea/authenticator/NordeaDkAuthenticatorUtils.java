package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.cryptography.hash.Hash;
import se.tink.libraries.encoding.EncodingUtils;

@RequiredArgsConstructor
public class NordeaDkAuthenticatorUtils {

    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;
    private final NordeaDkApiClient bankClient;

    protected void saveToken(OAuth2Token token) {
        persistentStorage.put(NordeaDkConstants.StorageKeys.OAUTH_TOKEN, token);
        sessionStorage.put(NordeaDkConstants.StorageKeys.OAUTH_TOKEN, token);
    }

    protected void exchangeOauthToken(String authorizationCode) {
        OAuth2Token token =
                bankClient
                        .oauthCallback(
                                authorizationCode,
                                sessionStorage.get(NordeaDkConstants.StorageKeys.CODE_VERIFIER))
                        .toOauthToken()
                        .orElseThrow(LoginError.CREDENTIALS_VERIFICATION_ERROR::exception);
        saveToken(token);
    }

    protected OAuthSessionData prepareOAuthSessionData() {
        String state = generateState();
        String nonce = generateNonce();
        String codeVerifier = generateCodeVerifier();
        sessionStorage.put(NordeaDkConstants.StorageKeys.CODE_VERIFIER, codeVerifier);

        return OAuthSessionData.builder()
                .state(state)
                .nonce(nonce)
                .codeChallenge(generateCodeChallenge(codeVerifier))
                .build();
    }

    private static String generateCodeChallenge(String codeVerifier) {
        byte[] digest = Hash.sha256(codeVerifier);
        return EncodingUtils.encodeAsBase64UrlSafe(digest);
    }

    private static String generateCodeVerifier() {
        return RandomUtils.generateRandomBase64UrlEncoded(86);
    }

    private static String generateNonce() {
        return RandomUtils.generateRandomBase64UrlEncoded(26);
    }

    private static String generateState() {
        return RandomUtils.generateRandomBase64UrlEncoded(26);
    }
}
