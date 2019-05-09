package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.ConsentParams;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.Paths;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.TransactionFetcherParams;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.rpc.ConsentRequestBody;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.configuration.VolksbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount.entities.accounts.AccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount.rpc.BalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount.rpc.TransactionResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class VolksbankApiClient {

    private final VolksbankHttpClient client;
    private final SessionStorage sessionStorage;
    private VolksbankConfiguration configuration;

    public VolksbankApiClient(VolksbankHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public void setConfiguration(VolksbankConfiguration configuration) {
        this.configuration = configuration;
    }

    public VolksbankConfiguration getConfiguration() {
        return configuration;
    }

    public TransactionResponse readTransactions(
            TransactionalAccount account, Map<String, String> urlParams) {

        URL url =
                VolksbankUtils.buildURL(
                        Paths.ACCOUNTS + "/" + account.getApiIdentifier() + Paths.TRANSACTIONS);

        RequestBuilder request =
                client.getTinkClient()
                        .request(url)
                        .header(HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .header(HeaderKeys.REQUEST_ID, getRequestId())
                        .header(HeaderKeys.CONSENT_ID, sessionStorage.get(Storage.CONSENT))
                        .addBearerToken(
                                sessionStorage
                                        .get(Storage.OAUTH_TOKEN, OAuth2Token.class)
                                        .orElseThrow(
                                                () ->
                                                        new NoSuchElementException(
                                                                "Missing Oauth token!")));

        if (urlParams != null) {
            for (String key : urlParams.keySet()) {
                request = request.queryParam(key, urlParams.get(key));
            }
        } else {
            String now = VolksbankUtils.getCurrentDateAsString();
            request =
                    request.queryParam(
                                    TransactionFetcherParams.BOOKING_STATUS,
                                    TransactionFetcherParams.BOOKING_STATUS_VALUE)
                            .queryParam(TransactionFetcherParams.DATE_FROM, now)
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

    public BalancesResponse readBalance(AccountsEntity account) {

        URL url =
                VolksbankUtils.buildURL(
                        Paths.ACCOUNTS + "/" + account.getResourceId() + Paths.BALANCES);

        String response =
                client.getTinkClient()
                        .request(url)
                        .header(HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .header(HeaderKeys.REQUEST_ID, getRequestId())
                        .header(HeaderKeys.CONSENT_ID, sessionStorage.get(Storage.CONSENT))
                        .addBearerToken(
                                sessionStorage
                                        .get(Storage.OAUTH_TOKEN, OAuth2Token.class)
                                        .orElseThrow(
                                                () ->
                                                        new NoSuchElementException(
                                                                "Missing Oauth token!")))
                        .get(String.class);

        return getResponse(response, BalancesResponse.class);
    }

    public AccountsResponse fetchAccounts() {

        URL url = VolksbankUtils.buildURL(Paths.ACCOUNTS);

        String response =
                client.getTinkClient()
                        .request(url)
                        .header(HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .header(HeaderKeys.REQUEST_ID, getRequestId())
                        .header(HeaderKeys.CONSENT_ID, sessionStorage.get(Storage.CONSENT))
                        .addBearerToken(
                                sessionStorage
                                        .get(Storage.OAUTH_TOKEN, OAuth2Token.class)
                                        .orElseThrow(
                                                () ->
                                                        new NoSuchElementException(
                                                                "Missing Oauth token!")))
                        .get(String.class);

        return getResponse(response, AccountsResponse.class);
    }

    public ConsentResponse consentRequest() {

        URL url = VolksbankUtils.buildURL(VolksbankConstants.Paths.CONSENT);

        String response =
                client.getTinkClient()
                        .request(url)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .header(HeaderKeys.REQUEST_ID, getRequestId())
                        .header(
                                HeaderKeys.AUTHORIZATION,
                                configuration.getAisConfiguration().getClientId())
                        .body(
                                new ConsentRequestBody(
                                        VolksbankUtils.getFutureDateAsString(
                                                ConsentParams.VALID_YEAR),
                                        ConsentParams.FREQUENCY_PER_DAY))
                        .post(String.class);

        return getResponse(response, ConsentResponse.class);
    }

    public OAuth2Token getBearerToken(URL url) {

        OAuth2Token token =
                client.getTinkClient()
                        .request(url)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HeaderKeys.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                        .header(HeaderKeys.REQUEST_ID, getRequestId())
                        .post(TokenResponse.class)
                        .toOauthToken();
        return token;
    }

    private String getRequestId() {
        return java.util.UUID.randomUUID().toString();
    }

    private <E> E getResponse(String response, Class<E> contentClass) {

        /*
           We are getting )]}',\n extra characters in response, we need to clean them first and then
           we can serialize the JSON string into an object
        */
        response = response.substring(response.indexOf("\n") + 1);

        try {
            E realResponse = new ObjectMapper().readValue(response, contentClass);
            return realResponse;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
