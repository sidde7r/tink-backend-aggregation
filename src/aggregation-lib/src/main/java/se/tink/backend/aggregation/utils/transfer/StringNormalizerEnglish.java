package se.tink.backend.aggregation.utils.transfer;

import com.google.common.base.Joiner;
import java.util.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import java.text.Normalizer;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Changes some chars, like åäö to their corresponding visible english match. Other special chars are removed. Lastly
 * trims the string from white spaces.
 *
 * Examples:
 * åäöÅÄÖ -> aaoAAO
 * ß -> s
 * Ø -> O
 * $ -> '' (empty string)
 * test$ $ ∞£@ ß åäö -> test s aao
 *
 * It's possible to construct this class with a white list, that makes it possible to e.g. white list swedish chars
 *
 * TODO: Improve performance when using larger strings and white listing
 */
public class StringNormalizerEnglish implements StringNormalizer {
    /**
     * Ref: http://stackoverflow.com/questions/1453171/remove-diacritical-marks-%C5%84-%C7%B9-%C5%88-%C3%B1-%E1%B9%85-%C5%86-%E1%B9%87-%E1%B9%8B-%E1%B9%89-%CC%88-%C9%B2-%C6%9E-%E1%B6%87-%C9%B3-%C8%B5-from-unicode-chars
     */
    private static final Pattern DIACRITICS
            = Pattern.compile("["
            + "\\p{InCombiningDiacriticalMarks}"
            + "\\p{IsLm}"
            + "\\p{IsSk}]+");

    private static final ImmutableMap<Character, Character> SPECIALCHARS = ImmutableMap.<Character, Character>builder()
            // Replace some specials as their equivalent characters
            .put('ß', 's')
            .put('æ', 'a')
            .put('Æ', 'A')
            .put('ø', 'o')
            .put('Ø', 'O')
            .build();

    private static final Pattern ENGLISH_CHARACTER = Pattern.compile("[a-zA-Z0-9\\s]");

    protected final Optional<Set<Character>> whiteListedCharacters;

    public StringNormalizerEnglish() {
        whiteListedCharacters = Optional.empty();
    }

    public StringNormalizerEnglish(Set<Character> whiteListedCharacters) {
        Preconditions.checkNotNull(whiteListedCharacters);

        this.whiteListedCharacters = Optional.of(whiteListedCharacters);
    }

    @Override
    public String normalize(String str) {
        if (Strings.isNullOrEmpty(str)) {
            return str;
        }

        return normalize(str, whiteListedCharacters);
    }

    /**
     * TODO: Improve performance (skip looping each char)
     * Let's loop through the characters, although not perfect performance.
     * This is not awesome for large strings, but not meant to be used with large strings
     */
    private static String normalize(String str, Optional<Set<Character>> whiteListedCharacters) {
        Set<Character> whiteList = whiteListedCharacters.orElse(null);

        StringBuilder stringBuilder = new StringBuilder();
        for (Character character : str.toCharArray()) {
            if (whiteList != null && whiteList.contains(character)) {
                stringBuilder.append(character);
                continue;
            }

            character = normalize(character);
            if (character != null) {
                stringBuilder.append(character);
            }
        }

        return stringBuilder.toString();
    }

    private static Character normalize(Character character) {
        character = stripDiacritics(character);
        // Alone ^ or ~ without a letter underneath will be removed -> null
        if (character == null) {
            return null;
        }

        // Other special chars that have good representations in english letters, e.g. Ø -> O
        character = replaceSpecialCharWithEquivalent(character);

        // If result is not within english chars, return null
        if (!isEnglishCharacter(character)) {
            return null;
        }

        return character;
    }

    @Override
    public String getUnchangedCharactersHumanReadable() {
        return createUnchangedCharactersHumanReadable("a-z A-Z 0-9", this.whiteListedCharacters);
    }

    protected static String createUnchangedCharactersHumanReadable(
            String baseVocabulary, Optional<Set<Character>> whiteListedCharacters) {
        if (!whiteListedCharacters.isPresent()) {
            return baseVocabulary;
        }

        StringBuilder stringBuilder = new StringBuilder(baseVocabulary);

        Set<Character> characters = whiteListedCharacters.get();
        if (!characters.isEmpty()) {
            String whiteListString = Joiner.on(" ").join(characters);
            stringBuilder
                    .append(" ")
                    .append(whiteListString);
        }

        return stringBuilder.toString();
    }

    private static Character stripDiacritics(char character) {
        String str = Character.toString(character);
        str = Normalizer.normalize(str, Normalizer.Form.NFD);
        str = DIACRITICS.matcher(str).replaceAll("");
        return !Strings.isNullOrEmpty(str) ? str.charAt(0) : null;
    }

    private static char replaceSpecialCharWithEquivalent(char singleChar) {
        Character replace = SPECIALCHARS.get(singleChar);
        return replace != null ? replace : singleChar;
    }

    private static boolean isEnglishCharacter(char character) {
        String str = Character.toString(character);
        return ENGLISH_CHARACTER.matcher(str).matches();
    }
}
