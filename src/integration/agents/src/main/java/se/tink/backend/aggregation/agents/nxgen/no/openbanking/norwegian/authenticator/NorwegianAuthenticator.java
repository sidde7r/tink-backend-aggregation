package se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.authenticator;

import java.util.Objects;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.NorwegianConfiguration;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.NorwegianConstants;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.authenticator.rpc.RefreshRequest;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.client.NorwegianApiClient;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NorwegianAuthenticator implements OAuth2Authenticator {

    private final SessionStorage sessionStorage;
    private final NorwegianApiClient apiClient;
    private final NorwegianConfiguration norwegianConfiguration;
    private final String codeVerifier;

    public NorwegianAuthenticator(
            NorwegianApiClient apiClient,
            SessionStorage sessionStorage,
            NorwegianConfiguration norwegianConfiguration) {
        this.apiClient = Objects.requireNonNull(apiClient);
        this.sessionStorage = Objects.requireNonNull(sessionStorage);
        this.norwegianConfiguration = Objects.requireNonNull(norwegianConfiguration);
        this.codeVerifier = generateCodeVerifier();
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return apiClient.getAuthorizeUrl(state, generateCodeChallenge(this.codeVerifier));
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        return apiClient.exchangeAuthorizationToken(code, this.codeVerifier);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {
        RefreshRequest refreshRequest =
                new RefreshRequest(
                        refreshToken,
                        norwegianConfiguration.getClientId(),
                        NorwegianConstants.QueryValues.REFRESH_TOKEN);
        return apiClient.exchangeRefreshToken(refreshRequest);
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        sessionStorage.put(NorwegianConstants.StorageKeys.TOKEN, accessToken);
    }

    private String generateCodeChallenge(String codeVerifier) {
        byte[] digest = Hash.sha256(codeVerifier);
        return EncodingUtils.encodeAsBase64UrlSafe(digest);
    }

    private String generateCodeVerifier() {
        return RandomUtils.generateRandomBase64UrlEncoded(48);
    }
}
