package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.utils;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Bytes;
import com.sun.jna.Library;
import com.sun.jna.Native;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.utils.crypto.AES;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class KbcOtpUtils {

    private static final String LIBRARY_FILE_PATH;

    static {
        boolean mac = System.getProperty("os.name").toLowerCase().contains("mac");
        String fileName;
        if (mac) {
            fileName = "libkbc_wbaes_mac.dylib";
        } else {
            fileName = "libkbc_wbaes_linux.so";
        }

        LIBRARY_FILE_PATH = System.getProperty("user.dir") + "/tools/" + fileName;
    }

    private interface KbcWhiteBoxAes extends Library {
        public void kbc_wb_aes128_encrypt(byte[] input, byte[] output);
    }

    private static byte[] xor(byte[] a, byte[] b) {
        Preconditions.checkArgument(a.length == b.length,
                "Cannot XOR two different length byte arrays.");
        byte[] output = new byte[a.length];
        for (int i=0; i<a.length; i++) {
            output[i] = (byte)(a[i] ^ b[i]);
        }
        return output;
    }

    public static byte[] intToBytes(int v) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(v);
        return buffer.array();
    }

    private static byte[] longToBytes(long v) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(v);
        return buffer.array();
    }

    private static long bytesToLong(byte[] b) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        if (b.length < 8) {
            buffer.put(new byte[8 - b.length]);
        }
        buffer.put(b);
        buffer.flip();
        return buffer.getLong();
    }

    public static int bytesToInt(byte[] b) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        if (b.length < 4) {
            buffer.put(new byte[4 - b.length]);
        }
        buffer.put(b);
        buffer.flip();
        return buffer.getInt();
    }

    public static String calculateLogonId(byte[] dynamicVector) {
        long output = 0;
        for (int shift=0, off=4; shift!=-24; shift-=8) {
            long v = dynamicVector[off++] & 0xff;
            output += (v << (shift + 16));
        }
        return String.format("%07d", output & 0xffffffffL);
    }

    public static byte[] aes8(byte[] key, byte[] data) {
        Preconditions.checkArgument((data.length % 8) == 0, "Input data must be dividable 8");

        byte[] output = new byte[8];
        for (int i=0; i<data.length; i+=8) {
            byte[] block = Arrays.copyOfRange(data, i, i+8);

            block = xor(block, output);
            byte[] concatenated_data = Bytes.concat(block, block);

            byte[] ct = AES.encryptEcbNoPadding(key, concatenated_data);

            // combine the 16 byte block with XOR into one 8 byte block
            for (int j=0; j<8; j++) {
                output[j] = (byte)(ct[j] ^ ct[8+j]);
            }
        }
        return output;
    }

    private static byte[] getXorKey(byte[] key) {
        byte[] zeroes = new byte[8];
        byte[] data = aes8(key, zeroes);

        long v = bytesToLong(data);

        long v2 = v * 2;

        byte[] output = longToBytes(v2);

        if ((v & 0x8000000000000000L) == 0x8000000000000000L) {
            output[7] ^= (byte)0x1b;
        }

        return output;
    }

    public static byte[] mashupChallenge(byte[] key, byte[] diversifier, int counter, List<byte[]> challenges) {
        Preconditions.checkArgument(!challenges.isEmpty(), "Challenges were empty.");

        byte[] challenge0 = challenges.get(0);
        byte[] ctr_diversifier = Bytes.concat(intToBytes(counter), diversifier);
        byte[] stage1 = aes8(key, ctr_diversifier);

        byte[] challenge0Xored = xor(challenge0, stage1);

        byte[] xorKey = getXorKey(key);

        if (challenges.size() == 1) {
            return xor(challenge0Xored, xorKey);
        }

        byte[] lastChallenge = challenges.get(challenges.size()-1);
        byte[] lastChallengeXored = xor(xorKey, lastChallenge);

        byte[] output = challenge0Xored;

        for (int i=1; i<challenges.size()-1; i++) {
            output = Bytes.concat(output, challenges.get(i));
        }

        return Bytes.concat(output, lastChallengeXored);
    }

    private static long unsignedDiv(int i1, int i2) {
        long l1 = i1 & 0xffffffffL, l2 = i2 & 0xffffffffL;
        return l1 / l2;
    }

    public static byte[] convertOtpResponse(byte[] data) {
        // Divide the lower and upper half (32 bit values) of the calculated response value by diminishing powers of 10.
        // Merge the resulting array into one where each entry is a nibble.

        int l = bytesToInt(Arrays.copyOfRange(data, 0, 4));
        int h = bytesToInt(Arrays.copyOfRange(data, 4, 8));

        byte[] barray = new byte[20];
        int m = 1000000000;
        for (int i=0; i<10; i++,m/=10) {
            barray[i] = (byte)(unsignedDiv(l, m) % 0x0a);
            barray[i+10] = (byte)(unsignedDiv(h, m) % 0x0a);
        }

        byte[] output = new byte[10];
        for (int i=0,j=0; i<10; i++,j+=2) {
            output[i] = (byte)(barray[j+1] + 16 * barray[j]);
        }
        return output;
    }

    public static String convertOtpToAscii(byte[] otp) {
        String s = EncodingUtils.encodeHexAsString(otp);
        return s.substring(s.length() - 16);
    }

    public static byte[] wbAesEncrypt(byte[] data) {
        KbcWhiteBoxAes kbcWhiteBoxAes = (KbcWhiteBoxAes)Native.loadLibrary(LIBRARY_FILE_PATH, KbcWhiteBoxAes.class);
        byte[] out = new byte[16];
        kbcWhiteBoxAes.kbc_wb_aes128_encrypt(data, out);
        return out;
    }

    public static long extractTlvFieldAsLong(byte[] staticVector, int pos, int fieldType) {
        Optional<byte[]> bValue = extractTlvField(staticVector, pos, fieldType, 0);
        if (!bValue.isPresent()) {
            throw new IllegalStateException(String.format("Could not find filedType %d in static vector.", fieldType));
        }
        return bytesToLong(bValue.get());
    }

    public static int extractTlvFieldAsInt(byte[] staticVector, int pos, int fieldType) {
        Optional<byte[]> bValue = extractTlvField(staticVector, pos, fieldType, 0);
        if (!bValue.isPresent()) {
            throw new IllegalStateException(String.format("Could not find filedType %d in static vector.", fieldType));
        }
        return bytesToInt(bValue.get());
    }

    public static Optional<byte[]> extractTlvField(byte[] tlvData, int pos, int fieldType, int index) {
        // The static vector is a TLV structure.
        // 1 byte type
        // 1 byte length
        // n bytes data

        while (pos < tlvData.length) {
            int type = tlvData[pos++] & 0xff;
            if (pos >= tlvData.length) {
                break;
            }

            int length = tlvData[pos++] & 0xff;
            if (pos >= tlvData.length) {
                break;
            }

            if (type == fieldType && (index-- == 0)) {
                return Optional.of(Arrays.copyOfRange(tlvData, pos, pos+length));
            }

            pos += length;
        }

        return Optional.empty();
    }

    public static byte[] decryptActivationMessage(byte[] key, byte[] activationMessage) {
        // 39 bytes of data
        byte[] data = Arrays.copyOfRange(activationMessage, 15,  15 + 39);

        // 8 bytes from activationMessage followed by 0 zeroes
        byte[] ctr = Bytes.concat(Arrays.copyOfRange(activationMessage, 7, 7 + 8), new byte[8]);

        // The method is called `decryptActivationMessage` regardless that the operation is `encrypt`
        return AES.encryptCtr(key, ctr, data);
    }

    public static byte[] encryptLogonId(byte[] key, byte[] signature, String logonId) {
        byte[] data = Bytes.concat(signature, logonId.getBytes());
        return AES.encryptEcbPkcs5(key, data);
    }

    public static byte[] decryptDynamicVector(byte[] key, byte[] dynamicVector) {
        byte[] data = Arrays.copyOfRange(dynamicVector, 32, 32+16);
        return AES.decryptEcbNoPadding(key, data);
    }

    public static byte[] encryptConstantA0(byte[] key) {
        byte[] data = new byte[] { (byte)0xa0 };
        return AES.encryptEcbPkcs5(key, data);
    }

    public static String modifyDiversifier(String diversifier, String otp) {
        StringBuilder out = new StringBuilder();
        for (int i=0; i<Math.max(diversifier.length(), otp.length()); i++) {
            byte dc = (byte)(diversifier.charAt(i % diversifier.length()) - 0x30);
            byte oc = (byte)(otp.charAt(i % otp.length()) - 0x30);

            out.append((char)((dc + oc) % 10 + 0x30));
        }
        return out.toString();
    }

    public static int extractDeviceCount(byte[] decryptedActivationMessage) {
        return (int)decryptedActivationMessage[5];
    }

    public static byte[] extractKey4(byte[] decryptedActivationMessage) {
        return Arrays.copyOfRange(decryptedActivationMessage, 6, 6+16);
    }
}
