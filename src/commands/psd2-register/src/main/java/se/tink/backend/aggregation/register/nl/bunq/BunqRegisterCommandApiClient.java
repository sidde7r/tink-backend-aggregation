package se.tink.backend.aggregation.register.nl.bunq;

import com.google.common.base.Strings;
import java.security.PublicKey;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.rpc.CreateSessionPSD2ProviderResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.rpc.CreateSessionPSD2ProviderResponseWrapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.rpc.CreateSessionRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.rpc.InstallResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.rpc.RegisterDeviceRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.rpc.RegisterDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.rpc.RegisterDeviceResponseWrapper;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.register.nl.bunq.BunqRegisterConstants.UrlParameterKeys;
import se.tink.backend.aggregation.register.nl.bunq.rpc.AddOAuthClientIdResponse;
import se.tink.backend.aggregation.register.nl.bunq.rpc.AddOAuthClientIdResponseWrapper;
import se.tink.backend.aggregation.register.nl.bunq.rpc.AddOauthClientIdRequest;
import se.tink.backend.aggregation.register.nl.bunq.rpc.AddOauthClientIdRequest.Status;
import se.tink.backend.aggregation.register.nl.bunq.rpc.GetClientIdAndSecretResponse;
import se.tink.backend.aggregation.register.nl.bunq.rpc.GetClientIdAndSecretResponseWrapper;
import se.tink.backend.aggregation.register.nl.bunq.rpc.RegisterAsPSD2ProviderRequest;
import se.tink.backend.aggregation.register.nl.bunq.rpc.RegisterAsPSD2ProviderResponse;
import se.tink.backend.aggregation.register.nl.bunq.rpc.RegisterAsPSD2ProviderResponseWrapper;
import se.tink.backend.aggregation.register.nl.bunq.rpc.RegisterCallbackRequest;
import se.tink.backend.aggregation.register.nl.bunq.rpc.RegisterCallbackResponse;
import se.tink.backend.aggregation.register.nl.bunq.rpc.RegisterCallbackResponseWrapper;

public class BunqRegisterCommandApiClient {

    private final BunqBaseApiClient baseApiClient;
    private final TinkHttpClient client;

    public BunqRegisterCommandApiClient(TinkHttpClient client, String baseApiEndpoint) {
        this.baseApiClient = new BunqBaseApiClient(client, baseApiEndpoint);
        this.client = client;
    }

    public RegisterAsPSD2ProviderResponse registerAsPSD2Provider(
            RegisterAsPSD2ProviderRequest registerAsPSD2ProviderRequest) {
        RegisterAsPSD2ProviderResponseWrapper response =
                client.request(
                                baseApiClient.getUrl(
                                        BunqRegisterConstants.Urls.REGISTER_AS_PSD2_PROVIDER))
                        .post(
                                RegisterAsPSD2ProviderResponseWrapper.class,
                                registerAsPSD2ProviderRequest);

        return Optional.ofNullable(response.getResponse())
                .map(BunqResponse::getResponseBody)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Could not deserialize RegisterAsPSD2ProviderResponse"));
    }

    public AddOAuthClientIdResponse addOAuthClientId(String userId) {
        AddOAuthClientIdResponseWrapper response =
                client.request(
                                baseApiClient
                                        .getUrl(BunqRegisterConstants.Urls.GET_OAUTH_CLIENT_ID)
                                        .parameter(
                                                BunqBaseConstants.UrlParameterKeys.USER_ID, userId))
                        .post(
                                AddOAuthClientIdResponseWrapper.class,
                                new AddOauthClientIdRequest(Status.ACTIVE));

        return Optional.ofNullable(response.getResponse())
                .map(BunqResponse::getResponseBody)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Could not deserialize AddOAuthClientIdResponse"));
    }

    public GetClientIdAndSecretResponse getClientIdAndSecret(String userId, String oAuthClientId) {
        GetClientIdAndSecretResponseWrapper response =
                client.request(
                                baseApiClient
                                        .getUrl(BunqRegisterConstants.Urls.GET_CLIENT_ID_AND_SECRET)
                                        .parameter(
                                                BunqBaseConstants.UrlParameterKeys.USER_ID, userId)
                                        .parameter(
                                                BunqRegisterConstants.UrlParameterKeys
                                                        .OAUTH_CLIENT_ID,
                                                oAuthClientId))
                        .get(GetClientIdAndSecretResponseWrapper.class);

        return Optional.ofNullable(response.getResponse())
                .map(BunqResponse::getResponseBody)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Could not deserialize GetClientIdAndSecretResponse"));
    }

    public RegisterCallbackResponse registerCallbackUrl(
            String userId, String oAuthClientId, String redirectUrl) {
        RegisterCallbackResponseWrapper response =
                client.request(
                                baseApiClient
                                        .getUrl(BunqRegisterConstants.Urls.REGISTER_CALLBACK_URL)
                                        .parameter(
                                                BunqBaseConstants.UrlParameterKeys.USER_ID, userId)
                                        .parameter(
                                                BunqRegisterConstants.UrlParameterKeys
                                                        .OAUTH_CLIENT_ID,
                                                oAuthClientId))
                        .post(
                                RegisterCallbackResponseWrapper.class,
                                new RegisterCallbackRequest(redirectUrl));

        return Optional.ofNullable(response.getResponse())
                .map(BunqResponse::getResponseBody)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Could not deserialize RegisterCallbackResponse"));
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

    public RegisterDeviceResponse registerDevice(String apiKey, String aggregatorIdentifier) {
        String aggregatorName =
                Strings.isNullOrEmpty(aggregatorIdentifier)
                        ? BunqBaseConstants.DEVICE_NAME
                        : aggregatorIdentifier;

        RegisterDeviceResponseWrapper response =
                client.request(baseApiClient.getUrl(BunqBaseConstants.Url.REGISTER_DEVICE))
                        .post(
                                RegisterDeviceResponseWrapper.class,
                                RegisterDeviceRequest.createFromApiKeyAllIPs(
                                        aggregatorName, apiKey));

        return Optional.ofNullable(response.getResponse())
                .map(BunqResponse::getResponseBody)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Could not deserialize RegisterDeviceResponse"));
    }

    public void deleteSession(String sessionId) {
        client.request(
                        baseApiClient
                                .getUrl(BunqRegisterConstants.Urls.DELETE_SESSION)
                                .parameter(UrlParameterKeys.ITEM_ID, sessionId))
                .delete();
    }

    public InstallResponse installation(PublicKey publicKey) {
        return baseApiClient.installation(publicKey);
    }
}
