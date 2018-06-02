package se.tink.backend.utils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import se.tink.backend.core.Transaction;

public class TagsUtils {

    private static final char HASHBANG = '#';
    private static final String REMOVE_TAGS_REGEX_FORMAT = " ?#(?i)(%s)";
    private static final Splitter SPLITTER = Splitter.on(CharMatcher.WHITESPACE).trimResults().omitEmptyStrings();
    private static final Pattern TRAILING_TAGS = Pattern.compile("(\\s*#[a-z0-9_]+)+\\s*$", Pattern.CASE_INSENSITIVE);

    /**
     * Extract all unique tags from a transaction
     */
    public static List<String> extractUniqueTags(Transaction transaction) {
        return extractUniqueTags(transaction.getNotes());
    }

    /**
     * Extract all unique tags from a string
     */
    public static List<String> extractUniqueTags(String message) {
        if (Strings.isNullOrEmpty(message)) {
            return Collections.emptyList();
        }

        return StreamSupport.stream(SPLITTER.split(message).spliterator(), false)
                .filter(TagsUtils::isTag)
                .map(tag -> tag.substring(1))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Extract all unique tags from a list of transactions
     */
    public static List<String> extractUniqueTags(List<Transaction> transactions) {
        return transactions.stream()
                .map(TagsUtils::extractUniqueTags)
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());
    }

    public static String buildNote(String note, List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return note;
        }

        return addTags(note, tags);
    }

    private static String addTags(String note, List<String> tags) {
        List<String> tagsWithHashtags = tags.stream()
                .map(TagsUtils::addHashbang)
                .filter(tag -> !note.contains(tag))
                .collect(Collectors.toList());
        return String.format("%s %s", note, Joiner.on(" ").join(tagsWithHashtags)).trim();
    }

    @VisibleForTesting
    static String removeTags(String note, Set<String> tags) {
        if (tags.isEmpty()) {
            return note;
        }
        return note.replaceAll(String.format(REMOVE_TAGS_REGEX_FORMAT, Joiner.on("|").join(tags)), "");
    }

    private static Set<String> subtractIgnoreCase(Collection<String> from, Collection<String> subtraction) {
        Set<String> set1 = Sets.newTreeSet(String.CASE_INSENSITIVE_ORDER);
        set1.addAll(from);
        Set<String> set2 = Sets.newTreeSet(String.CASE_INSENSITIVE_ORDER);
        set2.addAll(subtraction);
        set1.removeAll(set2);
        return set1;
    }

    public static boolean hasTags(Transaction transaction) {
        if (Strings.isNullOrEmpty(transaction.getNotes())) {
            return false;
        }

        return StreamSupport.stream(SPLITTER.split(transaction.getNotes()).spliterator(), false)
                .anyMatch(TagsUtils::isTag);
    }

    public static boolean isTag(String input) {
        return input.length() > 1 && input.charAt(0) == HASHBANG;
    }

    public static String addHashbang(String input) {
        return HASHBANG + input;
    }

    public static String removeTrailingTags(String text) {
        if (Strings.isNullOrEmpty(text)) {
            return text;
        }

        return TRAILING_TAGS.matcher(text).replaceAll("");
    }
}
