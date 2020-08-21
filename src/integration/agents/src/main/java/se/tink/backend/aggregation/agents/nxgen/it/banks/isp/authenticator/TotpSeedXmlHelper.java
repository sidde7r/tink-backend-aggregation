package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TotpSeedXmlHelper {

    private static final String TOTP_MASK_PATTERN = "<cs>.*UDK_=(.+?)::.*<\\/cs>";
    private static final String TOTP_DIGITS_PATTERN = "<cs>.*DIGS=(.+?)::.*<\\/cs>";
    private static final String TOTP_TYPE_PATTERN = "<cs>.*TYPE=(.+?)::.*<\\/cs>";
    private static final String TOTP_TYPE = "TOTP";

    private TotpSeedXmlHelper() {}

    static void validateTotpType(String seedXml) {
        Matcher m = Pattern.compile(TOTP_TYPE_PATTERN).matcher(seedXml);
        m.find();
        String type = m.group(1);
        if (!TOTP_TYPE.equals(type)) {
            throw new IllegalArgumentException("Unknown type of otp requested");
        }
    }

    static String getTotpMask(String seedXml) {
        Matcher m = Pattern.compile(TOTP_MASK_PATTERN).matcher(seedXml);
        m.find();
        return m.group(1);
    }

    static int getTotpDigits(String seedXml) {

        Matcher matcher = Pattern.compile(TOTP_DIGITS_PATTERN).matcher(seedXml);
        matcher.find();
        return Integer.parseInt(matcher.group(1));
    }
}
