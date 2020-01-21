package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.QueryValues.SCOPE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.QueryValues.SCOPE_WITHOUT_PAYMENT;

import java.util.List;
import java.util.Optional;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.GetTokenForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.GetTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.rpc.RefreshTokenForm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.configuration.NordeaBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.ConfirmPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.rpc.GetPaymentsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.filters.BankSideFailureFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.rpc.NordeaErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.filter.filters.ServiceUnavailableBankServiceErrorFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.payment.enums.PaymentType;

public class NordeaBaseApiClient implements TokenInterface {
    protected final TinkHttpClient client;
    protected final PersistentStorage persistentStorage;
    protected NordeaBaseConfiguration configuration;

    public NordeaBaseApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;

        this.client.addFilter(new BankSideFailureFilter());
        this.client.addFilter(new ServiceUnavailableBankServiceErrorFilter());
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
        OAuth2Token token = getStoredToken();
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
                                        NordeaBaseConstants.QueryValues.DURATION_MINUTES)
                                .queryParam(NordeaBaseConstants.QueryKeys.COUNTRY, country)
                                .queryParam(NordeaBaseConstants.QueryKeys.SCOPE, getScopes())
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

    public OAuth2Token refreshToken(String refreshToken) {
        return createTokenRequest()
                .body(RefreshTokenForm.of(refreshToken), MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(GetTokenResponse.class)
                .toTinkToken();
    }

    public GetAccountsResponse getAccounts() {
        return requestRefreshableGet(
                createRequestInSession(NordeaBaseConstants.Urls.GET_ACCOUNTS),
                GetAccountsResponse.class);
    }

    public GetAccountsResponse getCorporateAccounts() {
        return createRequestInSession(Urls.GET_CORPORATE_ACCOUNTS).get(GetAccountsResponse.class);
    }

    public GetTransactionsResponse getCorporateTransactions(
            TransactionalAccount account, String key) {
        URL url =
                Optional.ofNullable(key)
                        .map(k -> new URL(Urls.BASE_CORPORATE_URL + k))
                        .orElse(
                                NordeaBaseConstants.Urls.GET_CORPORATE_TRANSACTIONS.parameter(
                                        NordeaBaseConstants.IdTags.ACCOUNT_ID,
                                        account.getApiIdentifier()));

        return createRequestInSession(url).get(GetTransactionsResponse.class);
    }

    public GetTransactionsResponse getTransactions(TransactionalAccount account, String key) {
        URL url =
                Optional.ofNullable(key)
                        .map(k -> new URL(NordeaBaseConstants.Urls.BASE_URL + k))
                        .orElse(
                                NordeaBaseConstants.Urls.GET_TRANSACTIONS.parameter(
                                        NordeaBaseConstants.IdTags.ACCOUNT_ID,
                                        account.getApiIdentifier()));

        RequestBuilder request = createRequestInSession(url);
        return requestRefreshableGet(request, GetTransactionsResponse.class);
    }

    @Override
    public void storeToken(OAuth2Token token) {
        persistentStorage.rotateStorageValue(
                OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN, token);
    }

    @Override
    public OAuth2Token getStoredToken() {
        return persistentStorage
                .get(OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new IllegalStateException("Cannot find token!"));
    }

    public CreatePaymentResponse createPayment(
            CreatePaymentRequest createPaymentRequest, PaymentType paymentType)
            throws PaymentException {
        try {
            return createRequestInSession(
                            NordeaBaseConstants.Urls.INITIATE_PAYMENT.parameter(
                                    NordeaBaseConstants.IdTags.PAYMENT_TYPE,
                                    paymentType.toString()))
                    .post(CreatePaymentResponse.class, createPaymentRequest);
        } catch (HttpResponseException e) {
            handleHttpPisResponseException(e);
            throw e;
        }
    }

    public ConfirmPaymentResponse confirmPayment(String paymentId, PaymentType paymentType)
            throws PaymentException {
        try {
            return createRequestInSession(
                            NordeaBaseConstants.Urls.CONFIRM_PAYMENT
                                    .parameter(
                                            NordeaBaseConstants.IdTags.PAYMENT_TYPE,
                                            paymentType.toString())
                                    .parameter(NordeaBaseConstants.IdTags.PAYMENT_ID, paymentId))
                    .put(ConfirmPaymentResponse.class);
        } catch (HttpResponseException e) {
            handleHttpPisResponseException(e);
            throw e;
        }
    }

    public GetPaymentResponse getPayment(String paymentId, PaymentType paymentType)
            throws PaymentException {
        try {
            return createRequestInSession(
                            NordeaBaseConstants.Urls.GET_PAYMENT
                                    .parameter(
                                            NordeaBaseConstants.IdTags.PAYMENT_TYPE,
                                            paymentType.toString())
                                    .parameter(NordeaBaseConstants.IdTags.PAYMENT_ID, paymentId))
                    .get(GetPaymentResponse.class);
        } catch (HttpResponseException e) {
            handleHttpPisResponseException(e);
            throw e;
        }
    }

    public GetPaymentsResponse fetchPayments(PaymentType paymentType) throws PaymentException {
        try {
            return createRequestInSession(
                            NordeaBaseConstants.Urls.GET_PAYMENTS.parameter(
                                    NordeaBaseConstants.IdTags.PAYMENT_TYPE,
                                    paymentType.toString()))
                    .get(GetPaymentsResponse.class);
        } catch (HttpResponseException e) {
            handleHttpPisResponseException(e);
            throw e;
        }
    }

    private void handleHttpPisResponseException(HttpResponseException httpResponseException)
            throws PaymentException {
        if (httpResponseException.getResponse().hasBody()) {
            try {
                httpResponseException
                        .getResponse()
                        .getBody(NordeaErrorResponse.class)
                        .checkPisError(httpResponseException);
            } catch (HttpClientException | HttpResponseException d) {
                throw httpResponseException;
            }
        }
    }

    protected <T> T requestRefreshableGet(RequestBuilder request, Class<T> responseType) {
        try {
            return request.get(responseType);

        } catch (HttpResponseException hre) {

            validateIsAccessTokenEpiredOrElseThrow(hre);

            OAuth2Token oAuth2Token =
                    this.refreshToken(
                            getStoredToken()
                                    .getRefreshToken()
                                    .orElseThrow(
                                            () ->
                                                    new IllegalStateException(
                                                            "Could not find refresh token to refresh.",
                                                            hre)));
            this.storeToken(oAuth2Token);

            request.overrideHeader(HttpHeaders.AUTHORIZATION, oAuth2Token.toAuthorizeHeader());
            // retry request with new token
            return requestRefreshableGet(request, responseType);
        }
    }

    private void validateIsAccessTokenEpiredOrElseThrow(HttpResponseException hre) {
        HttpResponse response = hre.getResponse();
        if (response.getStatus() == HttpStatus.SC_UNAUTHORIZED && response.hasBody()) {
            String body = response.getBody(String.class);
            if (!Strings.isNullOrEmpty(body)
                    && body.toLowerCase()
                            .contains(NordeaBaseConstants.ErrorMessages.TOKEN_EXPIRED.toLowerCase())
                    && body.toLowerCase()
                            .contains(NordeaBaseConstants.ErrorCodes.TOKEN_EXPIRED.toLowerCase())) {
                return;
            }
        }
        throw hre;
    }

    private String getScopes() {
        List<String> scopes = configuration.getScopes();
        if (scopes.stream()
                .allMatch(scope -> NordeaBaseConstants.Scopes.AIS.equalsIgnoreCase(scope))) {
            // Return only AIS scopes
            return SCOPE_WITHOUT_PAYMENT;
        } else if (scopes.stream()
                .allMatch(
                        scope ->
                                NordeaBaseConstants.Scopes.AIS.equalsIgnoreCase(scope)
                                        || NordeaBaseConstants.Scopes.PIS.equalsIgnoreCase(
                                                scope))) {
            // Return AIS + PIS scopes
            return SCOPE;
        } else {
            throw new IllegalArgumentException(
                    String.format(
                            "%s contain invalid scope(s), only support scopes AIS and PIS",
                            scopes.toString()));
        }
    }
}
