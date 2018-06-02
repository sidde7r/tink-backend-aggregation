/**
 *
 */
package se.tink.backend.common.utils;

import com.google.common.base.CharMatcher;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.elasticsearch.common.primitives.Ints;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import se.tink.backend.common.repository.mysql.main.PostalCodeAreaRepository;

/**
 * Utility class for elastich search operations
 */
public class EsSearchUtils {

    private static final int MIN_WORD_LENGTH_IN_SIMILAR_QUERY = 3;
    protected static Splitter splitter = Splitter.on(CharMatcher.WHITESPACE);

    public static String[] getAllStopWords(PostalCodeAreaRepository postalCodeAreaRepo) {
        List<String> stopWords = Lists.newArrayList();

        try {
            stopWords.addAll(getStopWords());
            stopWords.addAll(getCities(postalCodeAreaRepo));
            stopWords.addAll(getNames());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return stopWords.toArray(new String[stopWords.size()]);
    }

    public static List<String> getStopWords() {

        List<String> stopWordsList = new ArrayList<String>();

        for (String word : new String[] {
                "aktiebolag",
                "ab",
                "restaurang",
                "rest",
                "cafe", "café", "kafe", "kafé", "la",
                "le",
                "www",
                "ref",
                "bg",
                "pg",
                "swish",
                "paypal",
                "izettle",
                "klarna",
                "shop",
                "till",
                "från",
                "svenska",
                "sverige",
                "sveriges",
                "sweden",
                "mottagen",
                "skickad",
                "överföring",
                "present",
                "tack",
                "betalning",
                "2007", "2008", "2009", "2010", "2011", "2012", "2013", "2014", "2015", "2016", "2017", "2018",
                "the",
                "city",
                "internet",
                "grattis",
                "januari", "februari", "mars", "april", "maj", "juni", "juli",
                "augusti", "september", "oktober", "november", "december",
                "jan", "feb", "mar", "apr", "jun", "jul", "aug", "sep", "okt", "nov", "dec",
                "måndag", "tisdag", "onsdag", "torsdag", "fredag", "lördag", "söndag",
                "mån", "tis", "ons", "tor", "fre", "lör", "sön", "cash",
                "mamma",
                "pappa",
                "kommun",
                "puss",
                "kram",
                "sthlm",
                "torget",
                "nation",
                "butik",
                "butiken",
                "autogiro",
                "skyddat belopp",
                "prel. kortlöp",
                "prel kortlöp",
                "direktbetalning",
                "och"
        }) {
            stopWordsList.add(word);
        }

        // Dutch stop words
        for (String word : new String[] { "van", "von", "den", "der" }) {
            stopWordsList.add(word);
        }

        return stopWordsList;
    }

    public static List<String> getCities(PostalCodeAreaRepository postalCodeAreaRepo) {

        List<String> citiesList = new ArrayList<String>();

        List<String> cities = postalCodeAreaRepo.findAllCities();

        for (String city : cities) {
            citiesList.add(city.toLowerCase());
            citiesList.addAll(addModifiedCities(city.toLowerCase(), 4));
        }

        return citiesList;

    }

    public static List<String> addModifiedCities(String city, int minLength) {
        Set<String> modifiedCities = Sets.newHashSet();

        if (city.contains("å") || city.contains("ö")) {
            modifiedCities.add(city.replace("å", "a").replace("ä", "a").replace("ö", "o"));
        }
        if (city.contains("ä") && (city.contains("å") || city.contains("ö"))) {
            modifiedCities.add(city.replace("å", "a").replace("ä", "e").replace("ö", "o"));
        } else if (city.contains("ä")) {
            modifiedCities.add(city.replace("å", "a").replace("ä", "a").replace("ö", "o"));
            modifiedCities.add(city.replace("å", "a").replace("ä", "e").replace("ö", "o"));
        }

        List<String> additions = Lists.newArrayList();

        for (String c : modifiedCities) {
            if (c.length() > minLength) {
                for (int i = minLength; i < c.length(); i++) {
                    additions.add(c.substring(0, i));
                }
            }
            if (!c.endsWith("s")) {
                additions.add(c + "s");
            }
        }

        if (!city.endsWith("s")) {
            additions.add(city + "s");
        }

        modifiedCities.addAll(additions);

        if (city.length() > minLength) {
            for (int i = minLength; i < city.length(); i++) {
                modifiedCities.add(city.substring(0, i));
            }
        }

        return Lists.newArrayList(modifiedCities);
    }

    public static List<String> getNames() throws IOException {
        List<String> lines = Files.readLines(new File("data/seeding/names.txt"), Charsets.UTF_8);

        List<String> names = Lists.newArrayList();

        for (String line : lines) {
            String[] data = line.split("\t");
            names.add(data[0].toLowerCase());
        }

        return names;
    }

    public static QueryBuilder getSimilarQuery(String description, String[] stopWords, float percentMatch) {

        String escapedDescription = CommonStringUtils.escapeElasticSearchSearchString(description);

        // determine length of longest word
        int minWordLength = 0;
        Iterable<String> words = splitter.split(escapedDescription);
        for (String word : words) {
            if (minWordLength < word.length()) {
                minWordLength = word.length();
            }
        }
        minWordLength = Math.min(minWordLength, MIN_WORD_LENGTH_IN_SIMILAR_QUERY);
        String noNumbersDescription = stripWordsOnlyContainNumbers(words);

        return QueryBuilders
                .moreLikeThisFieldQuery("description")
                .likeText(noNumbersDescription)
                .minTermFreq(0)
                .minDocFreq(0)
                .minWordLen(minWordLength)
                .percentTermsToMatch(percentMatch)
                .stopWords(stopWords);
    }

    private static String stripWordsOnlyContainNumbers(Iterable<String> words) {
        // strip words that only contains numbers
        StringBuilder noNumbersDescription = new StringBuilder();
        for (String word : words) {
            Integer numberWord = Ints.tryParse(word);
            if (numberWord == null) {
                noNumbersDescription.append(word).append(' ');
            }
        }

        return noNumbersDescription.toString().trim();
    }
}
