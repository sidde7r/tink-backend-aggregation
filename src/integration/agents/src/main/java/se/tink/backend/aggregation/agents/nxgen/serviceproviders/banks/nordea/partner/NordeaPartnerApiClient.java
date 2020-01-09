package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.NordeaPartnerConstants.EndPoints;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.NordeaPartnerConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.NordeaPartnerConstants.PathParamsKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.NordeaPartnerConstants.QueryParamsKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.authenticator.NordeaPartnerJweHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.configuration.NordeaPartnerConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.rpc.AccountListResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.rpc.AccountTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordeaPartnerApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private final Credentials credentials;
    private NordeaPartnerConfiguration configuration;
    private NordeaPartnerJweHelper jweHelper;

    public NordeaPartnerApiClient(
            TinkHttpClient client, SessionStorage sessionStorage, Credentials credentials) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.credentials = credentials;
    }

    public void setConfiguration(NordeaPartnerConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setJweHelper(NordeaPartnerJweHelper jweHelper) {
        this.jweHelper = jweHelper;
    }

    private URL endpointUrl(String endpoint) {
        return configuration.getBaseUrl().concat(endpoint);
    }

    public AccountListResponse fetchAccounts() {
        RequestBuilder request =
                client.request(
                                endpointUrl(EndPoints.ACCOUNTS)
                                        .parameter(
                                                PathParamsKeys.PARTNER_ID,
                                                configuration.getPartnerId()))
                        .accept(MediaType.APPLICATION_JSON)
                        .acceptLanguage(HeaderValues.ACCEPT_LANGUAGE);

        return requestRefreshableGet(request, AccountListResponse.class);
    }

    public AccountTransactionsResponse fetchAccountTransaction(String accountId, String key) {
        RequestBuilder request =
                client.request(
                                endpointUrl(EndPoints.ACCOUNT_TRANSACTIONS)
                                        .parameter(
                                                PathParamsKeys.PARTNER_ID,
                                                configuration.getPartnerId())
                                        .parameter(PathParamsKeys.ACCOUNT_ID, accountId))
                        .accept(MediaType.APPLICATION_JSON)
                        .acceptLanguage(HeaderValues.ACCEPT_LANGUAGE)
                        .queryParam(QueryParamsKeys.CONTINUATION_KEY, key);

        return requestRefreshableGet(request, AccountTransactionsResponse.class);
    }

    private <T> T requestRefreshableGet(RequestBuilder request, Class<T> responseType) {
        try {
            return request.addBearerToken(getAccessToken()).get(responseType);

        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_FORBIDDEN) {
                OAuth2Token newToken = refreshAccessToken();
                // use the new access token

                request.overrideHeader(HttpHeaders.AUTHORIZATION, newToken.toAuthorizeHeader());
                return request.get(responseType);
            }
            throw e;
        }
    }

    private OAuth2Token getAccessToken() {
        return sessionStorage
                .get(OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class)
                .filter(OAuth2Token::hasAccessExpired)
                .orElse(this.refreshAccessToken());
    }

    private OAuth2Token refreshAccessToken() {
        // if access token is expired we can generate a new access token using the puid
        String partnerUid = credentials.getField(Field.Key.USERNAME);
        OAuth2Token oAuth2Token = jweHelper.createToken(partnerUid);
        sessionStorage.put(OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN, oAuth2Token);
        return oAuth2Token;
    }
}
