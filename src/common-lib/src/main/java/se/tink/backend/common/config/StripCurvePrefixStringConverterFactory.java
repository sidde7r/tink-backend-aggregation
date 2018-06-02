package se.tink.backend.common.config;

import java.util.regex.Pattern;

public class StripCurvePrefixStringConverterFactory extends StringConverterFactory {
    // Curve cards adds "Crv*"-prefixes on their transactions https://www.imaginecurve.com/
    private static final Pattern pattern = Pattern.compile("^(Crv\\*)", Pattern.CASE_INSENSITIVE);

    public StripCurvePrefixStringConverterFactory() {
        builder.add((String s) -> pattern.matcher(s).replaceAll(""));
    }
}
