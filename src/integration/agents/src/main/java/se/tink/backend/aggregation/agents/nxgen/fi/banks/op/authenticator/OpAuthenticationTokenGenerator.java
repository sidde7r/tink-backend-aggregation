package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankConstants;

public class OpAuthenticationTokenGenerator {

    /*
     * Generate the key used in combination with the seed to generate the AUTH_TOKEN.
     */
    private static String generateKey() {
        int i = 0;
        KeyCalculator calculator = new KeyCalculator();
        int j = Math.max(pickChar(calculator.calculate()) - 'A', 0);
        StringBuilder localStringBuilder = new StringBuilder(j);
        while (i < j) {
            localStringBuilder.append(pickChar(calculator.calculate()));
            i += 1;
        }
        return localStringBuilder.toString();
    }

    /*
     * Pick chars from constant strings to be part of key.
     */
    private static char pickChar(int paramInt) {
        int i = (paramInt & 0x3F) % 127;
        switch ((paramInt & 0xC0) >> 6) {
            case 0:
                return OpBankConstants.KeyGenerator.CHAR_STRING_1.charAt(i);
            case 1:
                return OpBankConstants.KeyGenerator.CHAR_STRING_2.charAt(i);
            case 2:
                return OpBankConstants.KeyGenerator.CHAR_STRING_3.charAt(i);
            case 3:
                return OpBankConstants.KeyGenerator.CHAR_STRING_4.charAt(i);
            default:
                return 'A';
        }
    }

    private static class KeyCalculator {
        private int a = OpBankConstants.KeyGenerator.RAND_INT_A;
        private int b = OpBankConstants.KeyGenerator.RAND_INT_B;
        private int c = OpBankConstants.KeyGenerator.RAND_INT_C;
        private int d = OpBankConstants.KeyGenerator.RAND_INT_D;

        private KeyCalculator() {
            this(94);
        }

        private KeyCalculator(int paramInt) {
            while (paramInt > 0) {
                calculate();
                paramInt -= 1;
            }
        }

        public final int calculate() {
            int i = this.a ^ this.a << 11;
            this.a = this.b;
            this.b = this.c;
            this.c = this.d;
            i = i ^ i >> 8 ^ this.d ^ this.d >> 19;
            this.d = i;
            return i;
        }
    }

    public static String calculateAuthToken(String seed) {
        try {
            String KEY = OpAuthenticationTokenGenerator.generateKey();
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(Hex.decodeHex((seed + KEY).toCharArray()));
            String authToken = OpBankConstants.AUTH_TOKEN_PREFIX + new String(Hex.encodeHex(hash));
            return authToken;
        } catch (DecoderException | NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
