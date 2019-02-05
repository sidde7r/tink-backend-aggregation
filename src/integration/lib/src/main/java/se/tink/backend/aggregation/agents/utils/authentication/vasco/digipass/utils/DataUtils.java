package se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.utils;

import com.google.common.base.Preconditions;
import java.nio.ByteBuffer;

public class DataUtils {
    public static byte[] xor(byte[] a, byte[] b) {
        Preconditions.checkArgument(
                a.length == b.length,
                "Cannot XOR two different length byte arrays."
        );

        byte[] output = new byte[a.length];
        for (int i=0; i<a.length; i++) {
            output[i] = (byte)(a[i] ^ b[i]);
        }
        return output;
    }

    public static byte[] xorFirstBlock(byte[] data) {
        Preconditions.checkArgument(data.length == 8, "The data must be exactly 8 bytes.");

        byte[] output = data.clone();
        for (int i=0; i<4; i++) {
            output[i] = (byte)(data[i] ^ data[i+4]);
        }
        return output;
    }

    public static byte[] intToBytes(int v) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(v);
        return buffer.array();
    }

    public static byte[] longToBytes(long v) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(v);
        return buffer.array();
    }

    public static long bytesToLong(byte[] b) {
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

    public static long unsignedDiv(int i1, int i2) {
        long l1 = i1 & 0xffffffffL, l2 = i2 & 0xffffffffL;
        return l1 / l2;
    }

    public static byte[] swapBytes(byte[] input) {
        byte[] result = new byte[64];
        for (int i = 0; i < 32; i++) {
            result[i] = input[31 - i];
            result[i + 32] = input[63 - i];
        }
        return result;
    }
}
