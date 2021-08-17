package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab;

import java.util.Date;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.payment.DateValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.InsufficientFundsException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SbabConstants.ErrorMessage;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SbabConstants.Format;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SbabConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SbabConstants.IdTags;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SbabConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SbabConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SbabConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SbabConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.authenticator.rpc.BankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.authenticator.rpc.DecoupledResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.authenticator.rpc.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.configuration.SbabConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.fetcher.transactionalaccount.rpc.FetchCustomerResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.util.Utils;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.signableoperation.enums.InternalStatus;

public final class SbabApiClient {

    private final TinkHttpClient client;
    private String redirectUrl;
    private final SessionStorage sessionStorage;

    public SbabApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    protected void setConfiguration(AgentConfiguration<SbabConfiguration> agentConfiguration) {
        this.redirectUrl = agentConfiguration.getRedirectUrl();
    }

    private String getRedirectUrl() {
        return Optional.ofNullable(redirectUrl)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        SbabConstants.ErrorMessages.INVALID_CONFIGURATION));
    }

    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    public FetchAccountResponse fetchAccounts() {
        return client.request(Urls.ACCOUNTS)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.AUTHORIZATION, getToken().getAccessToken())
                .get(FetchAccountResponse.class);
    }

    public FetchCustomerResponse fetchCustomer() {
        return client.request(Urls.CUSTOMERS)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.AUTHORIZATION, getToken().getAccessToken())
                .get(FetchCustomerResponse.class);
    }

    private OAuth2Token getToken() {
        return sessionStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException("Expected token to be present"));
    }

    public FetchTransactionsResponse fetchTransactions(
            TransactionalAccount account, Date startDate, Date endDate) {
        return client.request(
                        Urls.TRANSACTIONS.parameter(
                                IdTags.ACCOUNT_NUMBER,
                                account.getFromTemporaryStorage(StorageKeys.ACCOUNT_NUMBER)))
                .queryParam(
                        QueryKeys.END_DATE,
                        Utils.formatDateTime(endDate, Format.TIMESTAMP, Format.TIMEZONE))
                .queryParam(
                        QueryKeys.START_DATE,
                        Utils.formatDateTime(startDate, Format.TIMESTAMP, Format.TIMEZONE))
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.AUTHORIZATION, getToken().getAccessToken())
                .get(FetchTransactionsResponse.class);
    }

    public CreatePaymentResponse createPayment(
            CreatePaymentRequest createPaymentRequest, String debtorAccountNumber)
            throws PaymentException {
        try {
            return createRequest(
                            Urls.INITIATE_PAYMENT.parameter(
                                    IdTags.ACCOUNT_NUMBER, debtorAccountNumber))
                    .header(HeaderKeys.AUTHORIZATION, getToken().getAccessToken())
                    .post(CreatePaymentResponse.class, createPaymentRequest);
        } catch (HttpResponseException e) {
            handleHttpResponseException(e);
            throw e;
        }
    }

    public GetPaymentResponse getPayment(String transferId, String debtorId)
            throws PaymentException {
        try {
            return createRequest(
                            Urls.GET_PAYMENT
                                    .parameter(IdTags.ACCOUNT_NUMBER, debtorId)
                                    .parameter(IdTags.PAYMENT_ID, transferId))
                    .header(HeaderKeys.AUTHORIZATION, getToken().getAccessToken())
                    .get(GetPaymentResponse.class);
        } catch (HttpResponseException e) {
            handleHttpResponseException(e);
            throw e;
        }
    }

    public BankIdResponse authorizeBankId(String ssn) {
        return client.request(Urls.AUTHORIZATION)
                .header(HeaderKeys.PSU_IP_ADDRESS, "")
                .queryParam(QueryKeys.USER_ID, ssn)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(BankIdResponse.class);
    }

    public BankIdResponse authenticateBankId(String ssn) {
        return client.request(Urls.AUTHENTICATION)
                .header(HeaderKeys.PSU_IP_ADDRESS, "")
                .queryParam(QueryKeys.USER_ID, ssn)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(BankIdResponse.class);
    }

    public DecoupledResponse getDecoupled(String code) {
        TokenRequest tokenRequest =
                new TokenRequest(getRedirectUrl(), code, QueryValues.PENDING_AUTHORIZATION_CODE);
        return client.request(Urls.TOKEN)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(DecoupledResponse.class, tokenRequest.toData());
    }

    public DecoupledResponse refreshAccessToken(String refreshToken) {
        RefreshTokenRequest tokenRequest =
                new RefreshTokenRequest(QueryValues.REFRESH_TOKEN, refreshToken);
        return client.request(Urls.TOKEN)
                .header(HeaderKeys.PSU_IP_ADDRESS, "")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(DecoupledResponse.class, tokenRequest.toData());
    }

    private void handleHttpResponseException(HttpResponseException httpResponseException)
            throws PaymentException {
        HttpResponse response = httpResponseException.getResponse();
        ErrorResponse errorResponse =
                httpResponseException.getResponse().getBody(ErrorResponse.class);
        switch (response.getStatus()) {
            case HttpStatus.SC_BAD_REQUEST:
                if (errorResponse.isInvalidDateError()) {
                    throw new DateValidationException(
                            ErrorMessage.INVALID_DATE,
                            "",
                            InternalStatus.INVALID_DUE_DATE,
                            new IllegalArgumentException());
                }
                if (errorResponse.isAmountExceedsCurrentBalance()) {
                    throw new InsufficientFundsException(
                            ErrorMessage.AMOUNT_EXCEEDS_BALANCE, InternalStatus.INSUFFICIENT_FUNDS);
                }
                break;
            case HttpStatus.SC_CONFLICT:
                if (errorResponse.isAmountLimitReached()) {
                    throw new PaymentValidationException(
                            ErrorMessage.AMOUNT_LIMIT_REACHED,
                            InternalStatus.TRANSFER_LIMIT_REACHED);
                }
                break;
            case HttpStatus.SC_FORBIDDEN:
                if (errorResponse.isFailedSignature()) {
                    throw new PaymentAuthorizationException(
                            ErrorMessage.SIGNATURE_FAILED, InternalStatus.PAYMENT_SIGNATURE_FAILED);
                }
                if (errorResponse.isForbiddenWithNoErrorCode()) {
                    throw new PaymentException(
                            ErrorMessage.FORBIDDEN, InternalStatus.USER_UNAUTHORIZED);
                }
                break;
            default:
                throw httpResponseException;
        }
    }
}
