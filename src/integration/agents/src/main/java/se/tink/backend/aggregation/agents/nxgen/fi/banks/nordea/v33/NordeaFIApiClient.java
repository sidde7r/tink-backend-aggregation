package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33;

import static se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIConstants.AUTH_FORM_PARAMS;
import static se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIConstants.DEFAULT_FORM_PARAMS;

import com.google.common.base.Strings;
import javax.annotation.Nullable;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIConstants.FormParams;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIConstants.QueryParams;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.authenticator.rpc.AuthenticateCode;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.authenticator.rpc.AuthenticateRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.authenticator.rpc.AuthenticateResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.authenticator.rpc.AuthenticateTokenResponse;
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
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@Slf4j
@RequiredArgsConstructor
public class NordeaFIApiClient {
    private final TinkHttpClient httpClient;
    private final SessionStorage sessionStorage;

    public AuthenticateResponse initAuthentication(String username, String codeChallenge)
            throws HttpResponseException {
        AuthenticateRequest authenticateRequest =
                AuthenticateRequest.builder()
                        .redirectUri(DEFAULT_FORM_PARAMS.get(FormParams.REDIRECT_URI))
                        .scope(DEFAULT_FORM_PARAMS.get(FormParams.SCOPE))
                        .clientId(DEFAULT_FORM_PARAMS.get(FormParams.CLIENT_ID))
                        .userId(username)
                        .codeChallenge(codeChallenge)
                        .codeChallengeMethod(AUTH_FORM_PARAMS.get(FormParams.CODE_CHALLENGE_METHOD))
                        .responseType(AUTH_FORM_PARAMS.get(FormParams.RESPONSE_TYPE))
                        .build();

        return sendAuthenticateInitRequest(authenticateRequest);
    }

    public AuthenticateTokenResponse getAuthenticationToken(String reference, String codeVerifier)
            throws HttpResponseException {

        Form formBuilder = new Form();
        formBuilder.put(FormParams.CLIENT_ID, DEFAULT_FORM_PARAMS.get(FormParams.CLIENT_ID));
        formBuilder.put(FormParams.CODE, reference);
        formBuilder.put(FormParams.CODE_VERIFIER, codeVerifier);
        formBuilder.put(FormParams.GRANT_TYPE, AUTH_FORM_PARAMS.get(FormParams.GRANT_TYPE));
        formBuilder.put(FormParams.REDIRECT_URI, DEFAULT_FORM_PARAMS.get(FormParams.REDIRECT_URI));

        return httpClient
                .request(NordeaFIConstants.Urls.AUTHENTICATE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(formBuilder, MediaType.APPLICATION_FORM_URLENCODED)
                .post(AuthenticateTokenResponse.class);
    }

    public AuthenticateResponse getAuthenticationStatus(String sessionId)
            throws HttpResponseException {
        return httpClient
                .request(NordeaFIConstants.Urls.AUTHENTICATE_INIT.concatWithSeparator(sessionId))
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(AuthenticateResponse.class);
    }

    public void cancelAuthentication(String sessionId) throws HttpResponseException {
        httpClient
                .request(NordeaFIConstants.Urls.AUTHENTICATE_INIT.concatWithSeparator(sessionId))
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .delete();
    }

    private AuthenticateResponse sendAuthenticateInitRequest(
            AuthenticateRequest authenticateRequest) throws HttpResponseException {
        return httpClient
                .request(NordeaFIConstants.Urls.AUTHENTICATE_INIT)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(authenticateRequest, MediaType.APPLICATION_JSON_TYPE)
                .post(AuthenticateResponse.class);
    }

    public AuthenticateCode getAuthenticateCode(AuthenticateCode authenticateCode)
            throws HttpResponseException {
        return httpClient
                .request(NordeaFIConstants.Urls.AUTHENTICATE_CODE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(authenticateCode, MediaType.APPLICATION_JSON_TYPE)
                .post(AuthenticateCode.class);
    }

    public FetchTransactionalAccountResponse fetchAccounts() {
        RequestBuilder request =
                httpClient
                        .request(NordeaFIConstants.Urls.FETCH_ACCOUNTS)
                        .accept(MediaType.APPLICATION_JSON_TYPE);
        return requestRefreshableGet(request, FetchTransactionalAccountResponse.class);
    }

    public FetchTransactionResponse fetchTransactions(
            int limit, @Nullable String continuationKey, String accountId) {
        RequestBuilder request =
                httpClient
                        .request(
                                NordeaFIConstants.Urls.FETCH_ACCOUNTS
                                        .concat(
                                                accountId
                                                        + NordeaFIConstants.ApiService
                                                                .FETCH_TRANSACTIONS)
                                        .queryParam(
                                                NordeaFIConstants.QueryParams.LIMIT,
                                                Integer.toString(limit)))
                        .accept(MediaType.APPLICATION_JSON_TYPE);
        if (!Strings.isNullOrEmpty(continuationKey)) {
            request = request.queryParam(QueryParams.CONTINUATION_KEY, continuationKey);
        }
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
                throw SessionError.SESSION_EXPIRED.exception(hre);
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
            log.warn("[Nordea FI] Exception while calling Nordea API");
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
            log.info("[Nordea FI] Attempting to refresh access token");
            refreshAccessToken(getRefreshToken());
            log.info("[Nordea FI] Access token refreshed successfully");
        } else {
            throw hre;
        }
    }

    private void refreshAccessToken(String refreshToken) {
        Form formBuilder = new Form(DEFAULT_FORM_PARAMS);
        formBuilder.put(FormParams.GRANT_TYPE, NordeaFIConstants.SessionStorage.REFRESH_TOKEN);
        formBuilder.put(NordeaFIConstants.SessionStorage.REFRESH_TOKEN, refreshToken);

        AuthenticateTokenResponse response =
                httpClient
                        .request(NordeaFIConstants.Urls.AUTHENTICATE)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .body(formBuilder, MediaType.APPLICATION_FORM_URLENCODED)
                        .post(AuthenticateTokenResponse.class);
        response.storeTokens(sessionStorage);
    }

    public void logout() {
        Form formBuilder = new Form();
        formBuilder.put(FormParams.TOKEN, getAccessToken());
        formBuilder.put(FormParams.TOKEN_TYPE_HINT, "access_token");

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
