package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq;

import java.security.PublicKey;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.BunqConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc.CreateSessionUserAsPSD2ProviderResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc.CreateSessionUserAsPSD2ProviderResponseWrapper;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc.TokenExchangeResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.executor.payment.rpc.CreateDraftPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.executor.payment.rpc.CreateDraftPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.executor.payment.rpc.CreateDraftPaymentResponseWrapper;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.executor.payment.rpc.GetDraftPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.executor.payment.rpc.GetDraftPaymentResponseWrapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqBaseConstants.UrlParameterKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.rpc.CreateSessionPSD2ProviderResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.rpc.CreateSessionPSD2ProviderResponseWrapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.rpc.CreateSessionRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.rpc.InstallResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.rpc.RegisterDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.fetchers.transactional.rpc.AccountsResponseWrapper;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class BunqApiClient {

    private final BunqBaseApiClient baseApiClient;
    private final TinkHttpClient client;

    public BunqApiClient(TinkHttpClient client, String baseApiEndpoint) {
        this.baseApiClient = new BunqBaseApiClient(client, baseApiEndpoint);
        this.client = client;
    }

    public BunqBaseApiClient getBaseApiClient() {
        return baseApiClient;
    }

    public TokenExchangeResponse getAccessToken(
            String code, String redirectUrl, String clientId, String clientSecret) {
        return client.request(Urls.TOKEN_EXCHANGE)
                .queryParam(
                        BunqConstants.QueryParams.GRANT_TYPE,
                        BunqConstants.QueryValues.AUTHORIZATION_CODE)
                .queryParam(BunqConstants.QueryParams.CODE, code)
                .queryParam(BunqConstants.QueryParams.REDIRECT_URI, redirectUrl)
                .queryParam(BunqConstants.QueryParams.CLIENT_ID, clientId)
                .queryParam(BunqConstants.QueryParams.CLIENT_SECRET, clientSecret)
                .post(TokenExchangeResponse.class);
    }

    public CreateSessionPSD2ProviderResponse createSessionPSD2Provider(String apiKey) {
        CreateSessionPSD2ProviderResponseWrapper response =
                client.request(baseApiClient.getUrl(BunqBaseConstants.Url.CREATE_SESSION))
                        .post(
                                CreateSessionPSD2ProviderResponseWrapper.class,
                                CreateSessionRequest.createFromApiKey(apiKey));

        return Optional.ofNullable(response.getResponse())
                .map(BunqResponse::getResponseBody)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Could not deserialize CreateSessionResponse"));
    }

    public CreateSessionUserAsPSD2ProviderResponse createSessionUserAsPSD2Provider(String apiKey) {
        CreateSessionUserAsPSD2ProviderResponseWrapper response =
                client.request(baseApiClient.getUrl(BunqBaseConstants.Url.CREATE_SESSION))
                        .post(
                                CreateSessionUserAsPSD2ProviderResponseWrapper.class,
                                CreateSessionRequest.createFromApiKey(apiKey));

        return Optional.ofNullable(response.getResponse())
                .map(BunqResponse::getResponseBody)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Could not deserialize CreateSessionUserAsPSD2ProviderResponse"));
    }

    public RegisterDeviceResponse registerDevice(String apiKey, String aggregatorIdentifier) {
        return baseApiClient.registerDevice(apiKey, aggregatorIdentifier);
    }

    public InstallResponse installation(PublicKey publicKey) {
        return baseApiClient.installation(publicKey);
    }

    public AccountsResponseWrapper listAccounts(String userId) {
        return baseApiClient.listAccounts(userId);
    }

    public CreateDraftPaymentResponse createDraftPayment(
            String userId, String accountId, CreateDraftPaymentRequest createDraftPaymentRequest) {
        CreateDraftPaymentResponseWrapper response =
                client.request(
                                baseApiClient
                                        .getUrl(Urls.DRAFT_PAYMENT)
                                        .parameter(UrlParameterKeys.USER_ID, userId)
                                        .parameter(UrlParameterKeys.ACCOUNT_ID, accountId))
                        .post(CreateDraftPaymentResponseWrapper.class, createDraftPaymentRequest);

        return Optional.ofNullable(response.getResponse())
                .map(BunqResponse::getResponseBody)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Could not deserialize CreateDraftPaymentResponse"));
    }

    public GetDraftPaymentResponse getDraftPayment(
            String userId, String accountId, long paymentId) {

        GetDraftPaymentResponseWrapper response =
                client.request(
                                baseApiClient
                                        .getUrl(Urls.DRAFT_PAYMENT_SPECIFIC_PAYMENT)
                                        .parameter(UrlParameterKeys.USER_ID, userId)
                                        .parameter(UrlParameterKeys.ACCOUNT_ID, accountId)
                                        .parameter(
                                                UrlParameterKeys.PAYMENT_ID,
                                                String.valueOf(paymentId)))
                        .get(GetDraftPaymentResponseWrapper.class);

        return Optional.ofNullable(response.getResponse())
                .map(BunqResponse::getResponseBody)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Could not deserialize GetDraftPaymentResponse"));
    }
}
