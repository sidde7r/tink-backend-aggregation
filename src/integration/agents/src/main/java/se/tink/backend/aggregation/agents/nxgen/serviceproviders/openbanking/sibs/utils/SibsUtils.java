package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.utils;

import com.google.common.base.Strings;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.Formats;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.SignatureValues;
import se.tink.libraries.serialization.utils.SerializationUtils;

public final class SibsUtils {

    private static final String DASH = "-";
    private static final String NEW_LINE = "\n";
    private static final String COLON_SPACE = ": ";

    private SibsUtils() {}

    public static String getSignature(
            String digest,
            String transactionId,
            String requestId,
            String signatureStringDate,
            String clientSigningKeyPath,
            String clientSigningCertificateSerialNumber) {

        StringBuilder signingString = new StringBuilder();

        if (!Strings.isNullOrEmpty(digest)) {
            signingString
                    .append(HeaderKeys.DIGEST.toLowerCase())
                    .append(COLON_SPACE)
                    .append(HeaderValues.DIGEST_PREFIX)
                    .append(digest)
                    .append(NEW_LINE);
        }

        signingString
                .append(HeaderKeys.TPP_TRANSACTION_ID.toLowerCase())
                .append(COLON_SPACE)
                .append(transactionId)
                .append(NEW_LINE)
                .append(HeaderKeys.TPP_REQUEST_ID.toLowerCase())
                .append(COLON_SPACE)
                .append(requestId)
                .append(NEW_LINE)
                .append(HeaderKeys.DATE.toLowerCase())
                .append(COLON_SPACE)
                .append(signatureStringDate);

        byte[] signatureSha;
        try {
            PrivateKey privateKey = SibsUtils.readSigningKey(clientSigningKeyPath);
            signatureSha = SibsUtils.toSHA256withRSA(privateKey, signingString.toString());
        } catch (SignatureException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("Cannot sign SIBS request.", e);
        }

        String signatureBase64Sha =
                org.apache.commons.codec.binary.Base64.encodeBase64String(signatureSha);

        return formSignature(digest, clientSigningCertificateSerialNumber, signatureBase64Sha);
    }

    public static String getDigest(Object body) {

        byte[] bytes =
                SerializationUtils.serializeToString(body).getBytes(StandardCharsets.US_ASCII);
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(Formats.SHA_256);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Cannot calculate SHA256.", e);
        }
        md.update(bytes, 0, bytes.length);
        byte[] sha = md.digest();
        return org.apache.commons.codec.binary.Base64.encodeBase64String(sha);
    }

    public static String getRequestId() {
        return java.util.UUID.randomUUID().toString().replace(DASH, StringUtils.EMPTY);
    }

    private static String formSignature(
            String digest, String clientSigningCertificateSerialNumber, String signatureBase64Sha) {
        return String.format(
                Formats.SIGNATURE_STRING_FORMAT,
                clientSigningCertificateSerialNumber,
                SignatureValues.RSA_SHA256,
                Strings.isNullOrEmpty(digest)
                        ? SignatureValues.HEADERS_NO_DIGEST
                        : SignatureValues.HEADERS,
                signatureBase64Sha);
    }

    private static PrivateKey readSigningKey(String path) {
        try {
            return KeyFactory.getInstance(Formats.RSA)
                    .generatePrivate(
                            new PKCS8EncodedKeySpec(
                                    Base64.getDecoder().decode(new String(readFile(path)))));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static String readSigningCertificate(final String certPath) {
        return new String(readFile(certPath));
    }

    private static byte[] readFile(String path) {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private static byte[] toSHA256withRSA(PrivateKey privateKey, String input)
            throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {

        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(privateKey);

        signer.update(input.getBytes());
        return signer.sign();
    }
}
