package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv;

import java.util.Date;

import java.util.Optional;
import org.apache.http.HttpHeaders;

import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities.CurrentUser;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc.*;
import se.tink.backend.aggregation.nxgen.http.Form;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class AsLhvApiClient {

    private final TinkHttpClient client;
    private final AsLhvSessionStorage storage;

    public AsLhvApiClient(final TinkHttpClient client,
                          final AsLhvSessionStorage storage) {
        this.client = client;
        this.storage = storage;
    }

    private RequestBuilder getBaseRequest(URL url) {
        return client.request(url)
                .header(HttpHeaders.HOST, AsLhvConstants.URLS.BASE_URL);
    }

    private URL getUrl() {
        return new URL(String.format("https://%s%s", AsLhvConstants.URLS.BASE_URL, AsLhvConstants.URLS.SERVICE_ENDPOINT));
    }

    private URL getIsAuthenticatedUrl() {
        return new URL(getUrl() + AsLhvConstants.URLS.AUTH_IS_AUTHENTICATED_ENDPOINT);
    }

    private URL getLoginPasswordUrl() {
        return new URL(getUrl() + AsLhvConstants.URLS.AUTH_PASSWORD_ENDPOINT);
    }

    private URL getLogoutUrl() {
        return new URL(getUrl() + AsLhvConstants.URLS.AUTH_LOGOUT_ENDPOINT);
    }

    private URL getGetUserDataUrl() {
        return new URL(getUrl() + AsLhvConstants.URLS.GET_USER_DATA_ENDPOINT);
    }

    private URL getGetCurrenciesUrl() {
        return new URL(getUrl() + AsLhvConstants.URLS.GET_CURRENCIES_ENDPOINT);
    }

    private URL getAccountTransactionsUrl() {
        return new URL(getUrl() + AsLhvConstants.URLS.GET_ACCOUNT_TRANSACTIONS_ENDPOINT);
    }

    public AsLhvSessionStorage getSessionStorage() {
        return storage;
    }

    public IsAuthenticatedResponse isAuthenticated() throws HttpResponseException {
        RequestBuilder requestBuilder = getBaseRequest(getIsAuthenticatedUrl());
        requestBuilder.header(HttpHeaders.ACCEPT, AsLhvConstants.Header.ACCEPT_ALL);
        IsAuthenticatedResponse response = requestBuilder.post(IsAuthenticatedResponse.class);
        if (response.isAuthenticated()) {
            Optional<CurrentUser> currenUser = response.getCurrentUser();
            if (currenUser.isPresent()) {
                Optional<String> name = currenUser.get().getName();
                int baseCurrencyId = currenUser.get().getBaseCurrencyId();
                if (name.isPresent()) {
                    storage.setCurrentUser(name.get());
                }
                storage.setBaseCurrencyId(baseCurrencyId);
            }
        }
        return response;
    }

    public GetUserDataResponse getUserData() {
        GetUserDataResponse response = getBaseRequest(getGetUserDataUrl())
                .header(HttpHeaders.ACCEPT, AsLhvConstants.Header.ACCEPT_JSON)
                .header(HttpHeaders.ACCEPT_LANGUAGE, AsLhvConstants.Header.ACCEPT_LANGUAGE)
                .header(HttpHeaders.CONTENT_TYPE,  AsLhvConstants.Header.CONTENT_TYPE_FORM_URLENCODED)
                .header(AsLhvConstants.Header.LHV_APPLICATION_LANGUAGE_HEADER, AsLhvConstants.Header.LHV_APPLICATION_LANUGAGE_US)
                .post(GetUserDataResponse.class);
        if (response.requestSuccessful()) {
            storage.setUserData(response);
        }
        return response;
    }

    public LoginResponse login(String username, String password) throws HttpResponseException, HttpClientException {
        final Form form = new Form.Builder()
                .put(AsLhvConstants.Form.USERNAME_PARAMETER, username)
                .put(AsLhvConstants.Form.PASSWORD_PARAMETER, password)
                .build();
        String serialized = form.serialize();
        return getBaseRequest(getLoginPasswordUrl())
                .header(HttpHeaders.ACCEPT, AsLhvConstants.Header.ACCEPT_JSON)
                .header(HttpHeaders.ACCEPT_LANGUAGE, AsLhvConstants.Header.ACCEPT_LANGUAGE)
                .header(HttpHeaders.CONTENT_TYPE,  AsLhvConstants.Header.CONTENT_TYPE_FORM_URLENCODED)
                .header(AsLhvConstants.Header.LHV_APPLICATION_LANGUAGE_HEADER, AsLhvConstants.Header.LHV_APPLICATION_LANUGAGE_US)
                .body(serialized)
                .post(LoginResponse.class);
    }

    public GetCurrenciesResponse getCurrencies() {
        GetCurrenciesResponse response = getBaseRequest(getGetCurrenciesUrl())
                .header(HttpHeaders.ACCEPT, AsLhvConstants.Header.ACCEPT_JSON)
                .header(HttpHeaders.ACCEPT_LANGUAGE, AsLhvConstants.Header.ACCEPT_LANGUAGE)
                .header(HttpHeaders.CONTENT_TYPE,  AsLhvConstants.Header.CONTENT_TYPE_FORM_URLENCODED)
                .header(AsLhvConstants.Header.LHV_APPLICATION_LANGUAGE_HEADER, AsLhvConstants.Header.LHV_APPLICATION_LANUGAGE_US)
                .post(GetCurrenciesResponse.class);
        if (response.requestSuccessful()) {
            storage.setCurrencies(response);
        }
        return response;
    }

    public GetAccountTransactionsResponse getAccountTransactions(final String portfolioId,
                                                                 final Date fromDate,
                                                                 final Date toDate) {

        final Form form = new Form.Builder()
                .put(AsLhvConstants.Form.FROM_DATE, AsLhvConstants.DATE_FORMAT.format(fromDate))
                .put(AsLhvConstants.Form.TO_DATE, AsLhvConstants.DATE_FORMAT.format(toDate))
                .put(AsLhvConstants.Form.PORTFOLIO_ID, portfolioId)
                .build();

        GetAccountTransactionsResponse response = getBaseRequest(getAccountTransactionsUrl())
                .header(HttpHeaders.ACCEPT, AsLhvConstants.Header.ACCEPT_JSON)
                .header(HttpHeaders.ACCEPT_LANGUAGE, AsLhvConstants.Header.ACCEPT_LANGUAGE)
                .header(HttpHeaders.CONTENT_TYPE,  AsLhvConstants.Header.CONTENT_TYPE_FORM_URLENCODED)
                .header(AsLhvConstants.Header.LHV_APPLICATION_LANGUAGE_HEADER, AsLhvConstants.Header.LHV_APPLICATION_LANUGAGE_US)
                .body(form.serialize())
                .post(GetAccountTransactionsResponse.class);
        return response;
    }

    public LogoutResponse logout() {
        return getBaseRequest(getLogoutUrl())
                .header(HttpHeaders.ACCEPT, AsLhvConstants.Header.ACCEPT_JSON)
                .header(HttpHeaders.ACCEPT_LANGUAGE, AsLhvConstants.Header.ACCEPT_LANGUAGE)
                .header(HttpHeaders.CONTENT_TYPE, AsLhvConstants.Header.CONTENT_TYPE_FORM_URLENCODED)
                .post(LogoutResponse.class);
    }
}
