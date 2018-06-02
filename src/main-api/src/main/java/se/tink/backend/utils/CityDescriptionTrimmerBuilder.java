package se.tink.backend.utils;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.googlecode.concurrenttrees.radix.ConcurrentRadixTree;
import com.googlecode.concurrenttrees.radix.RadixTree;
import com.googlecode.concurrenttrees.radix.node.concrete.DefaultCharArrayNodeFactory;
import com.googlecode.concurrenttrees.radix.node.concrete.voidvalue.VoidValue;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.serialization.TypeReferences;

public class CityDescriptionTrimmerBuilder {

    private static final LogUtils log = new LogUtils(CityDescriptionTrimmer.class);
    private static final Splitter SEMI_COLON_SPLITTER = Splitter.on(';').trimResults().omitEmptyStrings();

    // Downloaded from https://datahub.io/dataset/iso-3166-1-alpha-2-country-codes/
    private static final String ISO_COUNTRIES_PATH = "data/seeding/iso_countries.csv";

    // Downloaded from https://github.com/David-Haim/CountriesToCitiesJSON
    private static final String CITIES_BY_COUNTRY_PATH = "data/seeding/cities-global.json";
    private static final String CITIES_NETHERLANDS_PATH = "data/seeding/cities-nl.txt";
    private static final String CITIES_SWEDEN_PATH = "data/seeding/cities-se.txt";

    public CityDescriptionTrimmer build() {

        Stopwatch stopwatch = Stopwatch.createStarted();

        try {

            // Load countries and create pattern tree with iso codes
            RadixTree countryPatternTree = createCountryPatternTree(getCountriesWithIsoCode());

            // Load cities and create pattern tree with world cities
            RadixTree cityPatternTree = createCityPatternTree(getCities());

            return new CityDescriptionTrimmer(countryPatternTree, cityPatternTree);
        } catch (IOException e) {
            log.error("Could not create country city description trimmer", e);
        } finally {
            log.info(String.format("City trimmer initiated in %s ms", stopwatch.elapsed(TimeUnit.MILLISECONDS)));
        }

        return null;
    }

    private RadixTree createCountryPatternTree(List<CountryIsoCode> countries) {
        ConcurrentRadixTree tree = new ConcurrentRadixTree(new DefaultCharArrayNodeFactory());

        for (CountryIsoCode country : countries) {

            // Important that all values are added lower case since the pattern tree is case sensitive.
            tree.putIfAbsent(country.getAlpha2Code().toLowerCase(), VoidValue.SINGLETON);
            tree.putIfAbsent(country.getAlpha3Code().toLowerCase(), VoidValue.SINGLETON);
        }

        log.info(String.format("Created pattern tree (Countries = '%d')", countries.size()));

        return tree;
    }

    private RadixTree createCityPatternTree(Set<String> cities) {
        ConcurrentRadixTree tree = new ConcurrentRadixTree(new DefaultCharArrayNodeFactory());

        for (String city : cities) {

            // Important that all values are added lower case since the pattern tree is case sensitive
            tree.putIfAbsent(city.toLowerCase(), VoidValue.SINGLETON);
        }

        log.info(String.format("Created pattern tree (Cities = '%d')", cities.size()));

        return tree;
    }

    private Set<String> getCities() throws IOException {

        String contents = FileUtils.readFileToString(new File(CITIES_BY_COUNTRY_PATH), Charsets.UTF_8);

        HashMap<String, List<String>> citiesByCountryName = SerializationUtils.deserializeFromString(contents,
                TypeReferences.MAP_OF_STRING_LIST_STRING);

        Set<String> cities = Sets.newHashSet();

        for (List<String> citiesByCountry : citiesByCountryName.values()) {
            cities.addAll(citiesByCountry);
        }

        // Load special files for Sweden and Netherlands not to change any old logic.
        cities.addAll(Files.readLines(new File(CITIES_SWEDEN_PATH), Charsets.UTF_8));
        cities.addAll(Files.readLines(new File(CITIES_NETHERLANDS_PATH), Charsets.UTF_8));

        return FluentIterable.from(cities).filter(city -> !Strings.isNullOrEmpty(city)).toSet();
    }

    private List<CountryIsoCode> getCountriesWithIsoCode() throws IOException {

        List<String> rows = Files.readLines(new File(ISO_COUNTRIES_PATH), Charsets.UTF_8);

        List<CountryIsoCode> result = Lists.newArrayListWithExpectedSize(rows.size());

        for (int i = 0; i < rows.size(); i++) {
            String row = rows.get(i);

            List<String> columns = SEMI_COLON_SPLITTER.splitToList(row);

            if (columns.size() != 3) {
                log.warn(String.format("Unexpected number of columns (Row = %d)", i));
                continue;
            }

            result.add(new CountryIsoCode(columns.get(1), columns.get(2)));
        }

        return result;
    }

    private class CountryIsoCode {
        private final String alpha2Code;
        private final String alpha3Code;

        public CountryIsoCode(String alpha2Code, String alpha3Code) {
            Preconditions.checkArgument(alpha2Code.length() == 2);
            Preconditions.checkArgument(alpha3Code.length() == 3);

            this.alpha2Code = alpha2Code;
            this.alpha3Code = alpha3Code;
        }

        public String getAlpha2Code() {
            return alpha2Code;
        }

        public String getAlpha3Code() {
            return alpha3Code;
        }
    }

}
