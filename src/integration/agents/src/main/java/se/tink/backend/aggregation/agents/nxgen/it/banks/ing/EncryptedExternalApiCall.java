package se.tink.backend.aggregation.agents.nxgen.it.banks.ing;

import java.security.interfaces.RSAPublicKey;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.scaffold.ExternalApiCall;
import se.tink.backend.aggregation.nxgen.scaffold.ExternalApiCallResult;

public abstract class EncryptedExternalApiCall<T, R> implements ExternalApiCall<T, R> {

    @Value
    @RequiredArgsConstructor
    @EqualsAndHashCode
    public static class AesKey {

        private byte[] key;
        private byte[] initializationVector;
    }

    private final TinkHttpClient httpClient;
    private final Cryptor cryptor;
    private final RSAPublicKey rsaPublicExternalApiKey;

    protected EncryptedExternalApiCall(
            TinkHttpClient httpClient, Cryptor cryptor, RSAPublicKey rsaPublicExternalApiKey) {
        this.httpClient = httpClient;
        this.cryptor = cryptor;
        this.rsaPublicExternalApiKey = rsaPublicExternalApiKey;
    }

    @Override
    public ExternalApiCallResult<R> execute(T arg) {
        AesKey aesKey = generateAesKey();
        return Optional.of(arg)
                .map(value -> prepareRequest(value, aesKey))
                .map(this::executeHttpCall)
                .map(value -> parseResponse(value, aesKey))
                .orElseThrow(IllegalArgumentException::new);
    }

    protected abstract HttpRequest prepareRequest(T arg, AesKey aesKey);

    protected abstract ExternalApiCallResult<R> parseResponse(
            HttpResponse httpResponse, AesKey aesKey);

    protected AesKey generateAesKey() {
        return new AesKey(cryptor.generateRandomAesKey(), cryptor.generateRandomAesIv());
    }

    protected String rsaEncryptBase64UrlEncode(byte[] data) {
        return cryptor.rsaEncryptBase64UrlEncode(rsaPublicExternalApiKey, data);
    }

    protected String aesEncryptBase64UrlEncode(AesKey aesKey, String data) {
        return cryptor.aesEncryptBase64UrlEncode(
                aesKey.getKey(), aesKey.getInitializationVector(), data);
    }

    protected String base64DecodeAesDecrypt(AesKey aesKey, String data) {
        return cryptor.base64DecodeAesDecrypt(
                aesKey.getKey(), aesKey.getInitializationVector(), data);
    }

    protected Cryptor getCryptor() {
        return this.cryptor;
    }

    private HttpResponse executeHttpCall(HttpRequest httpRequest) {
        return httpClient.request(HttpResponse.class, httpRequest);
    }
}
