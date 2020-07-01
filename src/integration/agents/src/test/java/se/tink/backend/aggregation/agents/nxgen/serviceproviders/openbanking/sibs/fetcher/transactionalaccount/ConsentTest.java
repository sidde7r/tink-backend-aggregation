package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.entities.Consent;

public class ConsentTest {

    private static final String DUMMY_CONSENT_ID = "dummyConsentId";

    @Test
    public void shouldReturnTrueWhenConsentAreNotOlderThan30Minutes() {
        Consent consent =
                new Consent(DUMMY_CONSENT_ID, LocalDateTime.now().minusMinutes(30).toString());

        assertThat(consent.isConsentOlderThan30Minutes()).isTrue();
    }

    @Test
    public void shouldReturnFalseWhenConsentAreNotOlderThan30Minutes() {
        Consent consent = new Consent(DUMMY_CONSENT_ID, LocalDateTime.now().toString());

        assertThat(consent.isConsentOlderThan30Minutes()).isFalse();
    }

    @Test
    public void shouldReturnFalseWhenConsentAreAlmostOlderThan30MinutesBut() {
        Consent consent =
                new Consent(DUMMY_CONSENT_ID, LocalDateTime.now().minusMinutes(29).toString());

        assertThat(consent.isConsentOlderThan30Minutes()).isFalse();
    }
}
