package se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.utils;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Bytes;
import java.security.KeyPair;
import java.util.Arrays;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import se.tink.backend.aggregation.agents.utils.crypto.AES;
import se.tink.backend.aggregation.agents.utils.crypto.EllipticCurve;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.backend.aggregation.agents.utils.crypto.KeyDerivation;
import se.tink.backend.aggregation.agents.utils.crypto.TripleDES;

public class CryptoUtils {
    private static final int ITERATIONS = 1024;
    private static final byte[] SALT = "0".getBytes();
    private static final int AES_KEY_LENGTH = 16;

    public static byte[] deriveActivationKey(String activationPassword) {
        return KeyDerivation.pbkdf2WithHmacSha256(
                activationPassword,
                SALT,
                ITERATIONS,
                AES_KEY_LENGTH);
    }

    public static byte[] encryptPublicKeyAndNonce(byte[] key, byte[] iv, KeyPair ecKey, byte[] nonce) {
        byte[] publicKey = EllipticCurve.convertPublicKeyToPoint(ecKey, false);
        byte[] publicKeyAndNonce = Bytes.concat(publicKey, nonce);

        return AES.encryptCfbSegmentationSize8NoPadding(key, iv, publicKeyAndNonce);
    }

    public static byte[] reencryptServerNonce(byte[] key, byte[] decryptionIv, byte[] encryptionIv,
            byte[] encryptedNonces) {
        // Server sends us theirs and our nonces encrypted and expects us to encrypt their nonce and send it back.

        byte[] decryptedNonces = AES.decryptCbc(key, decryptionIv, encryptedNonces);
        Preconditions.checkState(decryptedNonces.length == 8,
                "Decrypted nonces must be of length 8.");

        // Extract the server nonce
        byte[] serverNonce = Arrays.copyOfRange(decryptedNonces, 0, 4);

        return AES.encryptCbc(key, encryptionIv, serverNonce);
    }

    public static ECPublicKey decryptPublicKey(String curveName, byte[] key, byte[] iv, byte[] encryptedPublicKey) {
        byte[] publicKeyBytes = AES.decryptCfbSegmentationSize8NoPadding(key, iv, encryptedPublicKey);

        return EllipticCurve.convertPointToPublicKey(Bytes.concat(new byte[] {0x04}, publicKeyBytes), curveName);
    }

    public static byte[] calculateSharedSecret(ECPrivateKey privateKey, ECPublicKey publicKey) {
        byte[] derivedKey = EllipticCurve.diffieHellmanDeriveKeyConcatXY(privateKey, publicKey);
        derivedKey = DataUtils.swapBytes(derivedKey);
        derivedKey = Hash.sha256(derivedKey);
        derivedKey = Arrays.copyOfRange(derivedKey, 0, 16);
        return derivedKey;
    }

    public static byte[] calculateOtpKey(byte[] key, byte[] iv, byte[] data) {
        byte[] keyPart0 = TripleDES.decryptCbcNoPadding(key, iv, data);
        byte[] keyPart1 = TripleDES.encryptCbcNoPadding(key, iv, data);

        return Bytes.concat(keyPart0, keyPart1);
    }

    public static byte[] aes8(byte[] key, byte[] data) {
        Preconditions.checkArgument((data.length % 8) == 0, "Input data must be dividable 8");

        byte[] output = new byte[8];
        for (int i=0; i<data.length; i+=8) {
            byte[] block = Arrays.copyOfRange(data, i, i+8);

            block = DataUtils.xor(block, output);
            byte[] concatenated_data = Bytes.concat(block, block);

            byte[] ct = AES.encryptEcbNoPadding(key, concatenated_data);

            // combine the 16 byte block with XOR into one 8 byte block
            for (int j=0; j<8; j++) {
                output[j] = (byte)(ct[j] ^ ct[8+j]);
            }
        }
        return output;
    }

    public static byte[] getXorKey(byte[] key) {
        byte[] zeroes = new byte[8];
        byte[] data = CryptoUtils.aes8(key, zeroes);

        long v = DataUtils.bytesToLong(data);

        long v2 = v * 2;

        byte[] output = DataUtils.longToBytes(v2);

        if ((v & 0x8000000000000000L) == 0x8000000000000000L) {
            output[7] ^= (byte)0x1b;
        }

        return output;
    }
}
