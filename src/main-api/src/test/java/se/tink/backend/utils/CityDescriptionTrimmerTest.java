package se.tink.backend.utils;

import java.util.concurrent.TimeUnit;
import org.junit.Ignore;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class CityDescriptionTrimmerTest {

    /**
     * Test to trim when the description is ending with a city
     */
    @Test
    public void testDescriptionCityTrimming() throws Exception {

        CityDescriptionTrimmer trimmer = CityDescriptionTrimmer.builder().build();

        assertThat(trimmer.trim("H&M Amsterdam")).isEqualTo("H&M");
        assertThat(trimmer.trim("H&M Rotterdam")).isEqualTo("H&M");
        assertThat(trimmer.trim("H&M ZOETERMEER")).isEqualTo("H&M");
        assertThat(trimmer.trim("H&M Paris")).isEqualTo("H&M");
        assertThat(trimmer.trim("H&M Lyon")).isEqualTo("H&M");
        assertThat(trimmer.trim("H&M Stockholm")).isEqualTo("H&M");
    }

    /**
     * Test that we trim a city even if there only is the city in the description
     * <p>
     * We would most likely don't want to have empty descriptions but that logic should be somewhere else and not in
     * a trimmer.
     */
    @Test
    public void testDescriptionWithOnlyCityInNameTrimming() throws Exception {

        CityDescriptionTrimmer trimmer = CityDescriptionTrimmer.builder().build();

        assertThat(trimmer.trim("Stockholm")).isEqualTo("");
        assertThat(trimmer.trim("Paris")).isEqualTo("");
    }

    /**
     * Test to trim with only country information
     */
    @Test
    public void testDescriptionWithOnlyCountryTrimming() {

        CityDescriptionTrimmer trimmer = CityDescriptionTrimmer.builder().build();

        assertThat(trimmer.trim("SWE")).isEqualTo("");
        assertThat(trimmer.trim("GBR")).isEqualTo("");
    }

    /**
     * Test was to trim when the description is ending with a city and country
     */
    @Test
    public void testDescriptionCountryAndCityTrimming() throws Exception {

        CityDescriptionTrimmer trimmer = CityDescriptionTrimmer.builder().build();

        assertThat(trimmer.trim("A b c d e H&M Lyon FR")).isEqualTo("A b c d e H&M");
        assertThat(trimmer.trim("H&M Amsterdam NLD")).isEqualTo("H&M");

        // This will be trimmed even if Rotterdam isn't in Sweden
        assertThat(trimmer.trim("H&M Rotterdam SE")).isEqualTo("H&M");
    }

    @Test
    public void testNoTrimming() {
        CityDescriptionTrimmer trimmer = CityDescriptionTrimmer.builder().build();

        assertThat(trimmer.trim("Amsterdam Hotel")).isEqualTo("Amsterdam Hotel");
        assertThat(trimmer.trim("Paris Hotel")).isEqualTo("Paris Hotel");

        assertThat(trimmer.trimWithFuzzyFallback("Amsterdam Hotel")).isEqualTo("Amsterdam Hotel");
        assertThat(trimmer.trimWithFuzzyFallback("Paris Hotel")).isEqualTo("Paris Hotel");
    }

    @Test
    public void testFuzzyMatching() {
        CityDescriptionTrimmer trimmer = CityDescriptionTrimmer.builder().build();

        // City is Frankfurt
        assertThat(trimmer.trimWithFuzzyFallback("WWW ZALANDO NL FRANKFUR DEU")).isEqualTo("WWW ZALANDO NL");

        // City is Zoetermeer
        assertThat(trimmer.trimWithFuzzyFallback("JACK&JONES ZOETERME ZOETERMEER NLD")).isEqualTo("JACK&JONES");

        // City is Alphen aan den Rijn
        assertThat(trimmer.trimWithFuzzyFallback("JACK&JONES Alphen aan")).isEqualTo("JACK&JONES");
    }

    /**
     * Test to trim descriptions that has multiple cities in the description
     */
    @Test
    public void testDescriptionsWithMultipleCities() {

        CityDescriptionTrimmer trimmer = CityDescriptionTrimmer.builder().build();

        assertThat(trimmer.trim("H&M Paris Paris")).isEqualTo("H&M");
        assertThat(trimmer.trim("H&M Paris Paris Paris")).isEqualTo("H&M");
        assertThat(trimmer.trim("H&M Stockholm Stockholm Stockholm")).isEqualTo("H&M");
    }

    @Test
    @Ignore
    public void performanceTests() throws Exception {

        CityDescriptionTrimmer trimmer = CityDescriptionTrimmer.builder().build();

        long startTime = System.currentTimeMillis();
        long maxTestTime = TimeUnit.SECONDS.toMillis(2);

        int iterations = 0;

        while ((System.currentTimeMillis() - startTime) < maxTestTime) {

            // Do different calls to get some "randomness"
            trimmer.trim("H&M Amsterdam NLD");
            trimmer.trim("H&M Paris");
            trimmer.trim("AB C H&M Lyon FR");
            trimmer.trim("AB C H&M Lyon FRA");

            trimmer.trimWithFuzzyFallback("H&M Amster");
            trimmer.trimWithFuzzyFallback("H&M Pari");
            trimmer.trimWithFuzzyFallback("AB C H&M Marsei");
            trimmer.trimWithFuzzyFallback("AB C H&M Marsei");

            iterations += 8; // 8 unique calls
        }

        double average = maxTestTime / (double) iterations;

        System.out.println(String.format("Calls: %,d Average: %,f ms", iterations, average));
        System.out.println(String.format("1000 transactions will take %,f ms", average * 1000));
    }
}
