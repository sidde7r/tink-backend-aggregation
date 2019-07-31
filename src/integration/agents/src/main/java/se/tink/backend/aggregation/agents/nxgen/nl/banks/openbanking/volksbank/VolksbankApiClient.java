package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.ConsentParams;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.Paths;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.TransactionFetcherParams;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.rpc.ConsentRequestBody;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount.entities.accounts.AccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount.rpc.BalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount.rpc.TransactionResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;

public class VolksbankApiClient {

    private final TinkHttpClient client;
    private final VolksbankUrlFactory urlFactory;

    public VolksbankApiClient(final TinkHttpClient client, final VolksbankUrlFactory urlFactory) {
        this.client = client;
        this.urlFactory = urlFactory;
    }

    public TransactionResponse readTransactions(
            final TransactionalAccount account,
            final Map<String, String> urlParams,
            final String consentId,
            final OAuth2Token oauth2Token) {

        URL url =
                urlFactory.buildURL(
                        Paths.ACCOUNTS + "/" + account.getApiIdentifier() + Paths.TRANSACTIONS);

        RequestBuilder request =
                client.request(url)
                        .header(HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .header(HeaderKeys.REQUEST_ID, getRequestId())
                        .header(HeaderKeys.CONSENT_ID, consentId)
                        .addBearerToken(oauth2Token);

        if (urlParams != null) {
            for (String key : urlParams.keySet()) {
                request = request.queryParam(key, urlParams.get(key));
            }
        } else {
            request =
                    request.queryParam(
                                    TransactionFetcherParams.BOOKING_STATUS,
                                    TransactionFetcherParams.BOOKING_STATUS_VALUE)
                            .queryParam(
                                    TransactionFetcherParams.PAGE_DIRECTION,
                                    TransactionFetcherParams.PAGE_DIRECTION_VALUE)
                            .queryParam(
                                    TransactionFetcherParams.LIMIT,
                                    TransactionFetcherParams.LIMIT_VALUE.toString());
        }

        String response = request.get(String.class);

        return getResponse(response, TransactionResponse.class);
    }

    public BalancesResponse readBalance(
            AccountsEntity account, final String consentId, final OAuth2Token oAuth2Token) {

        URL url =
                urlFactory.buildURL(
                        Paths.ACCOUNTS + "/" + account.getResourceId() + Paths.BALANCES);

        String response =
                client.request(url)
                        .header(HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .header(HeaderKeys.REQUEST_ID, getRequestId())
                        .header(HeaderKeys.CONSENT_ID, consentId)
                        .addBearerToken(oAuth2Token)
                        .get(String.class);

        return getResponse(response, BalancesResponse.class);
    }

    public AccountsResponse fetchAccounts(final String consentId, final OAuth2Token oAuth2Token) {

        URL url = urlFactory.buildURL(Paths.ACCOUNTS);

        String response =
                client.request(url)
                        .header(HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .header(HeaderKeys.REQUEST_ID, getRequestId())
                        .header(HeaderKeys.CONSENT_ID, consentId)
                        .addBearerToken(oAuth2Token)
                        .get(String.class);

        return getResponse(response, AccountsResponse.class);
    }

    public ConsentResponse consentRequest(final URL redirectUrl, final String clientId) {
        return getResponse(consentRequestString(redirectUrl, clientId), ConsentResponse.class);
    }

    public String consentRequestString(final URL redirectUrl, final String clientId) {
        final URL url = urlFactory.buildURL(VolksbankConstants.Paths.CONSENT);
        final ConsentRequestBody body =
                new ConsentRequestBody(
                        VolksbankUtils.getFutureDateAsString(ConsentParams.VALID_YEAR),
                        ConsentParams.FREQUENCY_PER_DAY);

        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header(HeaderKeys.REQUEST_ID, getRequestId())
                .header(HeaderKeys.TPP_REDIRECT_URI, redirectUrl)
                .header(HeaderKeys.AUTHORIZATION, clientId)
                .body(body)
                .post(String.class);
    }

    public OAuth2Token getBearerToken(URL url) {

        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .header(HeaderKeys.REQUEST_ID, getRequestId())
                .post(TokenResponse.class)
                .toOauthToken();
    }

    private static String getRequestId() {
        return java.util.UUID.randomUUID().toString();
    }

    private <E> E getResponse(String response, Class<E> contentClass) {

        /*
           We are getting )]}',\n extra characters in response, we need to clean them first and then
           we can serialize the JSON string into an object
        */
        response = response.substring(response.indexOf("\n") + 1);

        try {
            return new ObjectMapper().readValue(response, contentClass);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
