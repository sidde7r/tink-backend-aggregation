package se.tink.backend.core;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.backend.utils.CategorizationUtils;
import se.tink.backend.utils.LogUtils;

public class Cities implements Serializable {

    private final static LogUtils log = new LogUtils(Cities.class);

    private List<String> cities = Lists.newArrayList();
    private List<Pattern> trimmingPatterns = Lists.newArrayList();
    private List<Pattern> fuzzyTrimmingPatterns = Lists.newArrayList();

    private void compileTrimmingPatterns() {

        Builder<Pattern> listBuilder = ImmutableList.builder();

        // Create patterns of all the cities, to match the end of the string (i.e. tail trimming).
        // NB! Some descriptions have a country suffix to the city, so add that as an optional sub pattern.
        for (String city : cities) {
            listBuilder.add(Pattern.compile(String.format(" (%s)( se)?$", city), Pattern.CASE_INSENSITIVE));
        }

        trimmingPatterns = listBuilder.build();
    }

    private void compileFuzzyTrimmingPatterns() {

        Builder<Pattern> listBuilder = ImmutableList.builder();

        for (String city : cities) {
            String pattern = compileFuzzyPattern(city);
            if (!Strings.isNullOrEmpty(pattern)) {
                listBuilder.add(Pattern.compile(pattern, Pattern.CASE_INSENSITIVE));
            }
        }

        fuzzyTrimmingPatterns = listBuilder.build();
    }

    private static final int PREFIX_LENGTH = 3;

    /**
     * The fuzzy patterns matches the beginning of city names (at the end of the string) with the minimum length of
     * `PREFIX_LENGTH`. E.g. "Abcd 123 Stock" would match Stockholm, and trim "Stock" from the description.
     * @param city
     * @return
     */
    private static String compileFuzzyPattern(String city) {

        if (Strings.isNullOrEmpty(city)) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        sb.append(' ');

        if (city.length() > PREFIX_LENGTH) {
            sb.append(city.substring(0, PREFIX_LENGTH));

            for (int i = PREFIX_LENGTH; i < city.length(); i++) {
                sb.append('(');
                sb.append(city.charAt(i));
            }

            for (int i = PREFIX_LENGTH; i < city.length(); i++) {
                sb.append(")?");
            }
        } else {
            sb.append(city);
        }

        sb.append('$');

        return sb.toString();
    }

    public void loadCities(String filename) throws IOException {

        log.debug("Loading cities");

            List<String> tmp = Files.readLines(new File(filename), Charsets.UTF_8, new LineProcessor<List<String>>() {
                List<String> result = Lists.newArrayList();

                public boolean processLine(String line) {
                    // Make sure the cities are cleaned the same way as the transaction descriptions.
                    result.add(CategorizationUtils.clean(line));
                    return true;
                }

                public List<String> getResult() {
                    return result;
                }
            });

            // Sort the cities to start off with the longest patterns.
            Collections.sort(tmp, (s1, s2) -> new Integer(s1.length()).compareTo(new Integer(s2.length())));

            Collections.reverse(tmp);

        cities = ImmutableList.copyOf(tmp);

            log.debug("Cities loaded successfully");
    }

    public int size() {
        return cities.size();
    }

    public String trim(String string) {

        if (trimmingPatterns.size() == 0) {
            compileTrimmingPatterns();
        }

        for (Pattern cityPattern : trimmingPatterns) {
            Matcher m = cityPattern.matcher(string);
            if (m.find()) {
                return m.replaceFirst("").trim();
            }
        }

        return string;
    }

    public String trimFuzzy(String string) {

        if (fuzzyTrimmingPatterns.size() == 0) {
            compileFuzzyTrimmingPatterns();
        }

        for (Pattern cityPattern : fuzzyTrimmingPatterns) {
            Matcher m = cityPattern.matcher(string);
            if (m.find()) {
                return m.replaceFirst("").trim();
            }
        }

        return string;
    }
}
