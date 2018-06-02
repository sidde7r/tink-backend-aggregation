package se.tink.backend.system.workers.processor.formatting;

import com.google.common.base.CharMatcher;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.regex.Pattern;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.utils.CityDescriptionTrimmer;

public class NlDescriptionFormatter implements DescriptionFormatter, DescriptionExtractor {

    private final static ImmutableList<String> NOISE_PATTERNS_RAW = ImmutableList.of(
            "[ ,]PAS\\d{3,4}($|( .*))",
            "^CCV\\*",
            " B\\.?V\\.?[^\\s\\w]*(\\s|$)",
            " N\\.?V\\.?[^\\s\\w]*(\\s|$)",
            "By Adyen$"
    );

    private final BasicDescriptionFormatter fallback = new BasicDescriptionFormatter();

    private final static ImmutableList<Pattern> NOISE_PATTERNS = ImmutableList.copyOf(Lists.transform(
            NOISE_PATTERNS_RAW, pattern -> Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL)));

    private final CityTrimmer cityTrimmer;

    /**
     * Default constructor.
     */
    public NlDescriptionFormatter() {
        cityTrimmer = new DefaultCityTrimmer(CityDescriptionTrimmer.builder().build());
    }

    /**
     * Alternate constructor for custom city trimming.
     */
    public NlDescriptionFormatter(CityTrimmer cityTrimmer) {
        this.cityTrimmer = cityTrimmer;
    }

    @Override
    public String clean(String description) {

        String cleaned = fallback.clean(description);

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
        return fallback.extrapolate(description);
    }

    @Override
    public String getCleanDescription(Transaction transaction) {
        String description = transaction.getFormattedDescription();

        if (Strings.isNullOrEmpty(description)) {
            description = transaction.getOriginalDescription();
        }

        String cleaned = clean(description);

        // If the original description is the same as the external account name, don't trim the city (since it's most
        // likely somebody's last name).
        String counterpart = transaction.getPayloadValue(TransactionPayloadTypes.TRANSFER_ACCOUNT_NAME_EXTERNAL);

        if (Objects.equal(transaction.getOriginalDescription(), counterpart)) {
            return cleaned;
        }

        String cleanedCity = cityTrimmer.trim(cleaned);

        if (Strings.isNullOrEmpty(cleanedCity)) {
            return cleaned;
        } else {
            return cleanedCity;
        }

    }
}
