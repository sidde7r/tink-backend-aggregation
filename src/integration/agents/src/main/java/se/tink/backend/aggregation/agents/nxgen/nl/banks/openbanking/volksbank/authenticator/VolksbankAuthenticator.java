package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator;

import java.util.NoSuchElementException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.Paths;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.QueryParams;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.TokenParams;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankUtils;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class VolksbankAuthenticator implements OAuth2Authenticator {

    private final VolksbankApiClient client;
    private final SessionStorage sessionStorage;
    private final String redirectUri;

    public VolksbankAuthenticator(
            VolksbankApiClient client, SessionStorage sessionStorage, String redirectUri) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.redirectUri = redirectUri;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {

        return VolksbankUtils.buildURL(Paths.AUTHORIZE)
                .queryParam(QueryParams.SCOPE, QueryParams.SCOPE_VALUE)
                .queryParam(QueryParams.RESPONSE_TYPE, QueryParams.RESPONSE_TYPE_VALUE)
                .queryParam(QueryParams.STATE, state)
                .queryParam(QueryParams.REDIRECT_URI, redirectUri)
                .queryParam(
                        QueryParams.CLIENT_ID,
                        client.getConfiguration().getAisConfiguration().getClientId());
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {

        URL url =
                VolksbankUtils.buildURL(Paths.TOKEN)
                        .queryParam(QueryParams.CODE, code)
                        .queryParam(
                                QueryParams.CLIENT_ID,
                                client.getConfiguration().getAisConfiguration().getClientId())
                        .queryParam(
                                QueryParams.CLIENT_SECRET,
                                client.getConfiguration().getAisConfiguration().getClientSecret())
                        .queryParam(QueryParams.GRANT_TYPE, TokenParams.AUTHORIZATION_CODE)
                        .queryParam(QueryParams.REDIRECT_URI, redirectUri);

        OAuth2Token token = client.getBearerToken(url);

        if (!sessionStorage.containsKey(Storage.CONSENT)) {
            ConsentResponse consent = client.consentRequest();
            sessionStorage.put(Storage.CONSENT, consent.getConsentId());
        }

        return token;
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) throws BankServiceException {

        OAuth2Token oldToken =
                sessionStorage
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
                VolksbankUtils.buildURL(Paths.TOKEN)
                        .queryParam(
                                QueryParams.CLIENT_ID,
                                client.getConfiguration().getAisConfiguration().getClientId())
                        .queryParam(
                                QueryParams.CLIENT_SECRET,
                                client.getConfiguration().getAisConfiguration().getClientSecret())
                        .queryParam(QueryParams.GRANT_TYPE, TokenParams.REFRESH_TOKEN)
                        .queryParam(QueryParams.REFRESH_TOKEN, refreshToken);

        OAuth2Token token = client.getBearerToken(url);
        return token;
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        sessionStorage.put(Storage.OAUTH_TOKEN, accessToken);
    }
}
