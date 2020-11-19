package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.NordeaPartnerConstants.EndPoints;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.NordeaPartnerConstants.PathParamsKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.NordeaPartnerConstants.QueryParamsKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.authenticator.NordeaPartnerJweHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.configuration.NordeaPartnerConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.creditcard.rpc.CardListResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.creditcard.rpc.CardTransactionListResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.rpc.AccountListResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.rpc.AccountTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
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
    private final String locale;
    private NordeaPartnerConfiguration configuration;
    private NordeaPartnerJweHelper jweHelper;

    public NordeaPartnerApiClient(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            Credentials credentials,
            String locale) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.credentials = credentials;
        this.locale = locale;
    }

    public void setConfiguration(NordeaPartnerConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setJweHelper(NordeaPartnerJweHelper jweHelper) {
        this.jweHelper = jweHelper;
    }

    public AccountListResponse fetchAccounts() {
        return requestRefreshableGet(request(EndPoints.ACCOUNTS), AccountListResponse.class);
    }

    public AccountTransactionsResponse fetchAccountTransaction(String accountId, String key) {
        return requestRefreshableGet(
                request(EndPoints.ACCOUNT_TRANSACTIONS, PathParamsKeys.ACCOUNT_ID, accountId)
                        .queryParam(QueryParamsKeys.CONTINUATION_KEY, key),
                AccountTransactionsResponse.class);
    }

    private RequestBuilder request(String endpoint) {
        return request(endpoint, null, null);
    }

    private RequestBuilder request(String endpoint, String pathParamKey, String pathParamValue) {
        URL url =
                configuration
                        .getBaseUrl()
                        .concat(endpoint)
                        .parameter(PathParamsKeys.PARTNER_ID, configuration.getPartnerId());
        if (pathParamKey != null && pathParamValue != null) {
            url = url.parameter(pathParamKey, pathParamValue);
        }
        return client.request(url);
    }

    private <T> T requestRefreshableGet(RequestBuilder request, Class<T> responseType) {
        try {
            return request.addBearerToken(getAccessToken())
                    .accept(MediaType.APPLICATION_JSON)
                    .acceptLanguage(locale)
                    .get(responseType);

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
                .filter(OAuth2Token::isValid)
                .orElseGet(this::refreshAccessToken);
    }

    private OAuth2Token refreshAccessToken() {
        // if access token is expired we can generate a new access token using the puid
        String partnerUid = credentials.getField(Field.Key.USERNAME);
        OAuth2Token oAuth2Token = jweHelper.createToken(partnerUid);
        sessionStorage.put(OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN, oAuth2Token);
        return oAuth2Token;
    }

    public CardListResponse fetchCreditCards() {
        return requestRefreshableGet(request(EndPoints.CARDS), CardListResponse.class);
    }

    public CardTransactionListResponse fetchCreditCardTransactions(
            String cardId, int page, int pageSize) {
        return requestRefreshableGet(
                request(EndPoints.CARD_TRANSACTIONS, PathParamsKeys.CARD_ID, cardId)
                        .queryParam(QueryParamsKeys.PAGE, Integer.toString(page))
                        .queryParam(QueryParamsKeys.PAGE_SIZE, Integer.toString(pageSize)),
                CardTransactionListResponse.class);
    }
}
