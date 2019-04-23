package se.tink.libraries.strings;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.libraries.uuid.UUIDUtils;

public class StringUtils {
    private static final Logger log = LoggerFactory.getLogger(StringUtils.class);
    private static final double HUMAN_FORMATTING_UPPERCASE_THRESHOLD = 0.80;
    private static final Joiner JOINER = Joiner.on(" ").skipNulls();
    public static final char NON_BREAKING_WHITESPACE = (char) 160;
    private static final Splitter SPLITTER = Splitter.on(" ").trimResults().omitEmptyStrings();
    private static final Splitter TAB_SPLITTER = Splitter.on('\t').trimResults();
    private static final Splitter COMMA_SPLITTER =
            Splitter.on(',').trimResults().omitEmptyStrings();
    private static final Splitter CSV_SPLITTER = Splitter.on(",").trimResults(CharMatcher.is('"'));

    public static String generateUUID() {
        return UUIDUtils.toTinkUUID(UUID.randomUUID());
    }

    public static String insertPeriodically(String text, char insert, int period) {
        return text.replaceAll("(.{" + period + "})", "$1" + insert);
    }

    public static String firstLetterUppercaseFormatting(String cleanDescription) {
        Iterable<String> words = SPLITTER.split(cleanDescription);

        StringBuilder builder = new StringBuilder();

        for (String word : words) {
            builder.append(formatHumanWord(word));
            builder.append(" ");
        }

        return builder.toString().trim();
    }

    public static String formatCity(String description) {
        Iterable<String> words = SPLITTER.split(description);

        StringBuilder result = new StringBuilder();

        for (String word : words) {
            result.append(formatCityWord(word));
            result.append(" ");
        }

        String[] words2 = result.toString().trim().split("-");

        StringBuilder result2 = new StringBuilder();

        for (int i = 0; i < words2.length; i++) {
            if (i > 0) result2.append("-");
            result2.append(formatCityWord(words2[i]));
        }

        return result2.toString();
    }

    public static String formatCode(String input) {
        return stripNonAsciiCharacters(
                input.toLowerCase().replace(" ", "-").replace("&", "-").replace(",", ""));
    }

    private static String formatCityWord(String description) {
        if (description.length() <= 1) return description.toUpperCase();

        return description.substring(0, 1).toUpperCase() + description.substring(1).toLowerCase();
    }

    public static String formatHuman(String description) {

        if (Strings.isNullOrEmpty(description)) return description;

        String cleanDescription =
                description
                        .replace("(", "")
                        .replace(")", "")
                        .replace("\"", "")
                        .replace("!", "")
                        .replace("¤", "")
                        .replace("", "");

        cleanDescription =
                JOINER.join(SPLITTER.split(CharMatcher.whitespace().trimFrom(cleanDescription)));

        if (cleanDescription.toUpperCase().startsWith("WWW")) return cleanDescription.toLowerCase();

        String result = null;

        if (shouldFormatHumanDescription(description)) {
            result = firstLetterUppercaseFormatting(cleanDescription);
        } else {
            result = cleanDescription;
        }

        if (result.toUpperCase().startsWith("AB ")) result = result.substring(3);
        if (result.toUpperCase().endsWith(" AB")) result = result.substring(0, result.length() - 3);
        if (result.toUpperCase().startsWith("HB ")) result = result.substring(3);
        if (result.toUpperCase().endsWith(" HB")) result = result.substring(0, result.length() - 3);
        if (result.toUpperCase().endsWith(" -")) result = result.substring(0, result.length() - 2);
        if (result.toUpperCase().endsWith(" AKTIEBOLAG"))
            result = result.substring(0, result.length() - 11);
        if (result.toUpperCase().startsWith("AKTIEBOLAGET ")) {
            result = result.substring(13, result.length());
        }

        return result;
    }

    protected static String formatHumanWord(String description) {
        if (description.length() == 0) return "";

        return description.substring(0, 1).toUpperCase() + description.substring(1).toLowerCase();
    }

