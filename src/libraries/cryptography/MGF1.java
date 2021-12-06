package se.tink.libraries.cryptography;

import com.google.common.primitives.Bytes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MGF1 {

    public static byte[] generateMaskSHA1(byte[] mgfSeed, int length) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            return generateMask(mgfSeed, length, digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] generateMask(byte[] mgfSeed, int length, MessageDigest digest) {

        int hashCount = (length + digest.getDigestLength() - 1) / digest.getDigestLength();

        byte[] mask = new byte[0];
        for (int i = 0; i < hashCount; i++) {

            digest.update(mgfSeed);
            digest.update(new byte[3]);
            digest.update((byte) i);
            byte[] hash = digest.digest();
            mask = Bytes.concat(mask, hash);
        }

        byte[] output = new byte[length];
        System.arraycopy(mask, 0, output, 0, output.length);
        return output;
    }
}
