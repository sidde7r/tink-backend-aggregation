package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.Test;

public class HKENDv1Test {

    @Test
    public void shouldSerializeProperlyWithAllFieldsFilled() {
        // given
        BaseRequestPart segment = HKENDv1.builder().dialogId("ASDFZXCV").build();

        // when
        String serializedSegment = segment.toFinTsFormat();

        // then
        assertThat(serializedSegment).isEqualTo("HKEND:1:1+ASDFZXCV");
    }

    @Test
    public void shouldThrowNullPointerWhenBuiltWithoutRequiredFields() {
        // given
        HKENDv1.HKENDv1Builder builder = HKENDv1.builder();

        // when
        Throwable throwable = catchThrowable(builder::build);

        // then
        assertThat(throwable).isInstanceOf(NullPointerException.class);
    }
}
