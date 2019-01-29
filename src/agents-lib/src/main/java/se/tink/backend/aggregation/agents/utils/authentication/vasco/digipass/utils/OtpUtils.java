package se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.utils;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Bytes;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.DigipassConstants;
import se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.StaticVector;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class OtpUtils {
    public static String calculateOtp(String fingerprint, StaticVector staticVector, byte[] otpKey,
            int otpCounter, long epochTime, List<byte[]> challenges) {

        byte[] diversifier = FingerPrintUtils.getFingerPrintDiversifier(fingerprint, staticVector);

        byte[] mashup = mashupChallenge(otpKey, diversifier, otpCounter, epochTime, challenges);
        byte[] encryptedMashup = CryptoUtils.aes8(otpKey, mashup);

        byte[] rawOtpResponse = convertOtpResponse(encryptedMashup);
        return convertOtpToAscii(rawOtpResponse);
    }

    public static byte[] mashupChallenge(byte[] key, byte[] diversifier, int counter, long epochTime,
            List<byte[]> challenges) {
        Preconditions.checkArgument(!challenges.isEmpty(), "Challenges were empty.");

        byte[] challenge0 = challenges.get(0);
        byte[] ctr_diversifier = Bytes.concat(DataUtils.intToBytes(counter), diversifier);

        byte[] stage1 = CryptoUtils.aes8(key, ctr_diversifier);

        // Todo: read configuration from CryptoApplication:1 to decide if we should do this
        byte[] timeXoredStage1 = xorWithTime(stage1, epochTime);
        byte[] stage2 = CryptoUtils.aes8(key, timeXoredStage1);

        byte[] challenge0Xored = DataUtils.xor(challenge0, stage2);

        byte[] xorKey = CryptoUtils.getXorKey(key);

        if (challenges.size() == 1) {
            return DataUtils.xor(challenge0Xored, xorKey);
        }

        byte[] lastChallenge = challenges.get(challenges.size()-1);
        byte[] lastChallengeXored = DataUtils.xor(xorKey, lastChallenge);

        byte[] output = challenge0Xored;

        for (int i=1; i<challenges.size()-1; i++) {
            output = Bytes.concat(output, challenges.get(i));
        }

        return Bytes.concat(output, lastChallengeXored);
    }

    public static byte[] xorWithTime(byte[] data, long epochTime) {
        epochTime >>= 4;

        byte[] epochBytes = DataUtils.longToBytes(epochTime);
        return DataUtils.xor(data, epochBytes);
    }

    private static byte[] convertOtpResponse(byte[] data) {
        // Todo: read configuration from CryptoApplication:1 to decide if we should do this
        data = DataUtils.xorFirstBlock(data);

        // Divide the lower and upper half (32 bit values) of the calculated response value by
        // diminishing powers of 10.
        // Merge the resulting array into one where each entry is a nibble.

        int l = DataUtils.bytesToInt(Arrays.copyOfRange(data, 0, 4));
        int h = DataUtils.bytesToInt(Arrays.copyOfRange(data, 4, 8));

        byte[] barray = new byte[20];
        int m = 1000000000;
        for (int i=0; i<10; i++,m/=10) {
            barray[i] = (byte)(DataUtils.unsignedDiv(l, m) % 0x0a);
            barray[i+10] = (byte)(DataUtils.unsignedDiv(h, m) % 0x0a);
        }

        byte[] output = new byte[10];
        for (int i=0,j=0; i<10; i++,j+=2) {
            output[i] = (byte)(barray[j+1] + 16 * barray[j]);
        }
        return output;
    }

    private static String convertOtpToAscii(byte[] otp) {
        String s = EncodingUtils.encodeHexAsString(otp);
        return s.substring(s.length() - 16);
    }

    public static String calculateDerivationCode(String fingerprint, StaticVector staticVector, String otpResponse) {
        byte[] diversifierBytes = FingerPrintUtils.getFingerPrintDiversifier(fingerprint, staticVector);

        // Note: must use Long instead of Int even though the diversifier is only 32 bits. This is due to
        // Java and the lack of unsigned ints...
        String diversifierString = Long.toString(DataUtils.bytesToLong(diversifierBytes));

        Optional<Integer> diversifierLength = staticVector.getFieldAsInt(
                DigipassConstants.StaticVectorFieldType.DIVERSIFIER_LENGTH);

        if (diversifierLength.isPresent()) {
            diversifierString = StringUtils.leftPad(diversifierString, diversifierLength.get(), '0');
        }

        // Todo: read configuration from CryptoApplication if checksum is needed (not needed at the moment)
        // In that case we must remove the last char from otpResponse and replace it with a checksum.

        String mergedDiversifier = mergeDiversifierAndOtp(diversifierString, otpResponse);
        return mergedDiversifier + otpResponse;
    }

    private static String mergeDiversifierAndOtp(String diversifier, String otp) {
        StringBuilder out = new StringBuilder();
        for (int i=0; i<Math.min(diversifier.length(), otp.length()); i++) {
            byte dc = (byte)(diversifier.charAt(i % diversifier.length()) - 0x30);
            byte oc = (byte)(otp.charAt(i % otp.length()) - 0x30);

            out.append((char)((dc + oc) % 10 + 0x30));
        }
        return out.toString();
    }
}
