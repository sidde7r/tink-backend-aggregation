package se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.utils;

import java.util.Optional;
import se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.DigipassConstants;
import se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.StaticVector;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;

public class FingerPrintUtils {
    public static String generateFingerPrint() {
        byte[] fp = RandomUtils.secureRandom(32);
        return EncodingUtils.encodeHexAsString(fp);
    }

    public static byte[] getFingerPrintDiversifier(String fingerprint, StaticVector staticVector) {
        byte[] fpHash = Hash.sha256(fingerprint);
        long v = (fpHash[3] & 0xff) |
                ((fpHash[2] & 0xff) << 8) |
                ((fpHash[1] & 0xff) << 16) |
                ((fpHash[0] & 0xff) << 24);

        Optional<Long> initialValue = staticVector.getFieldAsLong(
                DigipassConstants.StaticVectorFieldType.INITIAL_VALUE);
        Optional<Integer> diversifierLength = staticVector.getFieldAsInt(
                DigipassConstants.StaticVectorFieldType.DIVERSIFIER_LENGTH);

        int ret = (int)(v & 0xffffffffL);

        if (initialValue.isPresent() && diversifierLength.isPresent()) {
            int CONSTANT = 3; // Unknown from where this value is taken from, possibly type `8` or `56`
            v = (CONSTANT + 32 * (v & 0xffffffffL)) & 0xffffffffL;

            long i = initialValue.get();
            int pow = diversifierLength.get();

            for (int j=0; j<pow; j++) {
                i *= 10;
            }
            ret = (int)(v - (v/i * i));
        }

        return DataUtils.intToBytes(ret);
    }
}
