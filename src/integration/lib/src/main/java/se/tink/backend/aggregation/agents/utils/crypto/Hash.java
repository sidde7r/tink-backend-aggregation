package se.tink.backend.aggregation.agents.utils.crypto;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;

public class Hash {
    public static String hmacSha1AsHex(byte[] key, byte[] data) {
        byte[] digest = hmacSha1(key, data);
        return Hex.encodeHexString(digest);
    }

    public static String hmacSha256AsHex(byte[] key, byte[] data) {
        byte[] digest = hmacSha256(key, data);
        return Hex.encodeHexString(digest);
    }

    public static String sha1AsHex(final byte[]... datas) {
        return Hex.encodeHexString(sha("SHA-1", datas));
    }

    public static String sha1AsHex(String data) {
        return sha1AsHex(data.getBytes());
    }

    public static String sha256AsHex(final byte[]... datas) {
        return Hex.encodeHexString(sha("SHA-256", datas));
    }

    public static String sha256AsHex(final String data) {
        return sha256AsHex(data.getBytes());
    }

    public static byte[] sha1(final String data) {
        return sha("SHA-1", data.getBytes());
    }

    public static byte[] sha256(final String data) {
        return sha("SHA-256", data.getBytes());
    }

    public static byte[] sha1(final byte[]... datas) {
        return sha("SHA-1", datas);
    }

    public static byte[] sha256(final byte[]... datas) {
        return sha("SHA-256", datas);
    }

    public static byte[] hmacSha1(String key, String data) {
        return hmacSha1(key.getBytes(), data.getBytes());
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

    public static byte[] sha512(final byte[]... datas) {
        return sha("SHA-512", datas);
    }

    public static byte[] sha512(final String data) {
        return sha("SHA-512", data.getBytes());
    }

    public static byte[] hmacSha512(final byte[] key, final byte[] data) {
        return hmac("HmacSHA512", key, data);
    }

    private static byte[] sha(String algorithm, final byte[]... datas) {
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

    private static byte[] sha1WithCounter(byte[] inputData, byte[] counter) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(inputData);
            md.update(counter);
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
