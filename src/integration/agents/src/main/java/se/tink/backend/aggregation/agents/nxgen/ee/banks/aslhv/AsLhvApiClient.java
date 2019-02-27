package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv;

import org.apache.http.HttpHeaders;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc.GetAccountTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc.GetCurrenciesResponse;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc.GetUserDataResponse;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc.IsAuthenticatedResponse;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc.LoginResponse;
import se.tink.backend.aggregation.nxgen.http.Form;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

import java.util.Date;

public class AsLhvApiClient {

    private final TinkHttpClient client;

    public AsLhvApiClient(final TinkHttpClient client) {
        this.client = client;
    }

    private RequestBuilder getBaseRequest(URL url) {
        return client.request(url)
                .header(HttpHeaders.HOST, AsLhvConstants.URLS.BASE_URL)
                .header(HttpHeaders.ACCEPT, AsLhvConstants.Header.ACCEPT_JSON)
                .header(HttpHeaders.ACCEPT_LANGUAGE, AsLhvConstants.Header.ACCEPT_LANGUAGE)
                .header(
                        HttpHeaders.CONTENT_TYPE,
                        AsLhvConstants.Header.CONTENT_TYPE_FORM_URLENCODED)
                .header(
                        AsLhvConstants.Header.LHV_APPLICATION_LANGUAGE_HEADER,
                        AsLhvConstants.Header.LHV_APPLICATION_LANUGAGE_US);
    }

    private URL getUrl() {
        return new URL(
                String.format(
                        "https://%s%s",
                        AsLhvConstants.URLS.BASE_URL, AsLhvConstants.URLS.SERVICE_ENDPOINT));
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

    public IsAuthenticatedResponse isAuthenticated() throws HttpResponseException {
        RequestBuilder requestBuilder = getBaseRequest(getIsAuthenticatedUrl());
        requestBuilder.header(HttpHeaders.ACCEPT, AsLhvConstants.Header.ACCEPT_ALL);
        return requestBuilder.post(IsAuthenticatedResponse.class);
    }

    public GetUserDataResponse getUserData() {
        return getBaseRequest(getGetUserDataUrl()).post(GetUserDataResponse.class);
    }

    public LoginResponse login(String username, String password)
            throws HttpResponseException, HttpClientException {
        final Form form =
                Form.builder()
                        .put(AsLhvConstants.Form.USERNAME_PARAMETER, username)
                        .put(AsLhvConstants.Form.PASSWORD_PARAMETER, password)
                        .build();
        String serialized = form.serialize();
        return getBaseRequest(getLoginPasswordUrl()).body(serialized).post(LoginResponse.class);
    }

    public GetCurrenciesResponse getCurrencies() {
        return getBaseRequest(getGetCurrenciesUrl()).post(GetCurrenciesResponse.class);
    }

    public GetAccountTransactionsResponse getAccountTransactions(
            final String portfolioId, final Date fromDate, final Date toDate) {

        final Form form =
                Form.builder()
                        .put(
                                AsLhvConstants.Form.FROM_DATE,
                                AsLhvConstants.DATE_FORMAT.format(fromDate))
                        .put(AsLhvConstants.Form.TO_DATE, AsLhvConstants.DATE_FORMAT.format(toDate))
                        .put(AsLhvConstants.Form.PORTFOLIO_ID, portfolioId)
                        .build();

        return getBaseRequest(getAccountTransactionsUrl())
                .body(form.serialize())
                .post(GetAccountTransactionsResponse.class);
    }

    public void logout() {
        getBaseRequest(getLogoutUrl()).post();
    }
}
