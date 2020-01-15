package se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration;

import static se.tink.backend.aggregation.nxgen.http.request.HttpMethod.POST;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.AuthMethod;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.ConfigurationProvider;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.Cryptor;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.EncryptedExternalApiCall;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration.ActivateTokenExternalApiCall.Arg;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration.ActivateTokenExternalApiCall.Result;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.scaffold.ExternalApiCallResult;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class ActivateTokenExternalApiCall extends EncryptedExternalApiCall<Arg, Result> {

    private final Clock clock;
    private final CommonDataProvider commonDataProvider;
    private final ConfigurationProvider configurationProvider;

    ActivateTokenExternalApiCall(
            TinkHttpClient httpClient,
            Clock clock,
            Cryptor cryptor,
            CommonDataProvider commonDataProvider,
            ConfigurationProvider configurationProvider,
            RSAPublicKey rsaPublicExternalApiKey) {
        super(httpClient, cryptor, rsaPublicExternalApiKey);
        this.clock = clock;
        this.commonDataProvider = commonDataProvider;
        this.configurationProvider = configurationProvider;
    }

    @Override
    protected HttpRequest prepareRequest(Arg arg, AesKey aesKey) {
        return new HttpRequestImpl(
                POST,
                new URL(
                        String.format(
                                "%s%s",
                                configurationProvider.getBaseUrl(),
                                "/MobileFlow/activateToken.htm")),
                prepareBody(arg, aesKey));
    }

    private String prepareBody(Arg arg, AesKey aesKey) {
        String encryptedPayload = prepareEncryptedPayloadFieldValue(arg, aesKey);
        RequestBody requestBody =
                RequestBody.builder()
                        .payload(encryptedPayload)
                        .encryptedKey(prepareEncryptedKey(aesKey.getKey()))
                        .encryptedInitializationVector(
                                prepareEncryptedInitializationVector(
                                        aesKey.getInitializationVector()))
                        .build();
        return String.format("request=%s", getSerializedRequestBody(requestBody));
    }

    private String getSerializedRequestBody(RequestBody requestBody) {
        return Optional.of(requestBody)
                .map(SerializationUtils::serializeToString)
                .orElseThrow(
                        () -> new IllegalArgumentException("Couldn't serialize request body."));
    }

    private String prepareEncryptedPayloadFieldValue(Arg arg, AesKey aesKey) {
        RequestBodyPayloadField requestBodyPayloadField =
                RequestBodyPayloadField.builder()
                        .password(preparePassword())
                        .authMethod(AuthMethod.SMART_CODE)
                        .fingerprint(commonDataProvider.prepareFingerprint(arg.getDeviceId()))
                        .userId(arg.getUserId())
                        .jailbroken(false)
                        .timestamp(clock.millis())
                        .pin(arg.getPin())
                        .build();
        return aesEncryptBase64UrlEncode(
                aesKey, SerializationUtils.serializeToString(requestBodyPayloadField));
    }

    private String preparePassword() {
        return new String(getCryptor().generateRandomAesIv(), StandardCharsets.UTF_8);
    }

    private String prepareEncryptedKey(byte[] key) {
        return rsaEncryptBase64UrlEncode(key);
    }

    private String prepareEncryptedInitializationVector(byte[] initializationVector) {
        return rsaEncryptBase64UrlEncode(initializationVector);
    }

    @Override
    protected ExternalApiCallResult<Result> parseResponse(
            HttpResponse httpResponse, AesKey aesKey) {
        ResponseBody responseBody = httpResponse.getBody(ResponseBody.class);
        Result result =
                SerializationUtils.deserializeFromString(
                        base64DecodeAesDecrypt(aesKey, responseBody.getResponse()), Result.class);
        return ExternalApiCallResult.of(result, httpResponse.getStatus());
    }

    @Builder
    @Value
    static class Arg {

        private String userId;
        private String pin;
        private String deviceId;
    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class Result {

        @JsonProperty("activationID")
        private String activationId;

        private Integer code;
        private String message;
        private String result;

        @JsonProperty("challenges_id")
        private String challengeId;
    }

    @JsonObject
    @Value
    @Builder
    static final class ResponseBody {

        private String activationId;
        private Integer code;

        @JsonProperty("res")
        private String response;

        private String sign;

        @JsonProperty("userId")
        private String userId;
    }

    @JsonObject
    @Builder
    @Value
    static final class RequestBody {

        @JsonProperty("payload")
        private String payload;

        @JsonProperty("aI")
        private String encryptedInitializationVector;

        @JsonProperty("aK")
        private String encryptedKey;
    }

    @JsonObject
    @Value
    @Builder
    static final class RequestBodyPayloadField {

        private String password;

        @JsonProperty("auth_method")
        private AuthMethod authMethod;

        private String fingerprint;

        @JsonProperty("username")
        private String userId;

        private boolean jailbroken;
        private Long timestamp;
        private String pin;
    }
}
