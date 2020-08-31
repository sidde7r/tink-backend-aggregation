package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator;

import se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass.utils.DataUtils;
import se.tink.backend.aggregation.agents.utils.crypto.MGF1;
import se.tink.backend.aggregation.agents.utils.crypto.TOTP;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class TotpCalculator {

    private TotpCalculator() {}

    static String calculateTOTP(int length, String mask, String pin, Long time) {
        byte[] maskedSeed = MGF1.generateMaskSHA1(pin.getBytes(), mask.length() / 2);
        String xor =
                EncodingUtils.encodeHexAsString(
                        DataUtils.xor(maskedSeed, EncodingUtils.decodeHexString(mask)));
        return TOTP.generateTotpIgnoreLast4bitsHmacSha1(xor, time, length);
    }
}
