package se.tink.backend.consent.core;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class UserConsentTest {
    @Test
    public void testCompatibility() {
        UserConsent consent = new UserConsent();

        consent.setKey("TERMS_AND_CONDITIONS");
        consent.setVersion("2.0.0");

        assertThat(consent.isCompatibleWith("ANOTHER_KEY", "1.0.0")).isFalse();
        assertThat(consent.isCompatibleWith("TERMS_AND_CONDITIONS", "0.9.0")).isTrue();
        assertThat(consent.isCompatibleWith("TERMS_AND_CONDITIONS", "1.0.0")).isTrue();
        assertThat(consent.isCompatibleWith("TERMS_AND_CONDITIONS", "1.9.0")).isTrue();
        assertThat(consent.isCompatibleWith("TERMS_AND_CONDITIONS", "2.0.0")).isTrue();
        assertThat(consent.isCompatibleWith("TERMS_AND_CONDITIONS", "2.0.1")).isTrue();
        assertThat(consent.isCompatibleWith("TERMS_AND_CONDITIONS", "3.0.0")).isFalse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSemanticVersion() {
        UserConsent consent = new UserConsent();

        consent.setKey("TERMS_AND_CONDITIONS");
        consent.setVersion("not-valid");

        consent.isCompatibleWith("TERMS_AND_CONDITIONS", "1.0.0");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidInputSemanticVersion() {
        UserConsent consent = new UserConsent();

        consent.setKey("TERMS_AND_CONDITIONS");
        consent.setVersion("1.0.0");

        consent.isCompatibleWith("TERMS_AND_CONDITIONS", "not-valid");
    }
}
