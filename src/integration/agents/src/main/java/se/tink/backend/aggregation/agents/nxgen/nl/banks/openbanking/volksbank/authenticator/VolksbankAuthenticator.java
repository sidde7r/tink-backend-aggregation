package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator;

import java.util.Optional;
import javax.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.Paths;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.QueryParams;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.TokenParams;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.rpc.TokenErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.configuration.VolksbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.utils.VolksbankUrlFactory;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
public class VolksbankAuthenticator implements OAuth2Authenticator {

    private final VolksbankApiClient client;
    private final PersistentStorage persistentStorage;
    private final AgentConfiguration<VolksbankConfiguration> agentConfiguration;
    private final VolksbankUrlFactory urlFactory;
    private final ConsentFetcher consentFetcher;
    private VolksbankConfiguration volksbankConfiguration;

    public VolksbankAuthenticator(
            VolksbankApiClient client,
            PersistentStorage persistentStorage,
            AgentConfiguration<VolksbankConfiguration> agentConfiguration,
            VolksbankUrlFactory urlFactory,
            ConsentFetcher consentFetcher) {
        this.client = client;
        this.persistentStorage = persistentStorage;
        this.agentConfiguration = agentConfiguration;
        this.urlFactory = urlFactory;
        this.consentFetcher = consentFetcher;

        this.volksbankConfiguration = agentConfiguration.getProviderSpecificConfiguration();
    }

    @Override
    public URL buildAuthorizeUrl(String state) {

        final String consentId = consentFetcher.fetchConsent();

        return urlFactory
                .buildURL(Urls.HOST_PORT_10443, Paths.AUTHORIZE)
                .queryParam(QueryParams.SCOPE, QueryParams.SCOPE_VALUE)
                .queryParam(QueryParams.RESPONSE_TYPE, QueryParams.RESPONSE_TYPE_VALUE)
                .queryParam(QueryParams.STATE, state)
                .queryParam(QueryParams.REDIRECT_URI, agentConfiguration.getRedirectUrl())
                .queryParam(QueryParams.CONSENT_ID, consentId)
                .queryParam(QueryParams.CLIENT_ID, volksbankConfiguration.getClientId());
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {

        URL url =
                urlFactory
                        .buildURL(Urls.HOST, Paths.TOKEN)
                        .queryParam(QueryParams.CODE, code)
                        .queryParam(QueryParams.GRANT_TYPE, TokenParams.AUTHORIZATION_CODE)
                        .queryParam(QueryParams.REDIRECT_URI, agentConfiguration.getRedirectUrl());

        OAuth2Token token = getBearerToken(url);
        persistentStorage.put(Storage.OAUTH_TOKEN, token);
        return token;
    }

    private OAuth2Token getBearerToken(final URL url) {

        return client.getBearerToken(
                url,
                volksbankConfiguration.getClientId(),
                volksbankConfiguration.getClientSecret());
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {

        checkConsentStatus();

        URL url =
                urlFactory
                        .buildURL(Urls.HOST, Paths.TOKEN)
                        .queryParam(QueryParams.GRANT_TYPE, TokenParams.REFRESH_TOKEN)
                        .queryParam(QueryParams.REFRESH_TOKEN, refreshToken);
        try {
            OAuth2Token token = getBearerToken(url);
            persistentStorage.put(Storage.OAUTH_TOKEN, token);
            return token;
        } catch (HttpResponseException e) {
            checkTokenExpiration(e.getResponse());
            // Unknown error, re-throw
            throw e;
        }
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        persistentStorage.put(Storage.OAUTH_TOKEN, accessToken);
    }

    private void checkTokenExpiration(HttpResponse httpResponse) {
        checkErrorBody(httpResponse);
        final TokenErrorResponse errorResponse = httpResponse.getBody(TokenErrorResponse.class);

        if (Optional.ofNullable(errorResponse).isPresent() && errorResponse.isExpiredToken()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    private void checkConsentStatus() {
        if (!consentFetcher.isConsentValid()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    private void checkErrorBody(HttpResponse httpResponse) {
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(httpResponse.getType())) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception("Incorrect error body response.");
        }
    }
}
