package se.tink.backend.aggregation.agents.utils.charsetguesser;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * The purpose of this util class, and its main method `getEncoding`, is to guess/deduce the
 * possible charset that will cover all characters present in passed value.
 */
public class CharsetGuesser {

    // This return either a charset display name, or some artificial charsets for information
    // purposes.
    // This could be extended with more checks, but for now we are mostly interested in ISO-8859-1,
    // due to it being used as header values charset

    public static String getCharset(String value) {
        String regex = "^[a-zA-Z0-9]+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(value);

        if (StringUtils.isNumeric(value)) {
            return "NUMERIC";
        } else if (matcher.matches()) {
            return "ALPHANUMERIC";
        } else if (StandardCharsets.US_ASCII.newEncoder().canEncode(value)) {
            return StandardCharsets.US_ASCII.displayName();
        } else if (StandardCharsets.ISO_8859_1.newEncoder().canEncode(value)) {
            return StandardCharsets.ISO_8859_1.displayName();
        } else {
            return "OTHER";
        }
    }
}
