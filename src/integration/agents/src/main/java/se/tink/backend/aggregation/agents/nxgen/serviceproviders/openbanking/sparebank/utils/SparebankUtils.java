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
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.HeaderKeys;

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
            String xRequestId,
            String date,
            String psuId,
            String redirectUri,
            String keyId,
            String keyPath) {
        final String algorithm = "rsa-sha256";

        String header = "date x-request-id tpp-redirect-uri";

        String sigingString =
                String.format(
                        SparebankConstants.Signature.DATE
                                + ": %s"
                                + System.lineSeparator()
                                + HeaderKeys.X_REQUEST_ID.toLowerCase()
                                + ": %s"
                                + System.lineSeparator()
                                + SparebankConstants.Signature.TPP_REDIRECT_URI
                                + ": %s",
                        date,
                        xRequestId,
                        redirectUri);

        if (!Strings.isNullOrEmpty(psuId)) {
            header += " psu-id";
            sigingString += String.format("\npsu-id: %s", psuId);
        }

        String signature =
                SparebankUtils.generateSignature(
                        sigingString, keyPath, SparebankConstants.Signature.SIGNING_ALGORITHM);
        return String.format(
                "keyId=\"%s\",algorithm=\"%s\",headers=\"%s\",signature=\"%s\"",
                keyId, algorithm, header, signature);
    }
}
