package se.tink.backend.connector.utils;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TagValidationUtils {

    private static final char HASHBANG = '#';
    private static final Pattern VALID_TAG_PATTERN = Pattern.compile("^[ÅÄÖåäöæÆøØA-Za-z0-9_\\-.]+$");
    private static final Splitter SPLITTER = Splitter.on(CharMatcher.WHITESPACE).trimResults().omitEmptyStrings();

    public static Optional<List<String>> asTagList(Object rawList) {
        if (!(rawList instanceof List)) {
            return Optional.empty();
        }

        List list = (List) rawList;
        ArrayList<String> tags = new ArrayList<>(list.size());

        for (Object tag : list) {
            if (tag instanceof String) {
                tags.add((String) tag);
            } else {
                return Optional.empty();
            }
        }

        return Optional.of(tags);
    }

    public static Optional<String> normalizeTag(String tag) {
        if (tag == null) {
            return Optional.empty();
        }

        tag = tag.trim();
        if (tag.charAt(0) == HASHBANG) {
            tag = tag.substring(1);
        }
        boolean valid = VALID_TAG_PATTERN.matcher(tag).matches();
        return valid ? Optional.of(tag) : Optional.empty();
    }

    public static List<String> normalizeTagList(List<String> tags) {
        return tags.stream()
                .map(TagValidationUtils::normalizeTag)
                .filter(Optional::isPresent)
                .distinct()
                .map(Optional::get)
                .map(t -> "#" + t)
                .collect(Collectors.toList());
    }
}