    public static String calculateHMAC(String data, String key) {
        try {
            Mac SHA256_HMAC = Mac.getInstance("HmacSHA256");

            SHA256_HMAC.init(new SecretKeySpec(Charsets.UTF_8.encode(key).array(), "HmacSHA256"));

            return new String(
                    Hex.encodeHex(SHA256_HMAC.doFinal(Charsets.UTF_8.encode(data).array())));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Helper function for MD5 hashing to a hex encoded string.
     *
     * @param string
     * @return
     */
    public static String hashAsStringMD5(String string) {
        return new String(Hex.encodeHex(hashMD5(string)));
    }

    /**
     * Helper function for SHA1 hashing to a hex encoded string.
     *
     * @param string
     * @return
     */
    public static String hashAsStringSHA1(String string) {
        return new String(Hex.encodeHex(hashSHA1(string)));
    }

    /**
     * Helper function for SHA1 hashing using a salt to a hex encoded string.
     *
     * @param string
     * @return
     */
    public static String hashAsStringSHA1(String string, String salt) {
        return new String(Hex.encodeHex(hashSHA1(string.concat(salt))));
    }

    /**
     * Helper function to hash a string into something that looks like a UUID.
     *
     * @param string the input on which the UUID should be based on.
     * @return UUID string look-alike based off of string.
     * @note This method does _not_ generate fully proper UUIDs. For that, it must set correct UUID
     *     type bit(s). See
     *     http://en.wikipedia.org/wiki/Universally_unique_identifier#Version_3_.28MD5_hash_.26_namespace.29
     *     for description on how to do this properly.
     */
    public static String hashAsUUID(String string) {
        try {
            String result = hashAsStringMD5(string);

            return (result.substring(0, 8)
                            + "-"
                            + result.substring(8, 12)
                            + "-"
                            + result.substring(12, 16)
                            + "-"
                            + result.substring(16, 20)
                            + "-"
                            + result.substring(20))
                    .toUpperCase();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Helper function for MD5 hashing. Hashes the US-ASCII interpretation of the input string.
     *
     * @param string to be hashed
     * @return
     */
    public static byte[] hashMD5(String string) {
        try {
            MessageDigest hasher = MessageDigest.getInstance("MD5");

            return hasher.digest(string.getBytes(Charsets.US_ASCII));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Could not initialize MD5 message digest", e);
        }
    }

    /**
     * Helper function for SHA-1 hashing. Hashes the US-ASCII interpretation of the input string.
     *
     * @param string to be hashed.
     * @return
     */
    public static byte[] hashSHA1(String string) {
        try {
            MessageDigest hasher = MessageDigest.getInstance("SHA-1");

            return hasher.digest(string.getBytes(Charsets.US_ASCII));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Could not initialize SHA-1 message digest", e);
        }
    }

    /**
     * Helper function to parse an arbitrary string into an amount.
     *
     * @param input
     * @return
     */
    public static double parseAmount(String input) {
        if (input == null) return 0;

        String text = CharMatcher.whitespace().removeFrom(input);

        char decimalSeparator = ' ';

        // Find a decimal separator candidate
        for (int i = text.length() - 1; i > -1; i--) {
            if (text.charAt(i) == '.' || text.charAt(i) == ',' || text.charAt(i) == '\'') {
                decimalSeparator = text.charAt(i);
                break;
            }
        }

        // A decimal separator may only appear once (otherwise it's not a decimal separator)
        if (text.indexOf(decimalSeparator) != text.lastIndexOf(decimalSeparator)) {
            decimalSeparator = ' ';
        }

        // Remove all non-decimal separator separators
        if (decimalSeparator != '.') text = text.replace(".", "");
        if (decimalSeparator != ',') text = text.replace(",", "");
        if (decimalSeparator != '\'') text = text.replace("'", "");

        // Make the decimal separator a dot
        text = text.replace(decimalSeparator, '.');

        try {
            return Double.parseDouble(text);
        } catch (Exception e) {
            log.warn("Cannot parse amount: " + input, e);
            return 0;
        }
    }

    /**
     * Helper function to parse an arbitrary string into an amout using US standard US standard uses
     * '.' as decimal separator. this function checks for the last decimal separator '.' and remove
     * the rest of all none numeric values
     *
     * @param input US standard amount string
     * @return amount as double
     */
    public static double parseAmountUS(String input) {
        return parseAmountWithDecimalSeparator(input, '.');
    }

    /**
     * Helper function to parse an arbitrary string into an amout using EU standard Eu standard uses
     * ',' as decimal separator. this function checks for the last decimal separator ',' and remove
     * the rest of all none numeric values
     *
     * @param input EU standard amount string
     * @return amount as double
     */
    public static double parseAmountEU(String input) {
        return parseAmountWithDecimalSeparator(input, ',');
    }

    public static double parseAmountWithDecimalSeparator(String input, char separator) {
        if (Strings.isNullOrEmpty(input)) {
            return 0;
        }

        boolean negative = extractNumericWithSign(input).charAt(0) == '-';

        String amount;

        int separatorIndex = input.lastIndexOf(String.valueOf(separator));

        if (separatorIndex < 0) {
            amount = input.replaceAll("[^\\d]", "");
        } else {
            String integerDigit = extractNumeric(input.substring(0, separatorIndex));
            String decimalDigit =
                    input.substring(separatorIndex + 1, input.length()).replaceAll("[^\\d]", "");
            amount = integerDigit.concat(".").concat(decimalDigit);
        }

        try {
            if (negative) {
                return -Double.parseDouble(amount);
            } else return Double.parseDouble(amount);
        } catch (Exception e) {
            log.warn("Cannot parse amount: " + input, e);
            return 0;
        }
    }

    private static String extractNumericWithSign(String input) {
        return input.replaceAll("[^\\d-]", "");
    }

    private static String extractNumeric(String input) {
        return input.replaceAll("[^\\d]", "");
    }
    /**
     * Checks if a string is a number.
     *
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static String trimTrailingDigits(String str) {
        if (str == null || str.length() == 0) return str;

        char c = str.charAt(str.length() - 1);
        while (Character.isDigit(c)) {
            str = str.substring(0, str.length() - 1);

            if (str.length() == 0) break;

            c = str.charAt(str.length() - 1);
        }
        return str;
    }

    public static List<List<String>> readLines(File file) throws IOException {
        return readLines(file, Charsets.UTF_8);
    }

    public static List<List<String>> readLines(File file, Charset charset) throws IOException {
        List<String> descriptions = Files.readLines(file, charset);

        return Lists.newArrayList(
                Iterables.transform(
                        descriptions,
                        s -> {
                            List<String> data = Lists.newArrayList(TAB_SPLITTER.split(s));

                            return data;
                        }));
    }

    /**
     * Parse a string of comma separated values (CSV) into a list of strings.
     *
     * @param s
     * @return
     */
    public static List<String> parseCSV(String s) {
        return Lists.newArrayList(Iterables.transform(COMMA_SPLITTER.split(s), StringUtils::trim));
    }

    /**
     * Parse a string of comma separated key-value pairs (i.e. "foo=A,bar=B") into an key-value
     * entry collection (i.e. [{k:"foo",v:"A"},{k:"bar",v:"B"}])
     *
     * @param s
     * @return
     */
    public static Collection<Entry<String, String>> parseCSKVPairs(String s) {
        Collection<Entry<String, String>> pairs = new ArrayList<Entry<String, String>>();

        for (String pair : parseCSV(s)) {
            String[] parts = pair.split("=");

            if (parts.length != 2) continue;

            pairs.add(new AbstractMap.SimpleEntry<String, String>(parts[0], parts[1]));
        }

        return pairs;
    }

    public static List<String> readCSVLine(String line) {
        return Lists.newArrayList(CSV_SPLITTER.split(line));
    }

    private static boolean shouldFormatHumanDescription(String description) {
        int numberOfUppercaseLetters = 0;
        int numberOfLetters = 0;

        for (char c : description.toCharArray()) {
            if (Character.isLetter(c)) numberOfLetters++;

            if (Character.isUpperCase(c)) numberOfUppercaseLetters++;
        }

        return ((double) numberOfUppercaseLetters / (double) numberOfLetters
                > HUMAN_FORMATTING_UPPERCASE_THRESHOLD);
    }

    /**
     * Helper function to strip extended ASCII chars from a string. That is DEC > 127
     *
     * @param s
     * @return
     */
    public static String stripExtendedAsciiCharacters(String s) {
        return CharMatcher.ascii().retainFrom(s);
    }

    /**
     * Helper function to strip non ASCII chars from a string.
     *
     * @param s
     * @return
     */
    public static String stripNonAsciiCharacters(String s) {
        return Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    public static String trim(String string) {
        return CharMatcher.whitespace().trimFrom(string);
    }

    /**
     * Removes whitespace characters from both ends of this String returning (using {@link
     * #trim(String)}) <code>null</code> if the String is empty ("") after the trim or if it is
     * <code>null</code>.
     *
     * <p>
     *
     * <p>The String is trimmed using {@link String#trim()}. Trim removes start and end characters
     * &lt;= 32. To strip whitespace use {@link #stripToNull(String)}.
     *
     * <p>
     *
     * <pre>
     * StringUtils.trimToNull(null)          = null
     * StringUtils.trimToNull("")            = null
     * StringUtils.trimToNull("     ")       = null
     * StringUtils.trimToNull("abc")         = "abc"
     * StringUtils.trimToNull("    abc    ") = "abc"
     * </pre>
     *
     * @param string the String to be trimmed, may be null
     * @return the trimmed String, <code>null</code> if only chars are whitespace, empty or null
     *     String input
     * @note This description was mostly ripped from {@link
     *     org.apache.commons.lang3.StringUtils#trimToNull(String)}
     */
    public static String trimToNull(String string) {
        if (string == null) return null;
        final String trimmedString = trim(string);
        if (trimmedString.isEmpty()) return null;
        else return trimmedString;
    }

    /**
     * Removes all non-alphanumeric [A-Za-z0-9]
     *
     * @param s The string to operate on
     * @return The input string without non-alphanumerics
     */
    public static String removeNonAlphaNumeric(String s) {
        return s.replaceAll("[^A-Za-z0-9]", "");
    }

    public static String maskSSN(String ssn) {
        return ssn.substring(0, 8) + "****";
    }

    public static String toUtf8FromIso(String passwordConfirmation) {
        return new String(
                passwordConfirmation.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
    }

    /** Helper function to join strings together with a different last separator */
    public static String join(List<String> strings, String separator, String lastSeparator) {
        if (strings == null) {
            return null;
        }

        switch (strings.size()) {
            case 0:
                return "";
            case 1:
                return strings.get(0);
            default:
                return Joiner.on(separator).join(strings.subList(0, strings.size() - 1))
                        + lastSeparator
                        + strings.get(strings.size() - 1);
        }
    }

    /**
     * JaroWinkler returns 0 as comparison score when comparing two empty strings We wan't two empty
     * strings or nulls to be interpreted as exact matches (1)
     */
    public static double getJaroWinklerDistance(CharSequence var0, CharSequence var1) {
        if (var0 == null && var1 == null) {
            return 1;
        }

        if (var0 == null || var1 == null) {
            return 0;
        }

        if (var0.length() == 0 && var1.length() == 0) {
            return 1;
        }

        return org.apache.commons.lang3.StringUtils.getJaroWinklerDistance(var0, var1);
    }
}
