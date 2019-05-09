package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.utils;

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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.Formats;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.rpc.ConsentRequest;
import se.tink.libraries.serialization.utils.SerializationUtils;

public final class SibsUtils {

    private SibsUtils() {
        throw new AssertionError("Suppress default constructor for noninstantiability");
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

    public static String readSigningCertificate(final String certPath) {
        return new String(readFile(certPath));
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

    public static String getDigest(ConsentRequest consentRequest) {

        byte[] bytes =
                SerializationUtils.serializeToString(consentRequest)
                        .getBytes(StandardCharsets.US_ASCII);
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(Formats.SHA_256);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException();
        }
        md.update(bytes, 0, bytes.length);
        byte[] sha = md.digest();
        return org.apache.commons.codec.binary.Base64.encodeBase64String(sha);
    }

    public static String getRequestId() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }
}
