package se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration;

import static se.tink.backend.aggregation.nxgen.http.request.HttpMethod.POST;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.AuthMethod;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.ConfigurationProvider;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.Cryptor;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.EncryptedExternalApiCall;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration.CompleteTokenActivationExternalApiCall.Arg;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration.CompleteTokenActivationExternalApiCall.Result;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.scaffold.ExternalApiCallResult;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

class CompleteTokenActivationExternalApiCall extends EncryptedExternalApiCall<Arg, Result> {

    private final Clock clock;
    private final CommonDataProvider commonDataProvider;
    private final ConfigurationProvider configurationProvider;
    private final RSAPrivateKey rsaPrivateClientKey;

    CompleteTokenActivationExternalApiCall(
            TinkHttpClient httpClient,
            Clock clock,
            Cryptor cryptor,
            CommonDataProvider commonDataProvider,
            ConfigurationProvider configurationProvider,
            RSAPublicKey rsaPublicExternalApiKey,
            RSAPrivateKey rsaPrivateClientKey) {
        super(httpClient, cryptor, rsaPublicExternalApiKey);
        this.clock = clock;
        this.commonDataProvider = commonDataProvider;
        this.configurationProvider = configurationProvider;
        this.rsaPrivateClientKey = rsaPrivateClientKey;
    }

    @Override
    protected HttpRequest prepareRequest(Arg arg, AesKey aesKey) {
        return new HttpRequestImpl(
                POST,
                new URL(
                        String.format(
                                "%s%s",
                                configurationProvider.getBaseUrl(),
                                "/MobileFlow/completeTokenActivation.htm")),
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
                        .sign(prepareSign(encryptedPayload))
                        .build();
        String serializedRequestBody = SerializationUtils.serializeToString(requestBody);
        return String.format("request=%s", serializedRequestBody);
    }

    private String prepareEncryptedPayloadFieldValue(Arg arg, AesKey aesKey) {
        RequestBodyPayloadField requestBodyPayloadField =
                RequestBodyPayloadField.builder()
                        .challenge(arg.getChallenge())
                        .otp(arg.getOtp())
                        .authMethod(AuthMethod.SMART_CODE)
                        .activationId(arg.getActivationId())
                        .fingerprint(commonDataProvider.prepareFingerprint(arg.getDeviceId()))
                        .jailbroken(false)
                        .timestamp(clock.millis())
                        .build();
        return aesEncryptBase64UrlEncode(
                aesKey, SerializationUtils.serializeToString(requestBodyPayloadField));
    }

    private String prepareEncryptedKey(byte[] key) {
        return rsaEncryptBase64UrlEncode(key);
    }

    private String prepareEncryptedInitializationVector(byte[] initializationVector) {
        return rsaEncryptBase64UrlEncode(initializationVector);
    }

    private String prepareSign(String dataToSign) {
        return getCryptor().rsaSha256SignBase64UrlEncode(rsaPrivateClientKey, dataToSign);
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

    @Value
    @Builder
    static final class Arg {

        private String challenge;
        private String otp;
        private String activationId;
        private String deviceId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static final class Result {

        private String result;
        private Integer code;
        private String message;
    }

    @JsonObject
    @Value
    static final class ResponseBody {

        private Integer code;

        @JsonProperty("res")
        private String response;

        private String sign;
    }

    @JsonObject
    @Builder
    @Value
    static final class RequestBody {

        @JsonProperty("payload")
        private String payload;

        @JsonProperty("aK")
        private String encryptedKey;

        @JsonProperty("aI")
        private String encryptedInitializationVector;

        @JsonProperty("sign")
        private String sign;
    }

    @JsonObject
    @Value
    @Builder
    static final class RequestBodyPayloadField {

        private String challenge;
        private String otp;

        @JsonProperty("auth_method")
        private AuthMethod authMethod;

        @JsonProperty("activationID")
        private String activationId;

        private String fingerprint;
        private boolean jailbroken;
        private Long timestamp;
    }
}
