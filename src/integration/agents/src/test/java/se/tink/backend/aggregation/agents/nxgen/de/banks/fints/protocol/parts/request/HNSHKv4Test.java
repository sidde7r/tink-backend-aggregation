package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.LocalDateTime;
import org.junit.Test;

public class HNSHKv4Test {

    @Test
    public void shouldSerializeProperlyWithAllFieldsFilled() {
        // given
        BaseRequestPart segment =
                HNSHKv4.builder()
                        .securityProcedureVersion(2421)
                        .securityFunction("SECFUN_657612")
                        .securityReference(72848)
                        .systemId("SYSTEMID_671247")
                        .creationTime(LocalDateTime.of(1992, 10, 23, 15, 47, 21))
                        .blz("BLZ_9402")
                        .username("USERNAME_127969")
                        .build();

        // when
        String serializedSegment = segment.toFinTsFormat();

        // then
        assertThat(serializedSegment)
                .isEqualTo(
                        "HNSHK:1:4+PIN:2421+SECFUN_657612+72848+1+1+1::SYSTEMID_671247+1+1:19921023:154721+1:999:1+6:10:16+280:BLZ_9402:USERNAME_127969:S:0:0");
    }

    @Test
    public void shouldThrowNullPointerWhenBuiltWithoutRequiredFields() {
        // given
        HNSHKv4.HNSHKv4Builder builder = HNSHKv4.builder();

        // when
        Throwable throwable = catchThrowable(builder::build);

        // then
        assertThat(throwable).isInstanceOf(NullPointerException.class);
    }
}
