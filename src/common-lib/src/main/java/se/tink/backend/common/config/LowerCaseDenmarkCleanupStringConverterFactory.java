package se.tink.backend.common.config;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import java.util.regex.Pattern;

public class LowerCaseDenmarkCleanupStringConverterFactory extends StringConverterFactory {
    private static final Joiner REGEXP_OR_JOINER = Joiner.on("|");

    private static final Pattern AE_PATTERN = Pattern.compile("æ");
    private static final Pattern OE_PATTERN = Pattern.compile("ø");
    private static final Pattern A_PATTERN = Pattern.compile("å");

    private static final Pattern DANKORT_PATTERN = Pattern.compile("\\bdankort\\b");
    private static final Pattern DSB_PATTERN = Pattern.compile("\\bdsb\\b");
    private static final Pattern DANKORT_NOTA_PATTERN = Pattern.compile("dankort-nota");
    private static final Pattern VISA_PATTERN = Pattern.compile("\\bvisa\\b");
    private static final Pattern VISA_DANKORT_PATTERN = Pattern.compile("visa/dankort");
    private static final Pattern MASTERCARD_PATTERN = Pattern.compile("mastercard");
    private static final Pattern NOTA_PATTERN = Pattern.compile("\\bnota\\b");
    private static final Pattern NOTA_Z_PATTERN = Pattern.compile("nota z");
    private static final Pattern KONTAKTLOS_PATTERN = Pattern.compile("kontaktløs|kontaktloes");
    private static final Pattern ATM_PATTERN = Pattern.compile("\\batm\\b");
    private static final Pattern UDBET_PATTERN = Pattern.compile("\\budbet\\b");

    // Ordering matters: visa/dankort before visa, otherwise it would result in a single "/"
    private static final Pattern COMBINED_PATTERNS = Pattern.compile(REGEXP_OR_JOINER.join(ImmutableList
            .of(DSB_PATTERN, DANKORT_NOTA_PATTERN, DANKORT_PATTERN, VISA_DANKORT_PATTERN, VISA_PATTERN,
                    MASTERCARD_PATTERN, NOTA_Z_PATTERN, NOTA_PATTERN, KONTAKTLOS_PATTERN, ATM_PATTERN, UDBET_PATTERN)
            .stream().map(s -> "(" + s + ")").iterator()));
    private static final Pattern CLEANUP_PATTERN = Pattern.compile("\\s+");
    private static final Pattern START_AND_END_CLEANUP_PATTERN = Pattern.compile("^\\s+|\\s+$");

    public LowerCaseDenmarkCleanupStringConverterFactory() {
        builder.add(new LowerCaseNoDashesNoLoneDigitsStringConverterFactory().build())
                .add((String s) -> AE_PATTERN.matcher(s).replaceAll("ae"))
                .add((String s) -> OE_PATTERN.matcher(s).replaceAll("oe"))
                .add((String s) -> A_PATTERN.matcher(s).replaceAll("a"))
                .add((String s) -> COMBINED_PATTERNS.matcher(s).replaceAll(" "))
                .add((String s) -> CLEANUP_PATTERN.matcher(s).replaceAll(" "))
                .add((String s) -> START_AND_END_CLEANUP_PATTERN.matcher(s).replaceAll(""));
    }
}
