package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator;

import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.Paths;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.QueryParams;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.TokenParams;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankUrlFactory;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class VolksbankAuthenticator implements OAuth2Authenticator {

    private final Logger logger = LoggerFactory.getLogger(VolksbankAuthenticator.class);

    private final VolksbankApiClient client;
    private final PersistentStorage persistentStorage;
    private final URL redirectUri;
    private final VolksbankUrlFactory urlFactory;
    private final ConsentFetcher consentFetcher;

    public VolksbankAuthenticator(
            VolksbankApiClient client,
            PersistentStorage persistentStorage,
            URL redirectUri,
            VolksbankUrlFactory urlFactory,
            ConsentFetcher consentFetcher) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.redirectUri = redirectUri;
        this.urlFactory = urlFactory;
        this.consentFetcher = consentFetcher;
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
                .queryParam(
                        QueryParams.CLIENT_ID,
                        client.getConfiguration().getAisConfiguration().getClientId());
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {

        URL url =
                urlFactory
                        .buildURL(Paths.TOKEN)
                        .queryParam(QueryParams.CODE, code)
                        .queryParam(
                                QueryParams.CLIENT_ID,
                                client.getConfiguration().getAisConfiguration().getClientId())
                        .queryParam(
                                QueryParams.CLIENT_SECRET,
                                client.getConfiguration().getAisConfiguration().getClientSecret())
                        .queryParam(QueryParams.GRANT_TYPE, TokenParams.AUTHORIZATION_CODE)
                        .queryParam(QueryParams.REDIRECT_URI, redirectUri.toString());

        return client.getBearerToken(url);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) throws BankServiceException {
        logger.info("SNS - Refreshing access token");

        OAuth2Token oldToken =
                persistentStorage
                        .get(Storage.OAUTH_TOKEN, OAuth2Token.class)
                        .orElseThrow(
                                () ->
                                        new NoSuchElementException(
                                                "Cannot refresh access token, could not fetch old token object"));

        refreshToken =
                oldToken.getRefreshToken()
                        .orElseThrow(
                                () ->
                                        new NoSuchElementException(
                                                "Cannot refresh access token, could not fetch refresh token from old token object"));

        URL url =
                urlFactory
                        .buildURL(Paths.TOKEN)
                        .queryParam(
                                QueryParams.CLIENT_ID,
                                client.getConfiguration().getAisConfiguration().getClientId())
                        .queryParam(
                                QueryParams.CLIENT_SECRET,
                                client.getConfiguration().getAisConfiguration().getClientSecret())
                        .queryParam(QueryParams.GRANT_TYPE, TokenParams.REFRESH_TOKEN)
                        .queryParam(QueryParams.REFRESH_TOKEN, refreshToken);

        return client.getBearerToken(url);
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        persistentStorage.put(Storage.OAUTH_TOKEN, accessToken);
    }
}
