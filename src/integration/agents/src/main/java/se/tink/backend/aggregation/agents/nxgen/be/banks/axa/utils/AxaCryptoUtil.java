package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.utils;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import se.tink.backend.aggregation.agents.utils.crypto.EllipticCurve;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.libraries.encoding.EncodingUtils;

public class AxaCryptoUtil {

    public static KeyPair generateRSAKeyPair() {
        return RSA.generateKeyPair(2048);
    }

    public static KeyPair generateRequestSignatureECKeyPair() {
        return EllipticCurve.generateKeyPair("prime256v1");
    }

    public static KeyPair generateChallengeSignECKeyPair() {
        return EllipticCurve.generateKeyPair(256);
    }

    public static String getRawEcPublicKey(KeyPair keyPair) {
        try {
            byte[] bytes = rawEcPublicKeyRepresentation((ECPublicKey) keyPair.getPublic());
            return EncodingUtils.encodeAsBase64String(bytes);
        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException("Wrong ecPublic key data", e);
        }
    }

    public static String createSignedFch(KeyPair keyPair, String challenge, String assertionId) {
        try {
            String dataToSign = challenge + assertionId;
            byte[] signatureBytes =
                    EllipticCurve.signSha256(keyPair.getPrivate(), dataToSign.getBytes());
            byte[] rawSignature = convertDEREcSignatureToRawEcSignature(signatureBytes);
            return EncodingUtils.encodeAsBase64String(rawSignature);
        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException("Wrong signature", e);
        }
    }

    public static String createHeaderSignature(
            KeyPair keyPair,
            String httpRequestPath,
            String clientVersionHeaderValue,
            String requestBody) {
        String headerDataToSign =
                createHeaderDataToSign(httpRequestPath, clientVersionHeaderValue, requestBody);
        return createSignedData(keyPair, headerDataToSign);
    }

    public static String createSignedData(KeyPair keyPair, String data) {
        byte[] signatureBytes = EllipticCurve.signSha256(keyPair.getPrivate(), data.getBytes());
        return EncodingUtils.encodeAsBase64String(signatureBytes);
    }

    public static KeyPair generateKeyPairFromBase64(
            String encodedPublicKey, String encodedPrivateKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PublicKey publicKey =
                    keyFactory.generatePublic(
                            new X509EncodedKeySpec(
                                    EncodingUtils.decodeBase64String(encodedPublicKey)));
            PrivateKey privateKey =
                    keyFactory.generatePrivate(
                            new PKCS8EncodedKeySpec(
                                    EncodingUtils.decodeBase64String(encodedPrivateKey)));
            return new KeyPair(publicKey, privateKey);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Stored keys can't be recreated", e);
        }
    }

    private static String createHeaderDataToSign(
            String httpRequestPath, String clientVersionHeaderValue, String requestBody) {
        return new StringBuilder()
                .append(httpRequestPath)
                .append("%%")
                .append(clientVersionHeaderValue)
                .append("%%")
                .append(requestBody)
                .toString();
    }

    private static byte[] rawEcPublicKeyRepresentation(ECPublicKey ecPublicKey)
            throws InvalidKeySpecException {
        byte[] r = ecPublicKey.getW().getAffineX().toByteArray();
        byte[] s = ecPublicKey.getW().getAffineY().toByteArray();
        byte[] outArray = new byte[65];
        outArray[0] = (byte) 4;
        addAffineAxisCoordinateToRepresentation(outArray, 1, r, 0, r.length);
        addAffineAxisCoordinateToRepresentation(outArray, 33, s, 0, s.length);
        return outArray;
    }

    private static byte[] convertDEREcSignatureToRawEcSignature(byte[] data)
            throws InvalidKeySpecException {
        int len = data.length;
        byte[] rawSigningResult = new byte[64];
        if (len >= 16 && data[0] == (byte) 48 && data[1] == len - 2) {
            int rLen = data[3];
            if (data[2] != (byte) 2 || len < rLen + 5) {
                throw new IllegalStateException("Invalid EC DER encoding");
            }
            int sLen = data[(rLen + 4) + 1];
            if (data[rLen + 4] != (byte) 2 || len < ((sLen + 4) + rLen) + 2) {
                throw new IllegalStateException("Invalid EC DER encoding");
            }
            // TODO: commented on purpose, verify if this check is needed
            // if (rLen < 32 || sLen < 32) {
            //    throw new IllegalStateException("Invalid EC DER encoding");
            // }
            addAffineAxisCoordinateToRepresentation(rawSigningResult, 0, data, 4, rLen);
            addAffineAxisCoordinateToRepresentation(
                    rawSigningResult, 32, data, (rLen + 4) + 2, sLen);
            return rawSigningResult;
        }

        throw new IllegalStateException("Invalid EC DER encoding");
    }

    private static void addAffineAxisCoordinateToRepresentation(
            byte[] destArray, int destOffset, byte[] affineSrc, int srcOffset, int affineLength)
            throws InvalidKeySpecException {
        if (affineLength < 32) {
            Arrays.fill(destArray, destOffset, ((destOffset + 32) - affineLength) - 1, (byte) 0);
            System.arraycopy(
                    affineSrc,
                    srcOffset,
                    destArray,
                    (destOffset + 32) - affineLength,
                    affineLength);
        } else if (affineLength == 32) {
            System.arraycopy(affineSrc, srcOffset, destArray, destOffset, 32);
        } else if (affineLength == 33) {
            System.arraycopy(affineSrc, srcOffset + 1, destArray, destOffset, 32);
        } else {
            throw new InvalidKeySpecException("Bad affine axis coordinate length: " + affineLength);
        }
    }
}
