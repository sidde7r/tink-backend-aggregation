package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq;

import java.security.PublicKey;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc.AddOAuthClientIdResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc.AddOAuthClientIdResponseWrapper;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc.AddOauthClientIdRequest;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc.AddOauthClientIdRequest.Status;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc.CreateSessionPSD2ProviderResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc.CreateSessionPSD2ProviderResponseWrapper;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc.CreateSessionUserAsPSD2ProviderResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc.CreateSessionUserAsPSD2ProviderResponseWrapper;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc.GetClientIdAndSecretResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc.GetClientIdAndSecretResponseWrapper;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc.RegisterAsPSD2ProviderRequest;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc.RegisterAsPSD2ProviderResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc.RegisterAsPSD2ProviderResponseWrapper;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc.RegisterCallbackRequest;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc.RegisterCallbackResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc.RegisterCallbackResponseWrapper;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc.TokenExchangeResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.BunqBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.BunqBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.BunqResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.authenticator.rpc.CreateSessionRequest;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.authenticator.rpc.TokenEntity;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class BunqApiClient extends BunqBaseApiClient {

    public BunqApiClient(TinkHttpClient client, String baseApiEndpoint) {
        super(client, baseApiEndpoint);
    }

    public RegisterAsPSD2ProviderResponse registerAsPSD2Provider(
            PublicKey publicKey, TokenEntity tokenEntity) {
        RegisterAsPSD2ProviderResponseWrapper response =
                client.request(getUrl(BunqConstants.Url.REGISTER_AS_PSD2_PROVIDER))
                        .post(
                                RegisterAsPSD2ProviderResponseWrapper.class,
                                RegisterAsPSD2ProviderRequest.of(publicKey, tokenEntity));

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
                                getUrl(BunqConstants.Url.GET_OAUTH_CLIENT_ID)
                                        .parameter(BunqConstants.UrlParameterKeys.USER_ID, userId))
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

    public GetClientIdAndSecretResponse getOAuthClientId(String userId) {
        GetClientIdAndSecretResponseWrapper response =
                client.request(
                                getUrl(BunqConstants.Url.GET_OAUTH_CLIENT_ID)
                                        .parameter(BunqConstants.UrlParameterKeys.USER_ID, userId))
                        .get(GetClientIdAndSecretResponseWrapper.class);

        return Optional.ofNullable(response.getResponse())
                .map(BunqResponse::getResponseBody)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Could not deserialize GetClientIdAndSecretResponse"));
    }

    public GetClientIdAndSecretResponse getClientIdAndSecret(String userId, String oAuthClientId) {
        GetClientIdAndSecretResponseWrapper response =
                client.request(
                                getUrl(BunqConstants.Url.GET_CLIENT_ID_AND_SECRET)
                                        .parameter(BunqConstants.UrlParameterKeys.USER_ID, userId)
                                        .parameter(
                                                BunqConstants.UrlParameterKeys.OAUTH_CLIENT_ID,
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
                                getUrl(BunqConstants.Url.REGISTER_CALLBACK_URL)
                                        .parameter(BunqConstants.UrlParameterKeys.USER_ID, userId)
                                        .parameter(
                                                BunqConstants.UrlParameterKeys.OAUTH_CLIENT_ID,
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

    public TokenExchangeResponse getAccessToken(
            String code, String redirectUrl, String clientId, String clientSecret) {
        return client.request(BunqConstants.Url.TOKEN_EXCHANGE)
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
                client.request(getUrl(BunqBaseConstants.Url.CREATE_SESSION))
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
                client.request(getUrl(BunqBaseConstants.Url.CREATE_SESSION))
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
}
