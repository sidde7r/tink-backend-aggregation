package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public final class JWTUtils {

    private JWTUtils() {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    public static String constructOIDCRequestObject(
            JWTHeader jwtHeader, JWTPayload jwtAuthPayload, String keyPath, String keyAlgorithm) {

        PrivateKey privateKey = JWTUtils.readSigningKey(keyPath, keyAlgorithm);

        return JWTUtils.toOIDCRequestObject(jwtHeader, jwtAuthPayload, privateKey);
    }

    public static String toOIDCRequestObject(
            JWTHeader jwtHeader, JWTPayload jwtPayload, PrivateKey privateKey) {

        try {
            ObjectMapper mapper = new ObjectMapper();

            String jwtHeaderJson;
            String jwtPayloadJson;

            jwtHeaderJson = mapper.writeValueAsString(jwtHeader);
            jwtPayloadJson = mapper.writeValueAsString(jwtPayload);

            String base64encodedHeader =
                    Base64.getUrlEncoder().encodeToString(jwtHeaderJson.getBytes());
            String base64encodedPayload =
                    Base64.getUrlEncoder().encodeToString(jwtPayloadJson.getBytes());

            String toBeSignedPayload =
                    String.format("%s.%s", base64encodedHeader, base64encodedPayload);

            byte[] signedPayload = toSHA256withRSA(privateKey, toBeSignedPayload);

            String signedAndEncodedPayload =
                    Base64.getUrlEncoder().encodeToString(signedPayload);

            return String.format("%s.%s", toBeSignedPayload, signedAndEncodedPayload);

        } catch (JsonProcessingException
                | InvalidKeyException
                | NoSuchAlgorithmException
                | SignatureException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static PrivateKey readSigningKey(String path, String algorithm) {
        try {
            return KeyFactory.getInstance(algorithm)
                    .generatePrivate(
                            new PKCS8EncodedKeySpec(
                                    Base64.getDecoder().decode(new String(readFile(path)))));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static byte[] readFile(String path) {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static byte[] toSHA256withRSA(PrivateKey privateKey, String input)
            throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {

        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(privateKey);

        signer.update(input.getBytes());
        return signer.sign();
    }
}
