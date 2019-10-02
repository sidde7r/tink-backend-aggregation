package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.NordeaPartnerConstants.EndPoints;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.NordeaPartnerConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.NordeaPartnerConstants.PathParamsKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.NordeaPartnerConstants.QueryParamsKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.NordeaPartnerConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.authenticator.NordeaPartnerJweHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.configuration.NordeaPartnerConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.rpc.AccountListResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.rpc.AccountTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordeaPartnerApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private NordeaPartnerConfiguration configuration;
    private NordeaPartnerJweHelper jweHelper;

    public NordeaPartnerApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public void setConfiguration(NordeaPartnerConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setJweHelper(NordeaPartnerJweHelper jweHelper) {
        this.jweHelper = jweHelper;
    }

    private URL endpointUrl(String endpoint) {
        return new URL(configuration.getBaseUrl() + endpoint);
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
                .get(StorageKeys.TOKEN, OAuth2Token.class)
                .filter(OAuth2Token::hasAccessExpired)
                .orElse(this.refreshAccessToken());
    }

    private OAuth2Token refreshAccessToken() {
        // if access token is expired we can generate a new access token using the puid
        String partnerUid =
                sessionStorage
                        .get(NordeaPartnerConstants.StorageKeys.PARTNER_USER_ID, String.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                SessionError.SESSION_EXPIRED.exception()));
        OAuth2Token oAuth2Token = jweHelper.createAccessToken(partnerUid);
        sessionStorage.put(NordeaPartnerConstants.StorageKeys.TOKEN, oAuth2Token);
        return oAuth2Token;
    }
}
