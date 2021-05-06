package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.utils;

import java.security.PrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Arrays;
import se.tink.backend.aggregation.agents.utils.crypto.EllipticCurve;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public interface ChallengeUtils {

    /**
     * Code is base on the library which is used by Metro android app. It make some shifts in bytes
     * of EC public key
     *
     * @param publicKey EC publicKey
     * @return shifted and encoded PublicKey in Base64
     */
    static String shiftEcPublicKey(ECPublicKey publicKey) {
        byte[] xAffine = publicKey.getW().getAffineX().toByteArray();
        byte[] yAffine = publicKey.getW().getAffineY().toByteArray();
        byte[] shiftedPublicKey = new byte[65];
        shiftedPublicKey[0] = 4;
        if (xAffine.length < 32 || yAffine.length < 32) {
            throw new IllegalStateException("EC key representation requires fixing");
        }
        addAffineAxisCoordinateToRepresentation(shiftedPublicKey, 1, xAffine, 0, xAffine.length);
        addAffineAxisCoordinateToRepresentation(shiftedPublicKey, 33, yAffine, 0, yAffine.length);
        return EncodingUtils.encodeAsBase64String(shiftedPublicKey);
    }

    /**
     * Code is base on the library which is used by Metro android app. It make some shifts in bytes
     * of the signature
     *
     * @param privateKey EC privateKey
     * @param data data which's gonna be signed
     * @return shifted and encoded signature in Base64
     */
    static String signDataWithShift(PrivateKey privateKey, String data) {
        byte[] bytes = EllipticCurve.signSha256(privateKey, data.getBytes());
        int length = bytes.length;
        byte[] shiftedSignature = new byte[64];
        if (length >= 16 && bytes[0] == 48 && bytes[1] == length - 2) {
            byte b = bytes[3];
            int i = b + 4;
            byte b2 = bytes[i + 1];
            addAffineAxisCoordinateToRepresentation(shiftedSignature, 0, bytes, 4, b);
            addAffineAxisCoordinateToRepresentation(shiftedSignature, 32, bytes, i + 2, b2);
            return EncodingUtils.encodeAsBase64String(shiftedSignature);
        }
        throw new IllegalStateException("Invalid EC DER encoding");
    }

    static void addAffineAxisCoordinateToRepresentation(
            byte[] publicKey,
            int startPosition,
            byte[] affine,
            int endPosition,
            int lengthOffAffine) {
        if (lengthOffAffine < 32) {
            int result = (startPosition + 32) - lengthOffAffine;
            Arrays.fill(publicKey, startPosition, result - 1, (byte) 0);
            System.arraycopy(affine, endPosition, publicKey, result, lengthOffAffine);
        } else if (lengthOffAffine == 32) {
            System.arraycopy(affine, endPosition, publicKey, startPosition, 32);
        } else if (lengthOffAffine == 33) {
            System.arraycopy(affine, endPosition + 1, publicKey, startPosition, 32);
        } else {
            throw new IllegalStateException(
                    "Bad affine axis coordinate length: " + lengthOffAffine);
        }
    }
}
