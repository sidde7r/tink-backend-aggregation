package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.Paths;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.QueryParams;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.TokenParams;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankUrlFactory;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class VolksbankAuthenticator implements OAuth2Authenticator {

    private final Logger logger = LoggerFactory.getLogger(VolksbankAuthenticator.class);

    private final VolksbankApiClient client;
    private final PersistentStorage persistentStorage;
    private final URL redirectUri;
    private final VolksbankUrlFactory urlFactory;
    private final ConsentFetcher consentFetcher;
    private final String clientId;
    private final String clientSecret;

    public VolksbankAuthenticator(
            VolksbankApiClient client,
            PersistentStorage persistentStorage,
            URL redirectUri,
            VolksbankUrlFactory urlFactory,
            ConsentFetcher consentFetcher,
            String clientId,
            String clientSecret) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.redirectUri = redirectUri;
        this.urlFactory = urlFactory;
        this.consentFetcher = consentFetcher;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {

        final String consentId = consentFetcher.fetchConsent();

        return urlFactory
                .buildURL(Paths.AUTHORIZE)
                .queryParam(QueryParams.SCOPE, QueryParams.SCOPE_VALUE)
                .queryParam(QueryParams.RESPONSE_TYPE, QueryParams.RESPONSE_TYPE_VALUE)
                .queryParam(QueryParams.STATE, state)
                .queryParam(QueryParams.REDIRECT_URI, redirectUri.toString())
                .queryParam(QueryParams.CONSENT_ID, consentId)
                .queryParam(QueryParams.CLIENT_ID, consentFetcher.getClientId());
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {

        URL url =
                urlFactory
                        .buildURL(Paths.TOKEN)
                        .queryParam(QueryParams.CODE, code)
                        .queryParam(QueryParams.GRANT_TYPE, TokenParams.AUTHORIZATION_CODE)
                        .queryParam(QueryParams.REDIRECT_URI, redirectUri.toString());

        OAuth2Token token = getBearerToken(url);
        persistentStorage.put(Storage.OAUTH_TOKEN, token);
        return token;
    }

    private OAuth2Token getBearerToken(final URL url) {

        try {
            return client.getBearerToken(url, clientId, clientSecret);
        } catch (HttpResponseException e) {
            if (e.getResponse().getBody(String.class).contains("unsupported_grant_type")) {
                // Likely indicates that the consent ID has been invalidated. At this point, there
                // is nothing left to do but to clear everything and start over.
                persistentStorage.remove(Storage.CONSENT);
                persistentStorage.remove(Storage.OAUTH_TOKEN);
                throw BankServiceError.CONSENT_REVOKED.exception(e);
            }
            throw e;
        }
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) throws BankServiceException {
        logger.info("Volksbank - Refreshing access token");

        // TODO temporary log persist refresh token
        logger.info("Volksbank - get persist refresh token: {}", refreshToken.hashCode());

        URL url =
                urlFactory
                        .buildURL(Paths.TOKEN)
                        .queryParam(QueryParams.GRANT_TYPE, TokenParams.REFRESH_TOKEN)
                        .queryParam(QueryParams.REFRESH_TOKEN, refreshToken);

        OAuth2Token token = getBearerToken(url);
        persistentStorage.put(Storage.OAUTH_TOKEN, token);
        // TODO temporary log to trace newly received refresh token
        logger.info("Volksbank - get new refresh token: {}", token.getRefreshToken().hashCode());
        return token;
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        persistentStorage.put(Storage.OAUTH_TOKEN, accessToken);
    }
}
