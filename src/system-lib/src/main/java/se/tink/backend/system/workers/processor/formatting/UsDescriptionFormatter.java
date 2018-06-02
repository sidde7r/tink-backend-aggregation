package se.tink.backend.system.workers.processor.formatting;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.regex.Pattern;

public class UsDescriptionFormatter implements DescriptionFormatter {

    private final static ImmutableList<String> NOISE_PATTERNS_RAW = ImmutableList.of(
            "RECUR DEBIT CRD PMT",
            "POS PURCHASE - ",
            "DEBIT CRD PUR INTL ",
            "CHECK CRD PURCHASE ",
            "CHECK CRD PUR RTRN ",
            "VISA DDA PUR\\d+",
            "VISA DDA REF\\d+",
            "CHECKCARD ",
            "CHKCARD",
            "POS ",
            "(?<=^|\\s)[^\\s]*xxx[^\\s]*(?=\\s|$)"
    ); // Tokens containing "xxx"

    private final static ImmutableList<Pattern> NOISE_PATTERNS = ImmutableList.copyOf(Lists.transform(
            NOISE_PATTERNS_RAW,
            usNoisePattern -> Pattern.compile(usNoisePattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL)
    ));

    private BasicDescriptionFormatter defaultImplementation = new BasicDescriptionFormatter();

    @Override
    public String clean(String description) {

        String cleaned = defaultImplementation.clean(description);

        if (Strings.isNullOrEmpty(cleaned)) {
            return cleaned;
        }

        for (Pattern pattern : NOISE_PATTERNS) {
            cleaned = CharMatcher.WHITESPACE.trimAndCollapseFrom(pattern.matcher(cleaned).replaceAll(" "), ' ');
        }

        return cleaned;
    }

    @Override
    public String extrapolate(String description) {
        return defaultImplementation.extrapolate(description);
    }
}
