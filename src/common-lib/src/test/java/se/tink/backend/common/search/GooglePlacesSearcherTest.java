package se.tink.backend.common.search;

import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;

import org.junit.Test;
import se.tink.backend.common.merchants.GooglePlacesSearcher;
import se.tink.backend.core.Place;
import se.tink.backend.core.StringStringPair;

/**
 * TODO: rewrite to not interact without external services
 */
@Ignore
public class GooglePlacesSearcherTest {

    private static GooglePlacesSearcher placesSearcher;
    private static final String DEFAULT_LOCALE = "sv_SE";
    private static final String DEFAULT_COUNTRY = "SE";

    @BeforeClass
    public static void initialize() {
        placesSearcher = new GooglePlacesSearcher();
    }

    @Test
    public void testQuerySearch() throws Exception {
        String query = "Coop Konsum Zinken";
        List<Place> results = placesSearcher.textSearch(query, DEFAULT_LOCALE, DEFAULT_COUNTRY);

        Assert.assertTrue(results.size() > 0);
    }

    @Test
    public void testNearBySearch() throws Exception {
        double stockholmLatitude = 59.329444;
        double stockholmLongitude = 18.068611;
        List<Place> results = placesSearcher.nearbySearch("Döbelns Pasta", stockholmLatitude, stockholmLongitude,
                DEFAULT_LOCALE);
        Assert.assertTrue(results.size() > 0);
    }

    @Test
    public void testAutocompleteDetails() throws Exception {
        List<Place> results = placesSearcher.detailedAutocomplete("Döbelns pa", 10, DEFAULT_LOCALE, DEFAULT_COUNTRY);
        Assert.assertTrue(results.size() > 0);
    }

    @Test
    public void testAutocompleteDetailsWithNonAsciiCharacters() throws Exception {
        List<Place> results = placesSearcher.detailedAutocomplete("Kjell & Company", 10, DEFAULT_LOCALE, DEFAULT_COUNTRY);
        Assert.assertTrue(results.size() > 0);
    }

    @Test
    public void testDetailedAutocompleteEstablishment() throws Exception {
        List<Place> results = placesSearcher.detailedAutocompleteEstablishment("Döbelns pa", 10, DEFAULT_LOCALE,
                DEFAULT_COUNTRY);
        Assert.assertTrue(results.size() > 0);

        for (Place place : results) {
            Assert.assertTrue(place.getTypes().contains("establishment"));
        }
    }

    @Test
    public void testAutocompleteEstablishment() throws Exception {
        List<StringStringPair> results = placesSearcher.autocompleteEstablishment("Döbelns pa", 10, DEFAULT_LOCALE,
                DEFAULT_COUNTRY);
        Assert.assertTrue(results.size() > 0);
    }

    @Test
    public void testQuerySearchNetherlands() throws Exception {
        String query = "La Rive";
        List<Place> results = placesSearcher.textSearch(query, "en_US", "NL");

        Assert.assertTrue(results.size() > 0);

        Place laRive = results.get(0);

        Assert.assertEquals("Larive International", laRive.getName());
        Assert.assertTrue(laRive.getTypes().contains("establishment"));
        Assert.assertTrue(laRive.getTypes().size() == 1);
    }

    @Test
    public void testAutocompleteAddress() throws Exception {
        List<StringStringPair> addresses = placesSearcher.autocompleteAddress("Lundagatan 36", 10, DEFAULT_LOCALE,
                DEFAULT_COUNTRY);

        boolean foundMatch = false;

        for (StringStringPair pair : addresses) {
            if (pair.getValue().contains("Lundagatan 36F")) {
                foundMatch = true;
                Place place = placesSearcher.details(pair.getKey(), DEFAULT_LOCALE);
                Assert.assertTrue(place.getTypes().contains("street_address"));
                Assert.assertTrue(place.getAddress().contains("117 27"));
            }
        }

        Assert.assertTrue(foundMatch);
    }
}
