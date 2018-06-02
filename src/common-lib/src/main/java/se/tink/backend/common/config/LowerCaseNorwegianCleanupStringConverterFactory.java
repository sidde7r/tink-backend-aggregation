package se.tink.backend.common.config;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import java.util.regex.Pattern;

public class LowerCaseNorwegianCleanupStringConverterFactory extends StringConverterFactory {
    private static final Joiner REGEXP_OR_JOINER = Joiner.on("|");

    private static final String BANK_NUMBER_PATTERN = "\\d{4}\\.\\d{2}\\.\\d{5}";
    private static final String LARGE_NUMBER_PATTERN = "^\\d{3,}| \\d{3,} |\\d{3,}$";
    private static final String CHARACTER_PATTERN = "[*._,]";
    private static final String DATE_PATTERN = "\\d{4}-\\d{2}-\\d{2}";
    private static final String ADDITIONAL_DATE_PATTERN = "\\d{2} \\d{2} \\d{2}";
    private static final String TIL_PATTERN = "til:";
    private static final String BETALT_PATTERN = "betalt:";

    private static final Pattern COMBINED_PATTERNS = Pattern.compile(REGEXP_OR_JOINER.join(ImmutableList
            .of(BANK_NUMBER_PATTERN, LARGE_NUMBER_PATTERN, CHARACTER_PATTERN, DATE_PATTERN,
                    ADDITIONAL_DATE_PATTERN, TIL_PATTERN)
            .stream().map(s -> "(" + s + ")").iterator()));
    private static final Pattern CLEANUP_PATTERN = Pattern.compile("\\s+");
    private static final Pattern START_AND_END_CLEANUP_PATTERN = Pattern.compile("^\\s+|\\s+$");

    public LowerCaseNorwegianCleanupStringConverterFactory() {
        builder.add(new LowerCaseStringConverterFactory().build())
                .add((String s) -> COMBINED_PATTERNS.matcher(s).replaceAll(" "))
                .add((String s) -> CLEANUP_PATTERN.matcher(s).replaceAll(" "))
                .add((String s) -> START_AND_END_CLEANUP_PATTERN.matcher(s).replaceAll(""));
    }
}
