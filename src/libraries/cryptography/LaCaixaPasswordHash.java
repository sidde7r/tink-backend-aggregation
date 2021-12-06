package se.tink.libraries.cryptography;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;

public class LaCaixaPasswordHash {

    private static final String HASH_ALGORITHM = "MD5";
    private static final int MESSAGE_LENGTH = 64;
    private static final int COUNTER_1_VALUE = 3;
    private static final int COUNTER_2_VALUE = 7;

    private String password;
    private int iterations;
    private String seed;

    /*
     * Create caixa otp (hashed password).
     * Arguments are seed (semilla), iterations (iteraciones) and pin.
     */
    public static String hash(String seed, int iterations, String password) {
        return new LaCaixaPasswordHash(seed, iterations, password).createOtp();
    }

    private LaCaixaPasswordHash(String seed, int iterations, String password) {
        this.seed = seed;
        this.iterations = iterations;
        this.password = convertPassword(password);
    }

    private String convertPassword(String password) {
        // [password.uppercaseString dataUsingEncoding:NSISOLatin1StringEncoding
        // allowLossyConversion:YES]
        final byte[] passwordBytes =
                password.toUpperCase(Locale.ROOT).getBytes(StandardCharsets.ISO_8859_1);
        return new String(passwordBytes, StandardCharsets.ISO_8859_1);
    }

    private String createOtp() {
        byte[] result = null;

        // md5 string [seed + uppercase password] padded with spaces to 64
        byte[] md5BaseData =
                StringUtils.rightPad(seed + password, MESSAGE_LENGTH, ' ')
                        .getBytes(StandardCharsets.ISO_8859_1);

        // calculate otp
        for (int i = 0; i < iterations; i++) {
            // md5
            byte[] md5Sum = calculateMD5(md5BaseData);
            result = calculateFoldedHash(md5Sum);
            md5BaseData = result;
        }

        return Hex.encodeHexString(result);
    }

    private byte[] calculateMD5(byte[] data) {

        MessageDigest md = null;

        try {

            md = MessageDigest.getInstance(HASH_ALGORITHM);

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "Algorithm: (" + HASH_ALGORITHM + ") could not be found.");
        }

        return md.digest(data);
    }

    private byte[] calculateFoldedHash(byte[] md5Sum) {
        byte[] outbuf = new byte[8];
        long[] foldedData = null;

        foldedData = fold(md5Sum);
        long foldFirst4 = foldedData[0];
        long foldLast4 = foldedData[1];

        // fold to first 4 bytes
        for (int counter1 = COUNTER_1_VALUE; counter1 >= 0; counter1--) {
            outbuf[(int) counter1] = (byte) (foldFirst4 & 0xFF);
            foldFirst4 >>= 8;
        }

        // fold to last 4 bytes
        for (int counter = COUNTER_2_VALUE; counter > COUNTER_1_VALUE; counter--) {
            outbuf[(int) counter] = (byte) (foldLast4 & 0xFF);
            foldLast4 >>= 8;
        }

        return outbuf;
    }

    private long[] fold(byte[] md5Sum) {
        long[] foldedData = new long[2];

        long[] dataAsInts = buildFoldData(md5Sum);

        foldedData[0] = dataAsInts[2] ^ dataAsInts[0];
        foldedData[1] = dataAsInts[3] ^ dataAsInts[1];

        return foldedData;
    }

    private long[] buildFoldData(byte[] md5Sum) {
        long[] dataAsInts = new long[4];
        for (int i = 0; i < 4; i++) {
            int idx = i << 2;

            long b3 = ((long) md5Sum[idx] << 24) & 0xFF000000;
            long b2 = ((long) md5Sum[(idx | 1)] << 16) & 0xFF0000;
            long b1 = ((long) md5Sum[(idx | 2)] << 8) & 0xFF00;
            long b0 = md5Sum[(idx | 3)] & 0xFF;

            dataAsInts[i] = b0 + b1 + b2 + b3;
        }

        return dataAsInts;
    }
}
