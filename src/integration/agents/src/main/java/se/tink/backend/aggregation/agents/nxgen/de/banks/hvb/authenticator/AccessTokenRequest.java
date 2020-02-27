package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator;

import static java.lang.String.format;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import javax.ws.rs.core.MultivaluedMap;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.scaffold.ExternalApiCallResult;
import se.tink.backend.aggregation.nxgen.scaffold.SimpleExternalApiCall;

@Slf4j
public class AccessTokenRequest extends SimpleExternalApiCall<AuthenticationData, AccessToken> {

    private static final int TOKEN_LIFE_TIME = 300;

    private final ConfigurationProvider configurationProvider;
    private final DataEncoder dataEncoder;

    public AccessTokenRequest(
            TinkHttpClient httpClient,
            ConfigurationProvider configurationProvider,
            DataEncoder dataEncoder) {
        super(httpClient);
        this.configurationProvider = configurationProvider;
        this.dataEncoder = dataEncoder;
    }

    @Override
    protected HttpRequest prepareRequest(AuthenticationData authData) {
        return new HttpRequestImpl(
                HttpMethod.POST,
                new URL(configurationProvider.getBaseUrl() + "/az/v1/token"),
                prepareRequestHeaders(),
                prepareRequestBody(authData));
    }

    private MultivaluedMap<String, Object> prepareRequestHeaders() {
        MultivaluedMap<String, Object> headers = configurationProvider.getStaticHeaders();
        headers.putSingle(ACCEPT, "*/*");
        headers.putSingle(CONTENT_TYPE, APPLICATION_FORM_URLENCODED);
        return headers;
    }

    private String prepareRequestBody(AuthenticationData authData) {

        return Form.builder()
                .encodeSpacesWithPercent()
                .put("client_assertion", prepareToken(authData))
                .put(
                        "client_assertion_type",
                        "urn:ietf:params:oauth:client-assertion-type:jwt-bearer")
                .put("code", authData.getCode())
                .put("grant_type", "authorization_code")
                .put("redirect_uri", "https://mfpredirecturi")
                .build()
                .serialize();
    }

    private String prepareToken(AuthenticationData authData) {
        String jwkHeader = prepareJwkHeader(authData);
        String token = format("%s.%s", jwkHeader, prepareTokenPayload(authData));
        return token + "." + prepareTokenSignature(token, authData);
    }

    private String prepareJwkHeader(AuthenticationData authData) {
        JwkHeader jwkHeader = authData.getJwkHeader();
        jwkHeader.getJwk().setKid(authData.getClientId());
        return dataEncoder.serializeAndBase64(jwkHeader);
    }

    private String prepareTokenPayload(AuthenticationData authData) {
        Payload payload =
                Payload.of(authData.getCode(), authData.getClientId(), authData.getInstant());
        return dataEncoder.serializeAndBase64(payload);
    }

    private String prepareTokenSignature(String token, AuthenticationData authData) {
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) authData.getKeyPair().getPrivate();
        return dataEncoder.rsaSha256SignBase64Encode(rsaPrivateKey, token);
    }

    @Override
    protected ExternalApiCallResult<AccessToken> parseResponse(HttpResponse httpResponse) {
        ResponseBody responseBody = httpResponse.getBody(ResponseBody.class);
        AccessToken accessToken =
                new AccessToken()
                        .setTokenValue(responseBody.getAccessToken())
                        .setTokenType(responseBody.getTokenType())
                        .setExpiresIn(responseBody.getExpiresIn())
                        .setScope(responseBody.getScope());
        return ExternalApiCallResult.of(accessToken, httpResponse.getStatus());
    }

    @Data
    @Accessors(chain = true)
    static class Arg {
        String clientId;
        String code;
        JwkHeader jwkHeader;
        RSAPrivateKey privateKey;
    }

    @Data
    @JsonObject
    @Accessors(chain = true)
    static class ResponseBody {
        @JsonProperty("access_token")
        String accessToken;

        @JsonProperty("token_type")
        String tokenType;

        @JsonProperty("expires_in")
        Long expiresIn;

        String scope;
    }

    @Data
    @JsonObject
    @Accessors(chain = true)
    static class Payload {
        private Long iat;
        private Long exp;
        private String iss = "de.unicredit.apptan$ios$4.1.0";
        private String jti;
        private String aud = "(null)az/v1/token";
        private String sub;

        static Payload of(String code, String clientId, Instant instant) {
            return new Payload()
                    .setIat(instant.toEpochMilli())
                    .setExp(instant.plusSeconds(TOKEN_LIFE_TIME).toEpochMilli())
                    .setJti(code)
                    .setSub(clientId);
        }
    }
}
