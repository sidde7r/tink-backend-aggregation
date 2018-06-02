package se.tink.backend.consent.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.Test;
import se.tink.backend.consent.core.cassandra.CassandraConsent;
import static org.assertj.core.api.Assertions.assertThat;

public class ConsentFiltersTest {
    @Test
    public void filterEmptyConsents() {
        assertThat(ConsentFilters.filterLatestConsents(Lists.newArrayList())).isEmpty();
    }

    @Test
    public void filterSingleConsent() {
        CassandraConsent c1 = createConsent("key", "1.0.0", "nl_NL");

        assertThat(ConsentFilters.filterLatestConsents(ImmutableList.of(c1))).containsOnly(c1);
    }

    @Test
    public void filterMultipleConsentsDifferentVersions() {
        CassandraConsent c1 = createConsent("key", "1.0.0", "nl_NL");
        CassandraConsent c2 = createConsent("key", "1.0.1", "nl_NL");

        assertThat(ConsentFilters.filterLatestConsents(ImmutableList.of(c1, c2))).containsOnly(c2);
    }

    @Test
    public void filterMultipleConsentsDifferentVersionsAndLocale() {
        CassandraConsent c1 = createConsent("key", "1.0.0", "nl_NL");
        CassandraConsent c2 = createConsent("key", "1.0.1", "nl_NL");
        CassandraConsent c3 = createConsent("key", "1.0.1", "en_US");

        assertThat(ConsentFilters.filterLatestConsents(ImmutableList.of(c1, c2, c3))).containsOnly(c2, c3);
    }

    @Test
    public void filterMultipleConsentsDifferentVersionsAndLocaleAndKey() {
        CassandraConsent c1 = createConsent("key", "1.0.0", "nl_NL");
        CassandraConsent c2 = createConsent("key", "1.0.1", "nl_NL");
        CassandraConsent c3 = createConsent("key", "1.0.1", "en_US");
        CassandraConsent c4 = createConsent("key-2", "1.0.0", "en_US");

        assertThat(ConsentFilters.filterLatestConsents(ImmutableList.of(c1, c2, c3, c4))).containsOnly(c2, c3, c4);
    }

    private CassandraConsent createConsent(String key, String version, String locale) {
        return CassandraConsent.builder()
                .withKey(key)
                .withVersion(version)
                .withTitle("Title")
                .withBody("Body")
                .withLocale(locale)
                .build();
    }
}
