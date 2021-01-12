package se.tink.backend.aggregation.agents.utils.crypto.hash;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;

public class Hash {

    private static final Base64.Encoder BASE64 = Base64.getEncoder();

    private Hash() {}

    public static String hmacSha1AsHex(byte[] key, byte[] data) {
        byte[] digest = hmacSha1(key, data);
        return Hex.encodeHexString(digest);
    }

    public static String hmacSha256AsBase64(byte[] key, byte[] data) {
        byte[] digest = hmacSha256(key, data);
        return BASE64.encodeToString(digest);
    }

    public static String sha1AsHex(final byte[]... datas) {
        return Hex.encodeHexString(hashFunction("SHA-1", datas));
    }

    public static String sha1AsHex(String data) {
        return sha1AsHex(data.getBytes());
    }

    public static String sha256AsHex(final byte[]... datas) {
        return Hex.encodeHexString(hashFunction("SHA-256", datas));
    }

    public static String sha256AsHex(final String data) {
        return sha256AsHex(data.getBytes());
    }

    public static byte[] sha256(final String data) {
        return hashFunction("SHA-256", data.getBytes());
    }

    public static byte[] sha1(final byte[]... datas) {
        return hashFunction("SHA-1", datas);
    }

    public static byte[] sha256(final byte[]... datas) {
        return hashFunction("SHA-256", datas);
    }

    public static String sha256Base64(final byte[]... datas) {
        return BASE64.encodeToString(sha256(datas));
    }

    public static byte[] hmacSha256(String key, String data) {
        return hmacSha256(key.getBytes(), data.getBytes());
    }

    public static byte[] hmacSha1(byte[] key, byte[] data) {
        return hmac("HmacSHA1", key, data);
    }

    public static byte[] hmacSha256(byte[] key, byte[] data) {
        return hmac("HmacSHA256", key, data);
    }

    public static byte[] hmacSha512(byte[] key, byte[] data) {
        return hmac("HmacSHA512", key, data);
    }

    public static byte[] sha512(final String data) {
        return hashFunction("SHA-512", data.getBytes());
    }

    public static byte[] sha512(final byte[]... data) {
        return hashFunction("SHA-512", data);
    }

    public static byte[] md5(final String data) {
        return hashFunction("MD5", data.getBytes());
    }

    public static byte[] md5(final byte[]... datas) {
        return hashFunction("MD5", datas);
    }

    private static byte[] hashFunction(String algorithm, final byte[]... datas) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            for (byte[] data : datas) {
                md.update(data);
            }
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private static byte[] hmac(String algorithm, byte[] key, byte[] data) {
        try {
            Mac hmac = Mac.getInstance(algorithm);
            SecretKeySpec keyValue = new SecretKeySpec(key, algorithm);
            hmac.init(keyValue);
            return hmac.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
