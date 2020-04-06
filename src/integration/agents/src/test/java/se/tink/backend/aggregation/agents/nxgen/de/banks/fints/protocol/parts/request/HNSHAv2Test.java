package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.Test;

public class HNSHAv2Test {

    @Test
    public void shouldSerializeProperlyWithAllFieldsFilled() {
        // given
        BaseRequestPart segment =
                HNSHAv2.builder()
                        .securityReference(2819991)
                        .password("PASSWORD_1247")
                        .tanAnswer("TANANSWER_1246178")
                        .build();

        // when
        String serializedSegment = segment.toFinTsFormat();

        // then
        assertThat(serializedSegment)
                .isEqualTo("HNSHA:1:2+2819991++PASSWORD_1247:TANANSWER_1246178");
    }

    @Test
    public void shouldSerializeProperlyWithoutTanAnswer() {
        // given
        BaseRequestPart segment =
                HNSHAv2.builder().securityReference(9823165).password("PASSWORD_88922").build();

        // when
        String serializedSegment = segment.toFinTsFormat();

        // then
        assertThat(serializedSegment).isEqualTo("HNSHA:1:2+9823165++PASSWORD_88922");
    }

    @Test
    public void shouldThrowNullPointerWhenBuiltWithoutRequiredFields() {
        // given
        HNSHAv2.HNSHAv2Builder builder =
                HNSHAv2.builder().password("PASSWORD_2346").tanAnswer("TANANSWER_343438");

        // when
        Throwable throwable = catchThrowable(builder::build);

        // then
        assertThat(throwable).isInstanceOf(NullPointerException.class);
    }
}
