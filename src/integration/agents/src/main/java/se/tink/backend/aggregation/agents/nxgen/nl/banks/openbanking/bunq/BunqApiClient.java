package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq;

import java.security.PublicKey;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.BunqConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc.CreateSessionUserAsPSD2ProviderResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc.CreateSessionUserAsPSD2ProviderResponseWrapper;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc.TokenExchangeResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.rpc.CreateSessionPSD2ProviderResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.rpc.CreateSessionPSD2ProviderResponseWrapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.rpc.CreateSessionRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.rpc.InstallResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.rpc.RegisterDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.fetchers.transactional.rpc.AccountsResponseWrapper;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

public class BunqApiClient {

    private final BunqBaseApiClient baseApiClient;
    private final TinkHttpClient client;

    public BunqApiClient(TinkHttpClient client, String baseApiEndpoint) {
        this.baseApiClient = new BunqBaseApiClient(client, baseApiEndpoint);
        this.client = client;
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
}
