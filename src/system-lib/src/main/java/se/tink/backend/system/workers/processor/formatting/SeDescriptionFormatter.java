package se.tink.backend.system.workers.processor.formatting;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import se.tink.backend.common.config.StripCurvePrefixStringConverterFactory;
import se.tink.backend.core.Transaction;
import se.tink.backend.utils.StringExtrapolator;

public class SeDescriptionFormatter implements DescriptionFormatter, DescriptionExtractor {
    private static final int MINIMUM_DESCRIPTION_LENGTH_TO_BE_EXTRAPOLATED = 10;
    private static final Pattern NO_REGULAR_CHARACTERS_PATTERN = Pattern.compile("[^a-zA-Z]+");

    private final BasicDescriptionFormatter fallback = new BasicDescriptionFormatter();
    private final StringExtrapolator extrapolator;

    private static final ImmutableMap<String, String> BROKEN_TO_READABLE_CHARACTERS = ImmutableMap.<String, String>builder()
            .put("}", "å") // Sk}netrafiken
            .put("{", "ä") // Fordonstj{nst I
            .put("¦", "ö") // Ekebo N¦jescentr
            .put("¨", "å") // GSk¨netrafiken
            .put("¯", "ä") // H¯ssleholm
            .put("Þ", "ö") // MALMÞ
            .put("þ", "ö") // þstgþta nation
            .put("»", "å") // SK»NETRAFIKEN
            .put("«", "ä") // RYDEB«CK
            .put("€", "å") // ASSISTANCEK€REN SWEDEN,  SUNDBYBERG
            .put("¢", "ä") // ONOFF Sverige AB Borl¢nge
            .put("§", "ä") // BOWLINGHALLEN,  STR§NGN§S
            .put("¬", "ä") // KEMTV¬TTSGRUPPE
            .put("±", "é") // 424 ÅHL±NS TRANÅS TRANÅS
            .build();

    private static final ImmutableSet<Function<String, String>> PREFIXES =
            ImmutableSet.<Function<String, String>>builder()
                .add(new StripCurvePrefixStringConverterFactory().build())
                .build();

    public SeDescriptionFormatter(StringExtrapolator extrapolator) {
        this.extrapolator = extrapolator;
    }

    @Override
    public String clean(String description) {
        return fallback.clean(description);
    }

    @Override
    public String extrapolate(String string) {

        if (Strings.isNullOrEmpty(string)) {
            return string;
        }

        String lowercased = string.toLowerCase();
        String extrapolated = lowercased;

        if (MINIMUM_DESCRIPTION_LENGTH_TO_BE_EXTRAPOLATED <= lowercased.length()) {
            extrapolated = extrapolator.extrapolate(lowercased, true);
        }

        // If the description was actually extrapolated, always return it in upper case (to apply human format later)
        if (!lowercased.equals(extrapolated)) {
            return extrapolated.toUpperCase();
        } else {
            return string;
        }
    }

    @Override
    public String getCleanDescription(Transaction transaction) {
        String description = transaction.getFormattedDescription();

        if (Strings.isNullOrEmpty(description)) {
            description = transaction.getOriginalDescription();
        }
        description = cleanupSpecialCharacters(description);
        description = stripPrefixes(description);

        return clean(description);
    }

    /**
     * Helper function for stripping description prefixes. Does not strip multiple prefixes unless they appear in same
     * order in the description as in `PREFIXES`-set.
     * @param description String
     * @return description with prefix stripped
     */
    private String stripPrefixes(String description) {
        String stripped = description;
        for(Function<String, String> p : PREFIXES) {
            stripped = p.apply(stripped);
        }
        return stripped;
    }

    // Replaces broken characters with their respective Swedish special character.
    private String cleanupSpecialCharacters(String description) {
        // Check if the string is all in caps (to know if we should replace with big or small letters).
        String letters = NO_REGULAR_CHARACTERS_PATTERN.matcher(description).replaceAll("");
        boolean isOnlyUpper = letters.equals(letters.toUpperCase());

        for (Map.Entry<String, String> pair : BROKEN_TO_READABLE_CHARACTERS.entrySet()) {
            description = description.replace(pair.getKey(), pair.getValue());
        }

        if (isOnlyUpper) {
            description = description.toUpperCase();
        }
        return description;
    }
}
