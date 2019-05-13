package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30;

import io.vavr.control.Option;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.authenticator.rpc.BankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.authenticator.rpc.ResultBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.entities.Form;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.BankPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.CompleteTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.ConfirmTransferRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.ConfirmTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.InternalBankTransferRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.InternalBankTransferResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.PaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.RecipientRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.RecipientResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.ResultSignResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.SignatureRequest;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc.SignatureResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.creditcard.rpc.FetchCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.creditcard.rpc.FetchCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.einvoice.entities.EInvoiceEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.einvoice.rpc.FetchEInvoiceResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.investment.rpc.FetchInvestmentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.loan.rpc.FetchLoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.loan.rpc.FetchLoanResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount.rpc.FetchAccountTransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transfer.rpc.FetchBeneficiariesResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordeaSEApiClient {
    private final TinkHttpClient httpClient;
    private final SessionStorage sessionStorage;

    public NordeaSEApiClient(TinkHttpClient httpClient, SessionStorage sessionStorage) {
        this.httpClient = httpClient;
        this.sessionStorage = sessionStorage;
    }

    public BankIdResponse formInitBankIdLogin(String ssn) {
        Form formBuilder = new Form(NordeaSEConstants.DEFAULT_FORM_PARAMS);
        formBuilder.put(NordeaSEConstants.FormParams.USERNAME, ssn);

        return sendBankIdRequest(formBuilder);
    }

    public ResultBankIdResponse formPollBankIdLogin(BankIdResponse response, String ssn) {
        Form formBuilder = new Form(NordeaSEConstants.DEFAULT_FORM_PARAMS);
        formBuilder.put(NordeaSEConstants.FormParams.CODE, response.getCode());
        formBuilder.put(NordeaSEConstants.FormParams.USERNAME, ssn);

        return getAccessToken(formBuilder);
    }

    private BankIdResponse sendBankIdRequest(Form form) {
        return httpClient
                .request(NordeaSEConstants.Urls.LOGIN_BANKID)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(form, MediaType.APPLICATION_FORM_URLENCODED)
                .post(BankIdResponse.class);
    }

    private ResultBankIdResponse getAccessToken(Form form) {
        return httpClient
                .request(NordeaSEConstants.Urls.LOGIN_BANKID)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .body(form, MediaType.APPLICATION_FORM_URLENCODED)
                .post(ResultBankIdResponse.class);
    }

    public FetchAccountResponse fetchAccount() {
        final RequestBuilder request =
                httpClient
                        .request(NordeaSEConstants.Urls.FETCH_ACCOUNTS)
                        .accept(MediaType.APPLICATION_JSON_TYPE);

        return requestRefreshableGet(request, FetchAccountResponse.class);
    }

    public FetchAccountTransactionResponse fetchAccountTransactions(
            int offset, int limit, String accountId) {
        final URL url =
                NordeaSEConstants.Urls.FETCH_ACCOUNT_TRANSACTIONS
                        .parameter(NordeaSEConstants.IdTags.ACCOUNT_NUMBER, accountId)
                        .queryParam(NordeaSEConstants.QueryParams.OFFSET, Integer.toString(offset))
                        .queryParam(NordeaSEConstants.QueryParams.LIMIT, Integer.toString(limit));

        final RequestBuilder request =
                httpClient.request(url).accept(MediaType.APPLICATION_JSON_TYPE);

        return requestRefreshableGet(request, FetchAccountTransactionResponse.class);
    }

    public FetchCardsResponse fetchCards() {
        final RequestBuilder request =
                httpClient
                        .request(NordeaSEConstants.Urls.FETCH_CARDS)
                        .accept(MediaType.APPLICATION_JSON_TYPE);

        return requestRefreshableGet(request, FetchCardsResponse.class);
    }

    public FetchCardTransactionsResponse fetchCardTransactions(int page, String accountId) {
        final URL url =
                NordeaSEConstants.Urls.FETCH_CARD_TRANSACTIONS
                        .parameter(NordeaSEConstants.IdTags.CARD_ID, accountId)
                        .queryParam(NordeaSEConstants.QueryParams.PAGE, Integer.toString(page))
                        .queryParam(
                                NordeaSEConstants.QueryParams.PAGE_SIZE,
                                NordeaSEConstants.QueryParams.PAGE_SIZE_LIMIT);

        final RequestBuilder request =
                httpClient.request(url).accept(MediaType.APPLICATION_JSON_TYPE);

        return requestRefreshableGet(request, FetchCardTransactionsResponse.class);
    }

    public FetchInvestmentResponse fetchInvestments() {
        final RequestBuilder request =
                httpClient
                        .request(NordeaSEConstants.Urls.FETCH_INVESTMENTS)
                        .accept(MediaType.APPLICATION_JSON_TYPE);

        return requestRefreshableGet(request, FetchInvestmentResponse.class);
    }

    public FetchLoanResponse fetchLoans() {
        final RequestBuilder request = httpClient.request(NordeaSEConstants.Urls.FETCH_LOANS);

        return requestRefreshableGet(request, FetchLoanResponse.class);
    }

    public FetchLoanDetailsResponse fetchLoanDetails(String accountId) {
        final RequestBuilder request =
                httpClient.request(
                        NordeaSEConstants.Urls.FETCH_LOAN_DETAILS.parameter(
                                NordeaSEConstants.IdTags.LOAN_ID, accountId));

        return requestRefreshableGet(request, FetchLoanDetailsResponse.class);
    }

    public FetchEInvoiceResponse fetchEInvoice() {
        final URL url =
                NordeaSEConstants.Urls.FETCH_EINVOICES.queryParam(
                        NordeaSEConstants.QueryParams.STATUS,
                        NordeaSEConstants.QueryParams.STATUS_VALUES);

        final RequestBuilder request =
                httpClient.request(url).accept(MediaType.APPLICATION_JSON_TYPE);

        return requestRefreshableGet(request, FetchEInvoiceResponse.class);
    }

    public EInvoiceEntity fetchEInvoiceDetails(String eInvoiceId) {
        final RequestBuilder request =
                httpClient
                        .request(
                                NordeaSEConstants.Urls.FETCH_EINVOICES_DETAILS.parameter(
                                        NordeaSEConstants.IdTags.EINVOICE_ID, eInvoiceId))
                        .accept(MediaType.APPLICATION_JSON_TYPE);

        return requestRefreshableGet(request, EInvoiceEntity.class);
    }

    public FetchBeneficiariesResponse fetchBeneficiaries() {
        final RequestBuilder request =
                httpClient
                        .request(NordeaSEConstants.Urls.FETCH_BENEFICIARIES)
                        .accept(MediaType.APPLICATION_JSON_TYPE);

        return requestRefreshableGet(request, FetchBeneficiariesResponse.class);
    }

    public InternalBankTransferResponse executeInternalBankTransfer(
            InternalBankTransferRequest transferRequest) {
        final RequestBuilder request =
                httpClient
                        .request(NordeaSEConstants.Urls.EXECUTE_TRANSFER)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .body(transferRequest, MediaType.APPLICATION_JSON_TYPE);

        return requestRefreshablePost(request, InternalBankTransferResponse.class);
    }

    public BankPaymentResponse executeBankPayment(PaymentRequest transferRequest) {
        final RequestBuilder request =
                httpClient
                        .request(NordeaSEConstants.Urls.EXECUTE_TRANSFER)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .body(transferRequest, MediaType.APPLICATION_JSON_TYPE);

        return requestRefreshablePost(request, BankPaymentResponse.class);
    }

    public ConfirmTransferResponse confirmBankTransfer(
            ConfirmTransferRequest confirmTransferRequest) {
        final RequestBuilder request =
                httpClient
                        .request(NordeaSEConstants.Urls.CONFIRM_TRANSFER)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .body(confirmTransferRequest, MediaType.APPLICATION_JSON_TYPE);

        return requestRefreshablePost(request, ConfirmTransferResponse.class);
    }

    public SignatureResponse signTransfer(SignatureRequest signatureRequest) {
        final RequestBuilder request =
                httpClient
                        .request(NordeaSEConstants.Urls.SIGN_TRANSFER)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .body(signatureRequest, MediaType.APPLICATION_JSON_TYPE);

        return requestRefreshablePost(request, SignatureResponse.class);
    }

    public ResultSignResponse pollSignTransfer(String orderRef, int pollSequence) {
        final URL url =
                NordeaSEConstants.Urls.POLL_SIGN_TRANSFER
                        .parameter(NordeaSEConstants.IdTags.ORDER_REF, orderRef)
                        .queryParam(
                                NordeaSEConstants.QueryParams.POLLING_SEQUENCE,
                                Integer.toString(pollSequence));

        final RequestBuilder request =
                httpClient.request(url).accept(MediaType.APPLICATION_JSON_TYPE);

        return requestRefreshableGet(request, ResultSignResponse.class);
    }

    public RecipientResponse registerRecipient(RecipientRequest recipientRequest) {
        final RequestBuilder request =
                httpClient
                        .request(NordeaSEConstants.Urls.FETCH_BENEFICIARIES)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .body(recipientRequest, MediaType.APPLICATION_JSON_TYPE);

        return requestRefreshablePost(request, RecipientResponse.class);
    }

    public CompleteTransferResponse completeTransfer(String orderRef) {
        final RequestBuilder request =
                httpClient
                        .request(
                                NordeaSEConstants.Urls.COMPLETE_TRANSFER.parameter(
                                        NordeaSEConstants.IdTags.ORDER_REF, orderRef))
                        .accept(MediaType.APPLICATION_JSON_TYPE);

        return requestRefreshablePost(request, CompleteTransferResponse.class);
    }

    public void deleteTransfer(String transferId) {
        try {
            httpClient
                    .request(
                            NordeaSEConstants.Urls.FETCH_EINVOICES_DETAILS.parameter(
                                    NordeaSEConstants.IdTags.EINVOICE_ID, transferId))
                    .header(HttpHeaders.AUTHORIZATION, getTokenType() + ' ' + getAccessToken())
                    .header(HttpHeaders.ACCEPT_LANGUAGE, NordeaSEConstants.HeaderParams.LANGUAGE)
                    .delete();
        } catch (HttpResponseException hre) {
            tryRefreshAccessToken(hre);
        }
        deleteTransfer(transferId);
    }

    public void keepAlive() throws SessionException {
        try {
            Option.of(getRefreshToken())
                    .peek(this::refreshAccessToken)
                    .getOrElseThrow(SessionError.SESSION_EXPIRED::exception);
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
                    .header(HttpHeaders.ACCEPT_LANGUAGE, NordeaSEConstants.HeaderParams.LANGUAGE)
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

    private <T> T requestRefreshablePost(RequestBuilder request, Class<T> responseType) {
        try {
            return request.header(
                            HttpHeaders.AUTHORIZATION, getTokenType() + ' ' + getAccessToken())
                    .header(HttpHeaders.ACCEPT_LANGUAGE, NordeaSEConstants.HeaderParams.LANGUAGE)
                    .post(responseType);

        } catch (HttpResponseException hre) {
            tryRefreshAccessToken(hre);
            // use the new access token
            request.overrideHeader(
                    HttpHeaders.AUTHORIZATION, getTokenType() + ' ' + getAccessToken());
        }

        // retry request with new access token
        return request.post(responseType);
    }

    private void tryRefreshAccessToken(HttpResponseException hre) {
        HttpResponse response = hre.getResponse();
        ErrorResponse error = response.getBody(ErrorResponse.class);

        if (error.tokenRequired() || error.isInvalidAccessToken()) {
            refreshAccessToken(getRefreshToken());
        } else {
            throw hre;
        }
    }

    private void refreshAccessToken(String refreshToken) {
        Form formBuilder = new Form(NordeaSEConstants.DEFAULT_FORM_PARAMS);
        formBuilder.put(
                NordeaSEConstants.FormParams.GRANT_TYPE,
                NordeaSEConstants.StorageKeys.REFRESH_TOKEN);
        formBuilder.put(NordeaSEConstants.StorageKeys.REFRESH_TOKEN, refreshToken);

        ResultBankIdResponse response = getAccessToken(formBuilder);
        response.storeTokens(sessionStorage);
    }

    public void logout() {
        Form formBuilder = new Form();
        formBuilder.put(NordeaSEConstants.FormParams.TOKEN, getAccessToken());
        formBuilder.put(
                NordeaSEConstants.FormParams.TOKEN_TYPE_HINT,
                NordeaSEConstants.FormParams.TOKEN_TYPE);

        httpClient
                .request(NordeaSEConstants.Urls.LOGOUT)
                .body(formBuilder, MediaType.APPLICATION_FORM_URLENCODED)
                .post();
    }

    private String getTokenType() {
        return sessionStorage.get(NordeaSEConstants.StorageKeys.TOKEN_TYPE);
    }

    private String getAccessToken() {
        return sessionStorage.get(NordeaSEConstants.StorageKeys.ACCESS_TOKEN);
    }

    private String getRefreshToken() {
        return sessionStorage.get(NordeaSEConstants.StorageKeys.REFRESH_TOKEN);
    }
}
