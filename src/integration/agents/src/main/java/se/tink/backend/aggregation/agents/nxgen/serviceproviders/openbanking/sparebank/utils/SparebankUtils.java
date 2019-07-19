package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;

public class SparebankUtils {

    public static byte[] readFile(String path) {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static String generateSignature(String signingString, String keyPath, String algorithm) {
        byte[] signedBytes;
        PrivateKey privateKey = readSigningKey(keyPath, algorithm);
        try {
            Signature sha256withRSA = Signature.getInstance("SHA256withRSA");
            sha256withRSA.initSign(privateKey, new SecureRandom());
            sha256withRSA.update(signingString.getBytes(StandardCharsets.UTF_8));
            signedBytes = sha256withRSA.sign();
        } catch (InvalidKeyException | SignatureException | NoSuchAlgorithmException var4) {
            throw new IllegalStateException(var4.getMessage(), var4);
        }

        return Base64.getEncoder().encodeToString(signedBytes);
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

    public static String getCertificateEncoded(String certificatePath) {
        final String certificate = new String(readFile(certificatePath));
        try {
            return Base64.getEncoder().encodeToString(certificate.getBytes());
        } catch (Exception e) {
            throw new IllegalStateException(
                    SparebankConstants.ErrorMessages.ENCODE_CERTIFICATE_ERROR, e);
        }
    }

    public static String getSignature(
            Map<String, Optional<String>> headers, String keyId, String keyPath) {
        final String algorithm = "rsa-sha256";

        StringBuilder signingString = new StringBuilder();
        StringBuilder header = new StringBuilder();

        headers.forEach(
                (key, value) ->
                        value.ifPresent(
                                item -> {
                                    header.append(" " + key);
                                    signingString.append(String.format("%s: %s\n", key, item));
                                }));

        String signature =
                generateSignature(
                        signingString.toString().trim(),
                        keyPath,
                        SparebankConstants.Signature.SIGNING_ALGORITHM);
        return String.format(
                "keyId=\"%s\",algorithm=\"%s\",headers=\"%s\",signature=\"%s\"",
                keyId, algorithm, header.toString().trim(), signature);
    }

    public static String calculateDigest(final String data) {
        return Base64.getEncoder().encodeToString(Hash.sha256(data));
    }
}
