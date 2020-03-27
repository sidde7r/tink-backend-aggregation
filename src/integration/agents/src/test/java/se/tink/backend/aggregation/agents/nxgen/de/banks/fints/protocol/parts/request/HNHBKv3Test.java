package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.Test;

public class HNHBKv3Test {

    @Test
    public void shouldSerializeProperlyWithAllFieldsFilled() {
        // given
        BaseRequestPart segment =
                HNHBKv3.builder().dialogId("DIALOGID_5616724").messageNumber(1772).build();

        // when
        String serializedSegment = segment.toFinTsFormat();

        // then
        assertThat(serializedSegment).isEqualTo("HNHBK:1:3+000000000000+300+DIALOGID_5616724+1772");
    }

    @Test
    public void shouldThrowNullPointerWhenBuiltWithoutRequiredFields() {
        // given
        HNHBKv3.HNHBKv3Builder builder = HNHBKv3.builder();

        // when
        Throwable throwable = catchThrowable(builder::build);

        // then
        assertThat(throwable).isInstanceOf(NullPointerException.class);
    }
}
