package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.Test;

public class HNHBSv1Test {

    @Test
    public void shouldSerializeProperlyWithAllFieldsFilled() {
        // given
        BaseRequestPart segment = HNHBSv1.builder().messageNumber(64278).build();

        // when
        String serializedSegment = segment.toFinTsFormat();

        // then
        assertThat(serializedSegment).isEqualTo("HNHBS:1:1+64278");
    }

    @Test
    public void shouldThrowNullPointerWhenBuiltWithoutRequiredFields() {
        // given
        HNHBSv1.HNHBSv1Builder builder = HNHBSv1.builder();

        // when
        Throwable throwable = catchThrowable(builder::build);

        // then
        assertThat(throwable).isInstanceOf(NullPointerException.class);
    }
}
