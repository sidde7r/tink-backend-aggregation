package se.tink.backend.common.config;

import java.util.regex.Pattern;

public class LowerCaseOnlyAZStringConverterFactory extends StringConverterFactory {
    private static final Pattern AA_UMLAUT_PATTERN = Pattern.compile("[åäÅÄ]");
    private static final Pattern O_UMLAUT_PATTERN = Pattern.compile("[öøÖØ]");
    private static final Pattern CLEANUP_PATTERN = Pattern.compile("[^\\w ]|\\d+|_");

    public LowerCaseOnlyAZStringConverterFactory() {
        builder.add(new LowerCaseStringConverterFactory().build())
                .add((String s) -> AA_UMLAUT_PATTERN.matcher(s).replaceAll("a"))
                .add((String s) -> O_UMLAUT_PATTERN.matcher(s).replaceAll("o"))
                .add((String s) -> CLEANUP_PATTERN.matcher(s).replaceAll(""));
    }
}
