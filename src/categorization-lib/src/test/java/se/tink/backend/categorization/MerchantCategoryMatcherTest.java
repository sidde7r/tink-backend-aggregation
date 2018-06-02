package se.tink.backend.categorization;

import java.io.IOException;
import org.junit.Test;
import se.tink.libraries.cluster.Cluster;
import static org.assertj.core.api.Assertions.assertThat;

public class MerchantCategoryMatcherTest {

    @Test
    public void testFindByDescriptionNoMatch() throws IOException {
        MerchantCategoryMatcher matcher = MerchantCategoryMatcher.builder(Cluster.ABNAMRO).build();

        assertThat(matcher.findByDescription("Erik Pettersson Makeup Store")).isNull();
    }

    @Test
    public void testFindByCodeNoMatch() throws IOException {
        MerchantCategoryMatcher matcher = MerchantCategoryMatcher.builder(Cluster.ABNAMRO).build();

        assertThat(matcher.findByCode(999999999)).isNull();
    }

    @Test
    public void testFindByDescription() throws IOException {
        MerchantCategoryMatcher matcher = MerchantCategoryMatcher.builder(Cluster.ABNAMRO).build();

        assertThat(matcher.findByDescription("EDUCATIONAL SERVICES")).isNotNull();
        assertThat(matcher.findByDescription("CAR RENTAL AGENCIES")).isNotNull();
        assertThat(matcher.findByDescription("EATING PLACES")).isNotNull();
    }

    @Test
    public void testFindByCode() throws IOException {
        MerchantCategoryMatcher matcher = MerchantCategoryMatcher.builder(Cluster.ABNAMRO).build();

        assertThat(matcher.findByCode(3000)).isNotNull();
        assertThat(matcher.findByCode(3001)).isNotNull();
    }

}
