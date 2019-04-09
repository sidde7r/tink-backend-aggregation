package se.tink.backend.aggregation.agents.utils.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LaCaixaPasswordHash {

    private static final String HASH_ALGORITHM = "MD5";
    private static final String PADDING =
            "                " + "                " + "                ";

    private static final int MESSAGE_LENGTH = 64;
    private static final int COUNTER_1_VALUE = 3;
    private static final int COUNTER_2_VALUE = 7;

    private String pin;
    private int iterations;
    private String seed;

    /*
     * Create caixa otp (hashed password).
     * Arguments are seed (semilla), iterations (iteraciones) and pin.
     */
    public LaCaixaPasswordHash(String seed, int iterations, String pin) {
        this.seed = seed;
        this.iterations = iterations;
        this.pin = pin;
    }

    public String createOtp() {
        String result = null;

        // md5 string [seed + pin + space] 64 long
        String md5BaseData = (seed + pin + PADDING).substring(0, MESSAGE_LENGTH);

        // calculate otp
        for (int i = 0; i < iterations; i++) {
            // md5
            byte[] md5Sum = calculateMD5(md5BaseData);
            result = calculateFoldedHash(md5Sum);
            md5BaseData = result;
        }

        return result;
    }

    private byte[] calculateMD5(String md5BaseData) {

        MessageDigest md = null;

        try {

            md = MessageDigest.getInstance(HASH_ALGORITHM);

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "Algorithm: (" + HASH_ALGORITHM + ") could not be found.");
        }

        return md.digest(md5BaseData.getBytes(StandardCharsets.UTF_8));
    }

    private String calculateFoldedHash(byte[] md5Sum) {
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

        return org.apache.commons.codec.binary.Hex.encodeHexString(outbuf);
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
