package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.LocalDateTime;
import org.junit.Test;

public class HNVSKv3Test {
    @Test
    public void shouldSerializeProperlyWithAllFieldsFilled() {
        // given
        BaseRequestPart segment =
                HNVSKv3.builder()
                        .securityProcedureVersion(363456)
                        .systemId("SYSTEMID_88199")
                        .creationTime(LocalDateTime.of(2001, 5, 18, 7, 22, 1))
                        .blz("BLZ_9402")
                        .username("USERNAME_127969")
                        .build();

        // when
        String serializedSegment = segment.toFinTsFormat();

        // then
        assertThat(serializedSegment)
                .isEqualTo(
                        "HNVSK:1:3+PIN:363456+998+1+1::SYSTEMID_88199+1:20010518:072201+2:2:13:@8@00000000:5:1+280:BLZ_9402:USERNAME_127969:S:0:0+0");
    }

    @Test
    public void shouldThrowNullPointerWhenBuiltWithoutRequiredFields() {
        // given
        HNVSKv3.HNVSKv3Builder builder = HNVSKv3.builder();

        // when
        Throwable throwable = catchThrowable(builder::build);

        // then
        assertThat(throwable).isInstanceOf(NullPointerException.class);
    }
}
