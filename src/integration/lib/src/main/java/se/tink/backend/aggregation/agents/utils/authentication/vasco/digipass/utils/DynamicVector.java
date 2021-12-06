package se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.utils;

import java.util.Arrays;
import se.tink.libraries.cryptography.AES;

// The message in the protocol is called `activationMessage`, however the code references to this as
// DynamicVector.
public class DynamicVector {
    public static String calculateLogonId(byte[] dynamicVector) {
        long output = 0;
        for (int shift = 0, off = 4; shift != -24; shift -= 8) {
            long v = dynamicVector[off++] & 0xff;
            output += (v << (shift + 16));
        }
        return String.format("%07d", output & 0xffffffffL);
    }

    public static byte[] decryptDynamicVectorKey(byte[] key, byte[] dynamicVector) {
        byte[] data = Arrays.copyOfRange(dynamicVector, 32, 32 + 16);
        return AES.decryptEcbNoPadding(key, data);
    }
}
