package se.tink.backend.common.config;

import java.util.regex.Pattern;

public class LowerCaseNoDashesNoLoneDigitsStringConverterFactory extends StringConverterFactory {
    private static final Pattern DASHES_PATTERN = Pattern.compile("-");
    private static final Pattern LONE_DIGITS_PATTERN = Pattern.compile("(^|\\s+)(\\d+)(\\s+|$)");

    public LowerCaseNoDashesNoLoneDigitsStringConverterFactory() {
        builder.add(new LowerCaseStringConverterFactory().build())
                .add((String s) -> LONE_DIGITS_PATTERN.matcher(s).replaceAll("$1$3"))
                .add((String s) -> DASHES_PATTERN.matcher(s).replaceAll(" ").trim());
    }
}
