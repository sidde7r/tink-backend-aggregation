package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank;

import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.AuthorizationValues.MAX_CONSENT_LENGTH_IN_DAYS;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.AuthorizationValues.MAX_TRANSACTION_HISTORY_LENGTH_IN_DAYS;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import javax.ws.rs.core.MediaType;
import lombok.SneakyThrows;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.Filters;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.entities.TokenHeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc.AuthorizationRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc.AuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc.ExchangeTokenForm;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc.ExchangeTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc.RefreshTokenForm;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc.TokenForm;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.configuration.OpBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.executor.payment.rpc.SubmittedPayment;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.rpc.GetCreditCardTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.rpc.GetCreditCardsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceInternalErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.ServerErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.ServerErrorRetryFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.SslHandshakeRetryFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.TimeoutRetryFilter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.UserAvailability;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class OpBankApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private final OpBankConfiguration configuration;
    private final String redirectUrl;
    private final QsealcSigner qsealcSigner;
    private final String financialId;
    private final UserAvailability userAvailability;

    @SneakyThrows
    public OpBankApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            AgentConfiguration<OpBankConfiguration> agentConfiguration,
            QsealcSigner qsealcSigner,
            UserAvailability userAvailability) {
        this.client = client;
        configureClient();
        this.persistentStorage = persistentStorage;
        this.qsealcSigner = qsealcSigner;
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
        this.financialId =
                CertificateUtils.getOrganizationIdentifier(agentConfiguration.getQsealc());
        this.userAvailability = userAvailability;
    }

    private void configureClient() {
        this.client.addFilter(new ServerErrorFilter());
        this.client.addFilter(new BankServiceInternalErrorFilter());
        this.client.addFilter(
                new ServerErrorRetryFilter(Filters.NUMBER_OF_RETRIES, Filters.MS_TO_WAIT));
        this.client.addFilter(new TimeoutFilter());
        this.client.addFilter(
                new TimeoutRetryFilter(Filters.NUMBER_OF_RETRIES, Filters.MS_TO_WAIT));
        this.client.addFilter(
                new SslHandshakeRetryFilter(Filters.NUMBER_OF_RETRIES, Filters.MS_TO_WAIT));
    }

    public TokenResponse fetchNewToken(String scope) {
        try {
            return client.request(Urls.OAUTH_TOKEN)
                    .accept(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                    .body(
                            new TokenForm(scope)
                                    .setClientId(configuration.getClientId())
                                    .setClientSecret(configuration.getClientSecret()),
                            MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                    .post(HttpResponse.class)
                    .getBody(TokenResponse.class);
        } catch (HttpResponseException e) {
            throw mapServiceException(e);
        }
    }

    public OAuth2Token fetchRefreshToken(String refreshToken) {
        final String clientId = configuration.getClientId();
        final String clientSecret = configuration.getClientSecret();
        RefreshTokenForm refreshTokenForm =
                RefreshTokenForm.builder()
                        .setClientId(clientId)
                        .setClientSecret(clientSecret)
                        .setGrantType(OpBankConstants.RefreshTokenFormKeys.REFRESH_TOKEN)
                        .setRefreshToken(refreshToken)
                        .build();
        return client.request(Urls.OAUTH_TOKEN)
                .accept(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                .body(refreshTokenForm, MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(TokenResponse.class)
                .toTinkToken();
    }

    public AuthorizationResponse createNewAuthorization(String bearerToken) {
        HttpResponse response =
                client.request(Urls.ACCOUNTS_AUTHORIZATION)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .body(
                                AuthorizationRequest.builder()
                                        .daysOfTransactions(MAX_TRANSACTION_HISTORY_LENGTH_IN_DAYS)
                                        .daysToExpire(MAX_CONSENT_LENGTH_IN_DAYS)
                                        .build())
                        .header(HeaderKeys.X_API_KEY, configuration.getApiKey())
                        .header(HeaderKeys.X_FAPI_FINANCIAL_ID, financialId)
                        .header(HeaderKeys.AUTHORIZATION, "Bearer " + bearerToken)
                        .post(HttpResponse.class);

        return response.getBody(AuthorizationResponse.class);
    }

    public CreatePaymentResponse createNewPayment(
            String bearerToken, CreatePaymentRequest payment, String kid) {
        HttpResponse response =
                client.request(Urls.CREATE_SEPA_PAYMENT)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .header(HeaderKeys.X_JWS_SIGNATURE, createJwsHeader(payment, kid))
                        .header(HeaderKeys.X_IDEMPOTENCY_KEY, UUID.randomUUID().toString())
                        .header(HeaderKeys.X_API_KEY, configuration.getApiKey())
                        .header(HeaderKeys.X_FAPI_FINANCIAL_ID, financialId)
                        .header(HeaderKeys.AUTHORIZATION, "Bearer " + bearerToken)
                        .body(payment)
                        .post(HttpResponse.class);

        return response.getBody(CreatePaymentResponse.class);
    }

    public CreatePaymentResponse verifyPayment(OAuth2Token bearerToken, String paymentId) {
        HttpResponse response =
                client.request(Urls.VERIFY_SEPA_PAYMENT + paymentId)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .header(HeaderKeys.X_API_KEY, configuration.getApiKey())
                        .header(HeaderKeys.X_FAPI_FINANCIAL_ID, financialId)
                        .header(HeaderKeys.AUTHORIZATION, bearerToken.toAuthorizeHeader())
                        .get(HttpResponse.class);

        return response.getBody(CreatePaymentResponse.class);
    }

    public SubmittedPayment submitPayment(
            String paymentUniqueId,
            String psuIp,
            OAuth2Token accessToken,
            String paymentSubmissionId) {
        HttpResponse response =
                client.request(
                                Urls.SUBMIT_SEPA_PAYMENT
                                        + "/"
                                        + paymentUniqueId
                                        + "/submissions/"
                                        + paymentSubmissionId)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .header(HeaderKeys.X_API_KEY, configuration.getApiKey())
                        .header(HeaderKeys.X_FAPI_CUSTOMER_IP_ADDRESS, psuIp)
                        .header(HeaderKeys.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                        .header(HeaderKeys.AUTHORIZATION, accessToken.toAuthorizeHeader())
                        .put(HttpResponse.class);

        return response.getBody(SubmittedPayment.class);
    }

    private String createJwsHeader(CreatePaymentRequest payload, String kid) {
        final String serializedToJsonPayload = SerializationUtils.serializeToString(payload);

        final String serializedToJsonHeader =
                SerializationUtils.serializeToString(new TokenHeaderEntity(kid));

        final String headerBase64 =
                Base64.getUrlEncoder()
                        .encodeToString(serializedToJsonHeader.getBytes(StandardCharsets.UTF_8))
                        .replace("=", "")
                        .replace("/", "_")
                        .replace("+", "-");

        final String payloadBase64 =
                Base64.getUrlEncoder()
                        .encodeToString(serializedToJsonPayload.getBytes(StandardCharsets.UTF_8))
                        .replace("=", "")
                        .replace("/", "_")
                        .replace("+", "-");

        final String headerBase64WithEncodedPayload =
                String.format("%s.%s", headerBase64, payloadBase64)
                        .replace("=", "")
                        .replace("/", "_")
                        .replace("+", "-");

        final String signedBase64HeadersAndPayload =
                qsealcSigner
                        .getSignatureBase64(headerBase64WithEncodedPayload.getBytes())
                        .replace("=", "")
                        .replace("/", "_")
                        .replace("+", "-");
        return String.format("%s..%s", headerBase64, signedBase64HeadersAndPayload);
    }

    public String fetchSignature(String jwt) {
        byte[] signatureBytes = qsealcSigner.getSignature(jwt.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().encodeToString(signatureBytes);
    }

    public ExchangeTokenResponse exchangeToken(String code) {
        return client.request(Urls.OAUTH_TOKEN)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .accept(MediaType.APPLICATION_JSON)
                .body(
                        new ExchangeTokenForm()
                                .setClientId(configuration.getClientId())
                                .setClientSecret(configuration.getClientSecret())
                                .setCode(code)
                                .setRedirectUri(redirectUrl))
                .post(HttpResponse.class)
                .getBody(ExchangeTokenResponse.class);
    }

    public GetAccountsResponse getAccounts() {
        return baseAuthenticatedRequest(Urls.GET_ACCOUNTS).get(GetAccountsResponse.class);
    }

    public GetCreditCardsResponse getCreditCards() {
        return baseAuthenticatedRequest(Urls.GET_CREDIT_CARDS).get(GetCreditCardsResponse.class);
    }

    public GetTransactionsResponse getTransactions(URL url) {
        return baseAuthenticatedRequest(url).get(GetTransactionsResponse.class);
    }

    public GetCreditCardTransactionsResponse getCreditCardTransactions(URL url) {
        return baseAuthenticatedRequest(url).get(GetCreditCardTransactionsResponse.class);
    }

    private RequestBuilder baseAuthenticatedRequest(URL url) {
        RequestBuilder requestBuilder =
                client.request(url)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .header(HeaderKeys.X_API_KEY, configuration.getApiKey())
                        .header(HeaderKeys.X_FAPI_FINANCIAL_ID, financialId)
                        .header(HeaderKeys.X_CUSTOMER_USER_AGENT, financialId)
                        .header(HeaderKeys.X_FAPI_INTERACTION_ID, UUID.randomUUID().toString())
                        .addBearerToken(
                                persistentStorage
                                        .get(PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class)
                                        .orElseThrow(
                                                () ->
                                                        new IllegalStateException(
                                                                SessionError.SESSION_EXPIRED
                                                                        .exception())));
        if (userAvailability.isUserPresent()) {
            requestBuilder =
                    requestBuilder.header(
                            HeaderKeys.X_FAPI_CUSTOMER_IP_ADDRESS,
                            userAvailability.getOriginatingUserIp());
        }

        return requestBuilder;
    }

    private BankServiceException mapServiceException(HttpResponseException exception) {
        if (exception.getResponse().getStatus() == 503) {
            return BankServiceError.BANK_SIDE_FAILURE.exception(exception);
        } else {
            throw exception;
        }
    }
}
