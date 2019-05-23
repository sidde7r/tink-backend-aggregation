package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.authenticator.rpc.AuthenticateResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.entities.Form;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.creditcard.rpc.FetchCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.creditcard.rpc.FetchCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.identitydata.rpc.CustomerInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.investment.rpc.FetchInvestmentResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.loan.rpc.FetchLoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.loan.rpc.FetchLoanResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.transactionalaccount.rpc.FetchTransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.transactionalaccount.rpc.FetchTransactionalAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordeaFIApiClient {
    private final TinkHttpClient httpClient;
    private final SessionStorage sessionStorage;

    public NordeaFIApiClient(TinkHttpClient httpClient, SessionStorage sessionStorage) {
        this.httpClient = httpClient;
        this.sessionStorage = sessionStorage;
    }

    public AuthenticateResponse initCodesAuthentication() throws HttpResponseException {
        Form formBuilder = new Form(NordeaFIConstants.DEFAULT_FORM_PARAMS);
        formBuilder.put(
                NordeaFIConstants.FormParams.USERNAME,
                sessionStorage.get(NordeaFIConstants.SessionStorage.USERNAME));

        return sendAuthenticateRequest(formBuilder);
    }

    public AuthenticateResponse pollCodesAuthentication(String reference)
            throws HttpResponseException {
        Form formBuilder = new Form(NordeaFIConstants.DEFAULT_FORM_PARAMS);
        formBuilder.put(
                NordeaFIConstants.FormParams.USERNAME,
                sessionStorage.get(NordeaFIConstants.SessionStorage.USERNAME));
        formBuilder.put(NordeaFIConstants.FormParams.CODE, reference);

        return sendAuthenticateRequest(formBuilder);
    }

    private AuthenticateResponse sendAuthenticateRequest(Form form) throws HttpResponseException {
        return httpClient
                .request(NordeaFIConstants.Urls.AUTHENTICATE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(form, MediaType.APPLICATION_FORM_URLENCODED)
                .post(AuthenticateResponse.class);
    }

    public FetchTransactionalAccountResponse fetchAccounts() {
        RequestBuilder request =
                httpClient
                        .request(NordeaFIConstants.Urls.FETCH_ACCOUNTS)
                        .accept(MediaType.APPLICATION_JSON_TYPE);
        return requestRefreshableGet(request, FetchTransactionalAccountResponse.class);
    }

    public FetchTransactionResponse fetchTransactions(int offset, int limit, String accountId) {
        RequestBuilder request =
                httpClient
                        .request(
                                NordeaFIConstants.Urls.FETCH_ACCOUNTS
                                        .concat(
                                                accountId
                                                        + NordeaFIConstants.ApiService
                                                                .FETCH_TRANSACTIONS)
                                        .queryParam(
                                                NordeaFIConstants.QueryParams.OFFSET,
                                                Integer.toString(offset))
                                        .queryParam(
                                                NordeaFIConstants.QueryParams.LIMIT,
                                                Integer.toString(limit)))
                        .accept(MediaType.APPLICATION_JSON_TYPE);
        return requestRefreshableGet(request, FetchTransactionResponse.class);
    }

    public FetchCardsResponse fetchCards() {
        RequestBuilder request =
                httpClient
                        .request(NordeaFIConstants.Urls.FETCH_CARDS)
                        .accept(MediaType.APPLICATION_JSON_TYPE);
        return requestRefreshableGet(request, FetchCardsResponse.class);
    }

    public FetchCardTransactionsResponse fetchCardTransactions(int page, String accountId) {
        RequestBuilder request =
                httpClient
                        .request(
                                NordeaFIConstants.Urls.FETCH_CARDS
                                        .concat(
                                                accountId
                                                        + NordeaFIConstants.ApiService
                                                                .FETCH_TRANSACTIONS)
                                        .queryParam(
                                                NordeaFIConstants.QueryParams.PAGE,
                                                Integer.toString(page))
                                        .queryParam(
                                                NordeaFIConstants.QueryParams.PAGE_SIZE,
                                                NordeaFIConstants.QueryParams.PAGE_SIZE_LIMIT))
                        .accept(MediaType.APPLICATION_JSON_TYPE);
        return requestRefreshableGet(request, FetchCardTransactionsResponse.class);
    }

    public FetchInvestmentResponse fetchInvestments() {
        RequestBuilder request =
                httpClient
                        .request(NordeaFIConstants.Urls.FETCH_INVESTMENTS)
                        .accept(MediaType.APPLICATION_JSON_TYPE);
        return requestRefreshableGet(request, FetchInvestmentResponse.class);
    }

    public FetchLoanResponse fetchLoans() {
        RequestBuilder request = httpClient.request(NordeaFIConstants.Urls.FETCH_LOANS);
        return requestRefreshableGet(request, FetchLoanResponse.class);
    }

    public FetchLoanDetailsResponse fetchLoanDetails(String accountId) {
        RequestBuilder request =
                httpClient.request(NordeaFIConstants.Urls.FETCH_LOANS.concat(accountId));
        return requestRefreshableGet(request, FetchLoanDetailsResponse.class);
    }

    public void keepAlive() throws SessionException {
        try {
            String refreshToken = getRefreshToken();

            if (refreshToken != null) {
                refreshAccessToken(refreshToken);
            } else {
                throw SessionError.SESSION_EXPIRED.exception();
            }
        } catch (HttpResponseException hre) {

            ErrorResponse error = hre.getResponse().getBody(ErrorResponse.class);
            if (error.isInvalidRefreshToken()) {
                throw SessionError.SESSION_EXPIRED.exception();
            }

            throw hre;
        }
    }

    // Nordeas short lived access tokens requires us to sometimes have to refresh the
    // access token during a request. This method should be used by all data fetching calls
    private <T> T requestRefreshableGet(RequestBuilder request, Class<T> responseType) {
        try {
            return request.header(
                            HttpHeaders.AUTHORIZATION, getTokenType() + ' ' + getAccessToken())
                    .header(HttpHeaders.ACCEPT_LANGUAGE, NordeaFIConstants.HeaderParams.LANGUAGE)
                    .get(responseType);

        } catch (HttpResponseException hre) {
            tryRefreshAccessToken(hre);
            // use the new access token
            request.overrideHeader(
                    HttpHeaders.AUTHORIZATION, getTokenType() + ' ' + getAccessToken());
        }

        // retry request with new access token
        return request.get(responseType);
    }

    private void tryRefreshAccessToken(HttpResponseException hre) {
        HttpResponse response = hre.getResponse();
        ErrorResponse error = response.getBody(ErrorResponse.class);

        if (error.isInvalidAccessToken()) {
            refreshAccessToken(getRefreshToken());
        } else {
            throw hre;
        }
    }

    private void refreshAccessToken(String refreshToken) {
        Form formBuilder = new Form(NordeaFIConstants.DEFAULT_FORM_PARAMS);
        formBuilder.put(
                NordeaFIConstants.FormParams.GRANT_TYPE,
                NordeaFIConstants.SessionStorage.REFRESH_TOKEN);
        formBuilder.put(NordeaFIConstants.SessionStorage.REFRESH_TOKEN, refreshToken);

        AuthenticateResponse response = sendAuthenticateRequest(formBuilder);
        response.storeTokens(sessionStorage);
    }

    public void logout() {
        Form formBuilder = new Form();
        formBuilder.put(NordeaFIConstants.FormParams.TOKEN, getAccessToken());
        formBuilder.put(NordeaFIConstants.FormParams.TOKEN_TYPE_HINT, "access_token");

        httpClient
                .request(NordeaFIConstants.Urls.LOGOUT)
                .body(formBuilder, MediaType.APPLICATION_FORM_URLENCODED)
                .post();
    }

    private String getTokenType() {
        return sessionStorage.get(NordeaFIConstants.SessionStorage.TOKEN_TYPE);
    }

    private String getAccessToken() {
        return sessionStorage.get(NordeaFIConstants.SessionStorage.ACCESS_TOKEN);
    }

    private String getRefreshToken() {
        return sessionStorage.get(NordeaFIConstants.SessionStorage.REFRESH_TOKEN);
    }

    public CustomerInfoResponse fetchCustomerInfo() {
        RequestBuilder request =
                httpClient
                        .request(NordeaFIConstants.Urls.FETCH_CUSTOMER_INFO)
                        .accept(MediaType.APPLICATION_JSON_TYPE);
        return requestRefreshableGet(request, CustomerInfoResponse.class);
    }
}
