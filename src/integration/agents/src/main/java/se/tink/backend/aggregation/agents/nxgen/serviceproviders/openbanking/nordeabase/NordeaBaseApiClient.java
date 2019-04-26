package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase;

import java.util.Optional;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.GetTokenForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.GetTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.RefreshTokenForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.configuration.NordeaBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.*;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.rpc.NordeaErrorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordeaBaseApiClient {
    protected final TinkHttpClient client;
    protected final SessionStorage sessionStorage;
    protected NordeaBaseConfiguration configuration;

    public NordeaBaseApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public NordeaBaseConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        NordeaBaseConstants.ErrorMessages.MISSING_CONFIGURATION));
    }

    public void setConfiguration(NordeaBaseConfiguration configuration) {
        this.configuration = configuration;
    }

    protected RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .header(NordeaBaseConstants.QueryKeys.X_CLIENT_ID, configuration.getClientId())
                .header(
                        NordeaBaseConstants.QueryKeys.X_CLIENT_SECRET,
                        configuration.getClientSecret());
    }

    private RequestBuilder createRequestInSession(URL url) {
        OAuth2Token token = getTokenFromSession();
        return createRequest(url)
                .header(
                        NordeaBaseConstants.HeaderKeys.AUTHORIZATION,
                        token.getTokenType() + " " + token.getAccessToken());
    }

    private RequestBuilder createTokenRequest() {
        return createRequest(NordeaBaseConstants.Urls.GET_TOKEN);
    }

    public URL getAuthorizeUrl(String state, String country) {
        return client.request(
                        NordeaBaseConstants.Urls.AUTHORIZE
                                .queryParam(
                                        NordeaBaseConstants.QueryKeys.CLIENT_ID,
                                        configuration.getClientId())
                                .queryParam(NordeaBaseConstants.QueryKeys.STATE, state)
                                .queryParam(
                                        NordeaBaseConstants.QueryKeys.DURATION,
                                        NordeaBaseConstants.QueryValues.DURATION)
                                .queryParam(NordeaBaseConstants.QueryKeys.COUNTRY, country)
                                .queryParam(
                                        NordeaBaseConstants.QueryKeys.SCOPE,
                                        NordeaBaseConstants.QueryValues.SCOPE)
                                .queryParam(
                                        NordeaBaseConstants.QueryKeys.REDIRECT_URI,
                                        configuration.getRedirectUrl()))
                .getUrl();
    }

    public OAuth2Token getToken(GetTokenForm form) {
        return createTokenRequest()
                .body(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(GetTokenResponse.class)
                .toTinkToken();
    }

    public OAuth2Token refreshToken(RefreshTokenForm form) {
        return createTokenRequest()
                .body(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(GetTokenResponse.class)
                .toTinkToken();
    }

    public GetAccountsResponse getAccounts() {
        return createRequestInSession(NordeaBaseConstants.Urls.GET_ACCOUNTS)
                .get(GetAccountsResponse.class);
    }

    public GetTransactionsResponse getTransactions(TransactionalAccount account, String key) {
        URL url =
                Optional.ofNullable(key)
                        .map(k -> new URL(NordeaBaseConstants.Urls.BASE_URL + k))
                        .orElse(
                                NordeaBaseConstants.Urls.GET_TRANSACTIONS.parameter(
                                        NordeaBaseConstants.IdTags.ACCOUNT_ID,
                                        account.getApiIdentifier()));

        return createRequestInSession(url).get(GetTransactionsResponse.class);
    }

    public void setTokenToSession(OAuth2Token accessToken) {
        sessionStorage.put(NordeaBaseConstants.StorageKeys.ACCESS_TOKEN, accessToken);
    }

    private OAuth2Token getTokenFromSession() {
        return sessionStorage
                .get(NordeaBaseConstants.StorageKeys.ACCESS_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException("Cannot find token!"));
    }

    public CreatePaymentResponse createPayment(CreatePaymentRequest createPaymentRequest)
            throws PaymentException {
        try {
            return createRequestInSession(NordeaBaseConstants.Urls.INITIATE_DOMESTIC_PAYMENT)
                    .post(CreatePaymentResponse.class, createPaymentRequest);
        } catch (HttpResponseException e) {
            handleHttpResponseException(e);
            throw e;
        }
    }

    public ConfirmPaymentResponse confirmPayment(String paymentId, boolean domestic)
            throws PaymentException {
        try {
            URL url =
                    domestic
                            ? NordeaBaseConstants.Urls.CONFIRM_DOMESTIC_PAYMENT.parameter(
                                    NordeaBaseConstants.IdTags.PAYMENT_ID, paymentId)
                            : NordeaBaseConstants.Urls.CONFIRM_SEPA_PAYMENT.parameter(
                                    NordeaBaseConstants.IdTags.PAYMENT_ID, paymentId);
            return createRequestInSession(url).put(ConfirmPaymentResponse.class);
        } catch (HttpResponseException e) {
            handleHttpResponseException(e);
            throw e;
        }
    }

    public GetPaymentResponse getPayment(String paymentId) throws PaymentException {
        try {
            return createRequestInSession(
                            NordeaBaseConstants.Urls.GET_DOMESTIC_PAYMENT.parameter(
                                    NordeaBaseConstants.IdTags.PAYMENT_ID, paymentId))
                    .get(GetPaymentResponse.class);
        } catch (HttpResponseException e) {
            handleHttpResponseException(e);
            throw e;
        }
    }

    public GetPaymentsResponse fetchPayments() throws PaymentException {
        try {
            return createRequestInSession(NordeaBaseConstants.Urls.GET_DOMESTIC_PAYMENTS)
                    .get(GetPaymentsResponse.class);
        } catch (HttpResponseException e) {
            handleHttpResponseException(e);
            throw e;
        }
    }

    private void handleHttpResponseException(HttpResponseException httpResponseException)
            throws PaymentException {
        if (httpResponseException.getResponse().hasBody()) {
            try {
                httpResponseException
                        .getResponse()
                        .getBody(NordeaErrorResponse.class)
                        .checkError(httpResponseException);
            } catch (HttpClientException | HttpResponseException d) {
                throw httpResponseException;
            }
        }
    }
}
