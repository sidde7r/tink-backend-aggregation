package se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.utils;

import java.util.Arrays;
import se.tink.libraries.cryptography.AES;

// The message in the protocol is called `activationMessage2`, i.e. this is not
// the second iteration of a `ActivationMessage` utils class.
public class ActivationMessage2 {
    public static byte[] decrypt(byte[] key, byte[] activationMessage2) {
        // 39 bytes of data
        byte[] data = Arrays.copyOfRange(activationMessage2, 15, 15 + 39);
        // 8 bytes counter nonce.
        byte[] counterNonce = Arrays.copyOfRange(activationMessage2, 7, 7 + 8);
        return AES.decryptCtr(key, counterNonce, data);
    }

    // I'm not sure what this value is really called, I've decided to call it `digipassId`.
    public static byte[] extractDigipassId(byte[] activationMessage2) {
        return Arrays.copyOfRange(activationMessage2, 2, 2 + 5);
    }

    public static int extractDeviceCount(byte[] decryptedActivationMessage2) {
        return (int) decryptedActivationMessage2[5];
    }

    public static byte[] decryptKey1(byte[] key, byte[] decryptedActivationMessage2) {
        byte[] data = Arrays.copyOfRange(decryptedActivationMessage2, 6, 6 + 16);
        return AES.decryptEcbNoPadding(key, data);
    }

    public static byte[] decryptKey2(byte[] key, byte[] decryptedActivationMessage2) {
        byte[] data = Arrays.copyOfRange(decryptedActivationMessage2, 6 + 16 + 1, 6 + 16 + 1 + 16);
        return AES.decryptEcbNoPadding(key, data);
    }
}
