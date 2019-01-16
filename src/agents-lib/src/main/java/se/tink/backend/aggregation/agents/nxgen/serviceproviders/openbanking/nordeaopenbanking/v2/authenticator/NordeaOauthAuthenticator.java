package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.authenticator;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.NordeaBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.NordeaPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.NordeaSessionStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;

public class NordeaOauthAuthenticator implements OAuth2Authenticator {
    private static final Logger LOG = LoggerFactory.getLogger(
            NordeaOauthAuthenticator.class);

    private final NordeaBaseApiClient apiClient;
    private final NordeaSessionStorage sessionStorage;
    private final NordeaPersistentStorage persistentStorage;

    public NordeaOauthAuthenticator(NordeaBaseApiClient apiClient,
            NordeaSessionStorage sessionStorage, NordeaPersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        Map<String, String> queryParams = setupAuthorizationQuery(state);
        return NordeaBaseConstants.Url.AUTHORIZE.queryParams(queryParams);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        OAuth2Token accessToken = apiClient.oauthExchangeCodeForAccessToken(code).toOauth2Token();
        sessionStorage.setAccessToken(accessToken);

        return accessToken;
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) throws SessionException, BankServiceException {
        LOG.info("Refresh token, not implemented for Nordea Open banking");
        throw SessionError.SESSION_EXPIRED.exception();
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        LOG.info("useAccessToken called");
        sessionStorage.setAccessToken(accessToken);
    }

    private Map<String, String> setupAuthorizationQuery(String state) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put(NordeaBaseConstants.Query.STATE,
                state);
        queryParams.put(NordeaBaseConstants.Query.CLIENT_ID,
                persistentStorage.getClientId());
        queryParams.put(NordeaBaseConstants.Query.REDIRECT_URI,
                persistentStorage.getRedirectUrl());
        queryParams.put(NordeaBaseConstants.Query.SCOPE,
                NordeaBaseConstants.Authorization.SCOPES.stream().collect(Collectors.joining(",")));
        queryParams.put(NordeaBaseConstants.Query.DURATION,
                String.valueOf(NordeaBaseConstants.Authorization.TOKEN_DURATION));
        queryParams.put(NordeaBaseConstants.Query.LANGUAGE,
                NordeaBaseConstants.Authorization.LANGUAGE);

        return queryParams;
    }
}
