package se.tink.libraries.serialization.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerializationUtils {
    private static final ObjectMapper BINARY_MAPPER =
            new ObjectMapper(new SmileFactory())
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final Logger log = LoggerFactory.getLogger(SerializationUtils.class);
    private static final ObjectMapper STRING_MAPPER =
            new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .registerModule(new JavaTimeModule());

    public static <T> T deserializeFromBinary(byte[] data, Class<T> cls) throws IOException {
        Objects.requireNonNull(data);

        try {
            return BINARY_MAPPER.readValue(data, cls);
        } catch (IOException e) {
            log.error("Could not deserialize object", e);
            throw e;
        }
    }

    public static <T> T deserializeFromString(File file, TypeReference<T> typeReference) {
        if (file == null) {
            return null;
        }

        try {
            return STRING_MAPPER.readValue(file, typeReference);
        } catch (Exception e) {
            log.error("Could not deserialize object", e);
            return null;
        }
    }

    public static <T> T deserializeFromString(File file, Class<T> cls) {
        if (file == null) {
            return null;
        }

        try {
            return STRING_MAPPER.readValue(file, cls);
        } catch (Exception e) {
            log.error("Could not deserialize object", e);
            return null;
        }
    }

    public static <T> T deserializeFromBytes(byte[] bytes, Class<T> cls) {
        if (bytes == null) {
            return null;
        }

        try {
            return STRING_MAPPER.readValue(bytes, cls);
        } catch (Exception e) {
            log.error("Could not deserialize object", e);
            return null;
        }
    }

    public static <T> T deserializeFromString(String data, Class<T> cls) {
        return deserializeFromString(data, cls, e -> log.error("Could not deserialize object", e));
    }

    public static <T> Optional<T> deserializeForLogging(String data, Class<T> cls) {
        return Optional.ofNullable(
                deserializeFromString(
                        data, cls, e -> log.info("Undeserializable object found", e)));
    }

    private static <T> T deserializeFromString(
            String data, Class<T> cls, Consumer<Exception> logger) {
        if (data == null) {
            return null;
        }

        try {
            return STRING_MAPPER.readValue(data, cls);
        } catch (Exception e) {
            logger.accept(e);
            return null;
        }
    }

    public static <T> T deserializeFromString(String data, TypeReference<T> typeReference) {
        if (data == null) {
            return null;
        }

        try {
            return STRING_MAPPER.readValue(data, typeReference);
        } catch (Exception e) {
            log.error("Could not deserialize object", e);
            return null;
        }
    }

    public static <T> T deserializeFromTreeNode(TreeNode json, Class<T> c) {
        if (json == null) {
            return null;
        }
        try {
            return STRING_MAPPER.treeToValue(json, c);
        } catch (JsonProcessingException e) {
            log.error("Could not deserialize json", e);
            return null;
        }
    }

    public static <T> byte[] serializeToBinary(T value) throws IOException {
        try {
            return BINARY_MAPPER.writeValueAsBytes(value);
        } catch (IOException e) {
            log.error("Could not serialize object", e);
            throw e;
        }
    }

    public static <T> String serializeToString(T value) {
        try {
            return STRING_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.error("Could not serialize object", e);
            return null;
        }
    }

    public static String serializeKeyPair(KeyPair kp) {
        PublicKey pubKey = kp.getPublic();
        PrivateKey privKey = kp.getPrivate();

        Map<String, String> m = new HashMap<>();
        m.put("alg", privKey.getAlgorithm());
        m.put("pubKey", Hex.encodeHexString(pubKey.getEncoded()));
        m.put("privKey", Hex.encodeHexString(privKey.getEncoded()));
        return serializeToString(m);
    }

    public static KeyPair deserializeKeyPair(String data) {
        HashMap<String, String> m =
                SerializationUtils.deserializeFromString(
                        data, new TypeReference<HashMap<String, String>>() {});
        if (m == null) {
            throw new IllegalStateException("Could not deserialize object");
        }
        try {
            byte[] pubKeyBytes = Hex.decodeHex(m.get("pubKey").toCharArray());
            byte[] privKeyBytes = Hex.decodeHex(m.get("privKey").toCharArray());

            PublicKey pubKey = deserializePublicKey(m.get("alg"), pubKeyBytes);
            PrivateKey privKey = deserializePrivateKey(m.get("alg"), privKeyBytes);

            return new KeyPair(pubKey, privKey);
        } catch (DecoderException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private static PublicKey deserializePublicKey(String algorithm, byte[] keyData) {
        try {
            KeyFactory kf = KeyFactory.getInstance(algorithm);
            X509EncodedKeySpec pkSpec = new X509EncodedKeySpec(keyData);
            return kf.generatePublic(pkSpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private static PrivateKey deserializePrivateKey(String algorithm, byte[] keyData) {
        try {
            KeyFactory kf = KeyFactory.getInstance(algorithm);
            PKCS8EncodedKeySpec pkSpec = new PKCS8EncodedKeySpec(keyData);
            return kf.generatePrivate(pkSpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
