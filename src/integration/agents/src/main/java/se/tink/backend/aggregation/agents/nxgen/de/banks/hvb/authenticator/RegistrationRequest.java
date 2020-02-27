package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator;

import static java.lang.String.format;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.security.interfaces.RSAPrivateKey;
import java.util.Optional;
import javax.ws.rs.core.MultivaluedMap;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.RegistrationRequest.RequestBody.SignedRegistrationData;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.scaffold.ExternalApiCallResult;
import se.tink.backend.aggregation.nxgen.scaffold.SimpleExternalApiCall;

@Slf4j
public class RegistrationRequest extends SimpleExternalApiCall<AuthenticationData, String> {

    private static final String REGISTERED_CLIENT_URL_PREFIX = "^/mfp/api/registration/clients/";
    private final ConfigurationProvider configurationProvider;
    private final DataEncoder dataEncoder;

    public RegistrationRequest(
            TinkHttpClient httpClient,
            ConfigurationProvider configurationProvider,
            DataEncoder dataEncoder) {
        super(httpClient);
        this.configurationProvider = configurationProvider;
        this.dataEncoder = dataEncoder;
    }

    @Override
    protected HttpRequest prepareRequest(AuthenticationData arg) {
        return new HttpRequestImpl(
                HttpMethod.POST,
                new URL(configurationProvider.getBaseUrl() + "/registration/v1/self"),
                prepareRequestHeaders(),
                prepareRequestBody(arg));
    }

    private MultivaluedMap<String, Object> prepareRequestHeaders() {
        MultivaluedMap<String, Object> headers = configurationProvider.getStaticHeaders();
        headers.putSingle(ACCEPT, "*/*");
        headers.putSingle(CONTENT_TYPE, APPLICATION_JSON);
        return headers;
    }

    private RequestBody prepareRequestBody(AuthenticationData arg) {

        String payload = preparePayload(arg.getDeviceId());
        String header = dataEncoder.serializeAndBase64(arg.getJwkHeader());
        return new RequestBody()
                .setSignedRegistrationData(
                        new SignedRegistrationData()
                                .setPayload(payload)
                                .setHeader(header)
                                .setSignature(
                                        prepareSignature(header, payload, getPrivateKey(arg))));
    }

    private RSAPrivateKey getPrivateKey(AuthenticationData arg) {
        return (RSAPrivateKey) arg.getKeyPair().getPrivate();
    }

    private String prepareSignature(String header, String payload, RSAPrivateKey privateKey) {
        return dataEncoder.rsaSha256SignBase64Encode(privateKey, format("%s.%s", header, payload));
    }

    private String preparePayload(String deviceId) {
        Payload payload = Payload.withDefaultValues(deviceId);
        return dataEncoder.serializeAndBase64(payload);
    }

    @Override
    protected ExternalApiCallResult<String> parseResponse(HttpResponse httpResponse) {
        return ExternalApiCallResult.of(getClientId(httpResponse), httpResponse.getStatus());
    }

    private String getClientId(HttpResponse httpResponse) {
        return Optional.ofNullable(httpResponse)
                .map(HttpResponse::getLocation)
                .map(URI::getPath)
                .map(this::removePrefix)
                .orElseThrow(() -> new IllegalArgumentException("Couldn't extract clientId."));
    }

    private String removePrefix(String path) {
        return path.replaceFirst(REGISTERED_CLIENT_URL_PREFIX, "");
    }

    @Data
    @JsonObject
    @Accessors(chain = true)
    static class RequestBody {

        private SignedRegistrationData signedRegistrationData;

        @Data
        @JsonObject
        @Accessors(chain = true)
        static class SignedRegistrationData {
            private String payload;
            private String signature;
            private String header;
        }
    }

    @Data
    @JsonObject
    @Accessors(chain = true)
    static class Payload {
        private Application application;
        private Device device;
        private Attributes attributes;

        static Payload withDefaultValues(String deviceId) {
            return new Payload()
                    .setApplication(new Application())
                    .setAttributes(new Attributes())
                    .setDevice(new Device().setId(deviceId));
        }

        @Data
        @JsonObject
        @Accessors(chain = true)
        static class Application {
            private String id = "de.unicredit.apptan";
            private String clientPlatform = "ios";
            private String version = "4.1.0";
        }

        @Data
        @JsonObject
        @Accessors(chain = true)
        static class Device {
            private String id;
            private String platform = "ios 12.4.3";
            private String hardware = "iPhone";
        }

        @Data
        @JsonObject
        @Accessors(chain = true)
        static class Attributes {
            @JsonProperty("sdk_protocol_version")
            private Integer sdkProtocolVersion = 1;
        }
    }
}
