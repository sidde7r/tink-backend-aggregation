package se.tink.backend.aggregation.agents.nxgen.it.banks.ing;

import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Optional;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.utils.crypto.AES;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;

@AllArgsConstructor
public class Cryptor {

    private static final int AES_KEY_LENGTH = 32;
    private static final int AES_IV_LENGTH = 16;

    private final RandomDataProvider randomDataProvider;
    private final ConfigurationProvider configurationProvider;

    public byte[] rsaEncrypt(RSAPublicKey rsaPublicKey, byte[] data) {
        return configurationProvider.useRsaWithPadding()
                ? RSA.encryptEcbPkcs1(rsaPublicKey, data)
                : RSA.encryptEcbNoPadding(rsaPublicKey, data);
    }

    public String rsaEncryptBase64UrlEncode(RSAPublicKey rsaPublicKey, byte[] data) {
        return base64UrlEncode(rsaEncrypt(rsaPublicKey, data));
    }

    public byte[] aesEncrypt(byte[] key, byte[] iv, byte[] data) {
        return AES.encryptCbcPkcs7(key, iv, data);
    }

    private String aesEncryptBase64UrlEncode(byte[] key, byte[] iv, byte[] data) {
        return base64UrlEncode(aesEncrypt(key, iv, data));
    }

    public String aesEncryptBase64UrlEncode(byte[] key, byte[] iv, String data) {
        return aesEncryptBase64UrlEncode(key, iv, data.getBytes(StandardCharsets.UTF_8));
    }

    private byte[] rsaSha256Sign(RSAPrivateKey rsaPrivateKey, byte[] dataToSign) {
        return RSA.signSha256(rsaPrivateKey, dataToSign);
    }

    public String rsaSha256SignBase64UrlEncode(RSAPrivateKey rsaPrivateKey, String dataToSign) {
        return base64UrlEncode(
                rsaSha256Sign(rsaPrivateKey, dataToSign.getBytes(StandardCharsets.UTF_8)));
    }

    private String base64UrlEncode(byte[] data) {
        try {
            return URLEncoder.encode(
                    Base64.getEncoder().encodeToString(data), StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new UncheckedIOException(e);
        }
    }

    public byte[] decodeBase64(String data) {
        return Base64.getDecoder().decode(data);
    }

    public String base64DecodeAesDecrypt(byte[] key, byte[] iv, String data) {
        return Optional.of(data)
                .map(this::decodeBase64)
                .map(value -> AES.decryptCbcPkcs7(key, iv, value))
                .map(value -> new String(value, StandardCharsets.UTF_8))
                .orElseThrow(IllegalArgumentException::new);
    }

    public byte[] generateRandomAesKey() {
        return randomDataProvider.generateRandomBytes(AES_KEY_LENGTH);
    }

    public byte[] generateRandomAesIv() {
        return randomDataProvider.generateRandomBytes(AES_IV_LENGTH);
    }

    public String encodeBase64(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }
}
