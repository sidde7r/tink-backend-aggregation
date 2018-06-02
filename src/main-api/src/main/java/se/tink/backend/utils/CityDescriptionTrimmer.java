package se.tink.backend.utils;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.googlecode.concurrenttrees.common.KeyValuePair;
import com.googlecode.concurrenttrees.radix.RadixTree;
import java.util.Set;

/**
 * Utility class for trimming away country and city information from a string.
 * <p>
 * Example usage:
 * trim("H&M Amsterdam") => "H&M"
 * trim("H&M Amsterdam NLD") => "H&M"
 * trim("H&M Stockholm SE") => "H&M"
 * <p>
 * Limitations:
 * - The trimming is done by first removing country and then removing the city, that means that it "Kiev SE" would be
 * removed even if it isn't a city in Sweden.
 */
public class CityDescriptionTrimmer {

    private static final LogUtils log = new LogUtils(CityDescriptionTrimmer.class);

    private static final Joiner SPACE_JOINER = Joiner.on(" ");

    // Setting used for fuzzy matching. Needs to be quite long if a lot of cities are loaded
    private static final int FUZZY_TRIMMING_PREFIX_LENGTH = 6;

    private RadixTree cityPatternTree;
    private RadixTree countryPatternTree;

    private enum MatchType {
        EXACT,
        FUZZY
    }

    CityDescriptionTrimmer(RadixTree countryPatternTree, RadixTree cityPatternTree) {

        this.cityPatternTree = cityPatternTree;
        this.countryPatternTree = countryPatternTree;
    }

    public static CityDescriptionTrimmerBuilder builder() {
        return new CityDescriptionTrimmerBuilder();
    }

    public String trimWithFuzzyFallback(String description) {
        return trim(description, true);
    }

    public String trim(String description) {
        return trim(description, false);
    }

    /**
     * Trim away country and city information from a description. Use a fuzzy fallback if all information couldn't
     * be trimmed away.
     */
    private String trim(String description, boolean fuzzyFallback) {

        if (Strings.isNullOrEmpty(description)) {
            return description;
        }

        if (countryPatternTree == null || cityPatternTree == null) {
            log.warn("Pattern trees have not been initialized");
            return description;
        }

        // Split up the description into words / parts
        ImmutableList<String> words = ImmutableList.copyOf(description.split(" "));

        // Start by trimming country from the end of the string
        ImmutableList<String> result = trimCountry(words);

        // Continue with trimming as many cities as possible

        while (true) {

            ImmutableList<String> trimmedCity = trimCity(result, MatchType.EXACT);

            // Try with fuzzy if we didn't trimmed the city
            if (fuzzyFallback && result.size() == trimmedCity.size()) {
                trimmedCity = trimCity(trimmedCity, MatchType.FUZZY);
            }

            // Break if we didn't trim anything or update the result and do a new try
            if (result.size() == trimmedCity.size()) {
                break;
            } else {
                result = trimmedCity;
            }
        }

        // Return the full result back
        return SPACE_JOINER.join(result);
    }

    private ImmutableList<String> trimCity(ImmutableList<String> words, MatchType matchType) {

        switch (matchType) {
        case EXACT:
            return trimCityExactMatch(words);
        case FUZZY:
            return trimCityFuzzyMatch(words);
        default:
            return words;
        }
    }

    /**
     * Trim ending country code from a list of words.
     */
    private ImmutableList<String> trimCountry(ImmutableList<String> words) {

        String lastWord = words.get(words.size() - 1);

        Object match = countryPatternTree.getValueForExactKey(lastWord.toLowerCase());

        if (match == null) {
            return words;
        } else {
            return words.subList(0, words.size() - 1);
        }
    }

    /**
     * Trim city from a description / list if words. Try to match as much as possible of the input. If the input
     * is "H&M ABC Alphen aan den Rijn" the following patterns will be tried
     * "H&M ABC123 Alphen aan den Rijn" => No match
     * "ABC123 Alphen aan den Rijn"     => No match
     * "Alphen aan den Rijn"            => Match (a dutch city)
     */
    private ImmutableList<String> trimCityExactMatch(ImmutableList<String> words) {

        for (int i = 0; i < words.size(); i++) {

            String patternToTest = FluentIterable.from(words).skip(i).join(SPACE_JOINER);

            // All tree nodes are in lower case
            Object match = cityPatternTree.getValueForExactKey(patternToTest.toLowerCase());

            if (match != null) {
                return words.subList(0, i);
            }
        }

        return words;
    }

    /**
     * Fuzzy trim city from a description / list if words.
     *
     * Different combinations will be tried until we find one that matches.
     *
     * An example:
     * Full description: "H&M XYZ Alphen aan den Rijn" (Alphen aan den Rijn is the city)
     * Input to this method: "H&M XYZ Alphen aan"
     *
     * 1st iteration: "H&M XYZ Alphen aan" => Not found
     * 2nd iteration: "XYZ Alphen aan" => Not found
     * 3rd iteration: "Alphen aan" => City found
     */
    private ImmutableList<String> trimCityFuzzyMatch(ImmutableList<String> words) {

        for (int i = 0; i < words.size(); i++) {

            String patternToTest = FluentIterable.from(words).skip(i).join(SPACE_JOINER);

            if (patternToTest.length() < FUZZY_TRIMMING_PREFIX_LENGTH) {
                return words;
            }

            // Get the keys that starts with the last word, tree is in lower case
            Set<KeyValuePair> result = cityPatternTree.getKeyValuePairsForKeysStartingWith(patternToTest.toLowerCase());

            if (!result.isEmpty()) {
                return words.subList(0, i);
            }
        }

        return words;
    }
}
