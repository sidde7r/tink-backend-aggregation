package se.tink.backend.consent.core.cassandra;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class CassandraConsentTest {
    @Test
    public void testCreateWithBuilder() {
        CassandraConsent cassandraConsent = CassandraConsent.builder()
                .withBody("body")
                .withKey("key")
                .withLocale("nl_NL")
                .withTitle("title")
                .withMessage(new MessageEntity("foo"))
                .withVersion("1.0.0")
                .build();

        assertThat(cassandraConsent.getBody()).isEqualTo("body");
        assertThat(cassandraConsent.getKey()).isEqualTo("key");
        assertThat(cassandraConsent.getLocale()).isEqualTo("nl_NL");
        assertThat(cassandraConsent.getMessages().size()).isEqualTo(1);
        assertThat(cassandraConsent.getVersion()).isEqualTo("1.0.0");
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidVersion() {
        CassandraConsent.builder()
                .withBody("body")
                .withKey("key")
                .withLocale("nl_NL")
                .withTitle("title")
                .withMessage(new MessageEntity("foo"))
                .withVersion("abc") // this is not a valid semantic version
                .build();
    }

    @Test
    public void testChecksumWithDifferentBody() {
        CassandraConsent cassandraConsent1 = CassandraConsent.builder()
                .withBody("body-1") // different body
                .withKey("key")
                .withLocale("nl_NL")
                .withTitle("title")
                .withMessage(new MessageEntity("foo"))
                .withVersion("1.0.0")
                .build();

        CassandraConsent cassandraConsent2 = CassandraConsent.builder()
                .withBody("body-2") // different body
                .withKey("key")
                .withLocale("nl_NL")
                .withTitle("title")
                .withMessage(new MessageEntity("foo"))
                .withVersion("1.0.0")
                .build();

        assertThat(cassandraConsent1.getChecksum()).isNotNull();
        assertThat(cassandraConsent2.getChecksum()).isNotNull();

        // Consents have different bodies so the checksum should not be equal.
        assertThat(cassandraConsent1.getChecksum()).isNotEqualTo(cassandraConsent2.getChecksum());
    }

    @Test
    public void testUpdateFor() {
        assertThat(createConsent("k", "1.0.1", "nl_NL").isUpdateFor(createConsent("k", "2.0.0", "nl_NL"))).isFalse();
        assertThat(createConsent("k", "1.0.1", "nl_NL").isUpdateFor(createConsent("k", "1.0.0", "nl_NL"))).isTrue();
        assertThat(createConsent("k", "1.0.1", "en_US").isUpdateFor(createConsent("k", "1.0.0", "nl_NL"))).isFalse();
        assertThat(createConsent("k1", "1.0.1", "en_US").isUpdateFor(createConsent("k2", "1.0.0", "nl_NL"))).isFalse();
    }

    private static CassandraConsent createConsent(String key, String version, String locale) {
        return CassandraConsent.builder()
                .withKey(key)
                .withVersion(version)
                .withTitle("Title")
                .withBody("Body")
                .withLocale(locale)
                .build();
    }
}
