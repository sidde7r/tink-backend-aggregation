package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount;

import java.time.LocalDateTime;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class ConsentTest {

    @Test
    public void shouldRecognizeConsentAsYoungerThan30Minutes() {
        Consent consent = new Consent("dummyId", LocalDateTime.now());

        Assertions.assertThat(consent.isConsentYoungerThan30Minutes()).isTrue();
    }

    @Test
    public void shouldRecognizeConsentAsOlderThan30Minutes() {
        Consent consent = new Consent("dummyId", LocalDateTime.now().minusMinutes(30L));

        Assertions.assertThat(consent.isConsentYoungerThan30Minutes()).isFalse();
    }
}
