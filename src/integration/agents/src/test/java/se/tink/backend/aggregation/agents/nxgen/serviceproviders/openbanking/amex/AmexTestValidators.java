package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex;

import org.junit.Ignore;

@Ignore
public class AmexTestValidators {

    public static String getSupplementalKeyMatchingRegex() {
        final String uniquePrefix = "tpcb_";
        final String uuid4First28CharsMatchingRegex =
                "[0-9a-f]{8}\\-[0-9a-f]{4}\\-4[0-9a-f]{3}\\-[89ab][0-9a-f]{3}\\-[0-9a-f]{8}";
        final String uniqueTinkTag = "feed";

        return String.format("%s%s%s", uniquePrefix, uuid4First28CharsMatchingRegex, uniqueTinkTag);
    }

    public static String createAuthHeaderMatchingPattern(String macId) {
        final String timestampMatchingRegex = "1[5-9][0-9]{8}";
        final String uuid4MatchingRegex =
                "[0-9a-f]{8}\\-[0-9a-f]{4}\\-4[0-9a-f]{3}\\-[89ab][0-9a-f]{3}\\-[0-9a-f]{12}";
        final String nonceSuffix = ":AMEX";
        final String simpleBase64MatchingRegex = "[a-zA-Z0-9+\\/]*={0,3}";

        return String.format(
                "MAC id=\"%s\",ts=\"%s\",nonce=\"%s%s\",mac=\"%s\"",
                macId,
                timestampMatchingRegex,
                uuid4MatchingRegex,
                nonceSuffix,
                simpleBase64MatchingRegex);
    }

    public static String getAmexRequestIdMatchingRegex() {
        return "[0-9a-f]{8}[0-9a-f]{4}4[0-9a-f]{3}[89ab][0-9a-f]{3}[0-9a-f]{12}";
    }
}
