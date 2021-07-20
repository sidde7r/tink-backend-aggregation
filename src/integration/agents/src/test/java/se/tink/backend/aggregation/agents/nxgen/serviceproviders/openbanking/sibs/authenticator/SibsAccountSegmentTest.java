package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class SibsAccountSegmentTest {

    @Test
    public void shouldReturnPersonalSegment() {
        // when
        SibsAccountSegment segment = SibsAccountSegment.getSegment("1");

        // then
        assertThat(segment).isEqualTo(SibsAccountSegment.PERSONAL);
    }

    @Test
    public void shouldReturnBusinessSegment() {
        // when
        SibsAccountSegment segment = SibsAccountSegment.getSegment("2");

        // then
        assertThat(segment).isEqualTo(SibsAccountSegment.BUSINESS);
    }
}
