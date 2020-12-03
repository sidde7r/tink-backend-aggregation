package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.helper;

import java.nio.ByteBuffer;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.FortisConstants;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class FortisOTPCalculator {

    /**
     * Calculates OTP using OCRA OATH Protocol. Prepares timestamp to fit the protocol settings
     *
     * @param keyHex a key encoded in hex string
     * @param challenge a challenge received from server
     * @param timestamp a timestamp without milliseconds
     * @return calculated OTP
     */
    public static String calculateOTP(String keyHex, String challenge, Long timestamp) {
        long t10s = timestamp / 10; // 10 seconds units because of OCRA T10S
        byte[] t10sBytes = longToBytes(t10s);

        return FortisOCRA.generateOCRA(
                FortisConstants.Encryption.OCRA_T10S,
                keyHex,
                null,
                challenge,
                null,
                null,
                EncodingUtils.encodeHexAsString(t10sBytes));
    }

    private static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }
}
