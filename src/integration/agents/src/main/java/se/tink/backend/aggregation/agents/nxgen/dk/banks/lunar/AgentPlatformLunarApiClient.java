package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants.HeaderValues;
import static se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants.Headers;
import static se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants.Uri;

import agents_platform_agents_framework.org.springframework.http.HttpMethod;
import agents_platform_agents_framework.org.springframework.http.RequestEntity;
import agents_platform_agents_framework.org.springframework.http.RequestEntity.BodyBuilder;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.agentplatform.AgentPlatformHttpClient;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.entites.NemidEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.rpc.AccessTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.rpc.AccessTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.rpc.NemIdParamsResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.rpc.SignInRequest;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RequiredArgsConstructor
public class AgentPlatformLunarApiClient {

    private final AgentPlatformHttpClient client;
    private final RandomValueGenerator randomValueGenerator;

    public NemIdParamsResponse getNemIdParameters(String deviceId) {
        BodyBuilder requestBuilder =
                getDefaultBodyBuilder(HttpMethod.GET, Uri.NEM_ID_AUTHENTICATE, deviceId);
        return client.exchange(requestBuilder.build(), NemIdParamsResponse.class).getBody();
    }

    public AccessTokenResponse postNemIdToken(String signature, String challenge, String deviceId) {
        AccessTokenRequest accessTokenRequest =
                new AccessTokenRequest(new NemidEntity(signature, challenge));

        BodyBuilder requestBuilder =
                getDefaultBodyBuilder(HttpMethod.POST, Uri.NEM_ID_AUTHENTICATE, deviceId)
                        .header(Headers.CONTENT_TYPE, APPLICATION_JSON);

        return client.exchange(
                        requestBuilder.body(getSerializedAccessTokenRequest(accessTokenRequest)),
                        AccessTokenResponse.class)
                .getBody();
    }

    private String getSerializedAccessTokenRequest(AccessTokenRequest accessTokenRequest) {
        String serializedAccessTokenRequest =
                SerializationUtils.serializeToString(accessTokenRequest);
        if (serializedAccessTokenRequest != null) {
            return StringUtils.replace(serializedAccessTokenRequest, "/", "\\/");
        }
        throw LoginError.DEFAULT_MESSAGE.exception("Couldn't serialize access token request");
    }

    public TokenResponse signIn(String serviceCode, String token, String deviceId) {
        SignInRequest signInRequest = new SignInRequest(serviceCode);

        BodyBuilder requestBuilder =
                getDefaultBodyBuilder(HttpMethod.POST, Uri.SIGN_IN, deviceId)
                        .header(Headers.AUTHORIZATION, token)
                        .header(Headers.CONTENT_TYPE, APPLICATION_JSON);

        return client.exchange(requestBuilder.body(signInRequest), TokenResponse.class).getBody();
    }

    private BodyBuilder getDefaultBodyBuilder(HttpMethod httpMethod, URI uri, String deviceId) {
        BodyBuilder bodyBuilder = RequestEntity.method(httpMethod, uri);
        return addDefaultHeaders(bodyBuilder, deviceId);
    }

    private BodyBuilder addDefaultHeaders(BodyBuilder bodyBuilder, String deviceId) {
        return bodyBuilder
                .header(Headers.DEVICE_MODEL, HeaderValues.DEVICE_MODEL)
                .header(Headers.USER_AGENT, HeaderValues.USER_AGENT_VALUE)
                .header(Headers.REGION, HeaderValues.DK_REGION)
                .header(Headers.OS, HeaderValues.I_OS)
                .header(Headers.DEVICE_MANUFACTURER, HeaderValues.DEVICE_MANUFACTURER)
                .header(Headers.OS_VERSION, HeaderValues.OS_VERSION)
                .header(Headers.LANGUAGE, LunarConstants.DA_LANGUAGE)
                .header(Headers.REQUEST_ID, randomValueGenerator.getUUID().toString())
                .header(Headers.DEVICE_ID, deviceId)
                .header(Headers.ACCEPT_LANGUAGE, HeaderValues.DA_LANGUAGE_ACCEPT)
                .header(Headers.ORIGIN, HeaderValues.APP_ORIGIN)
                .header(Headers.APP_VERSION, LunarConstants.APP_VERSION)
                .header(Headers.ACCEPT, HeaderValues.ACCEPT_ALL)
                .header(Headers.ACCEPT_ENCODING, HeaderValues.ENCODING);
    }
}
