package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.SegmentType;

public class HKTANv6Test {

    @Test
    public void shouldSerializeProperlyWithAllFieldsFilled() {
        // given
        BaseRequestPart segment =
                HKTANv6.builder()
                        .tanProcess("4")
                        .segmentType(SegmentType.HKKAZ)
                        .taskHashValue("TASKHASHVALUE_123421513")
                        .taskReference("TASKREFERENCE_124372588")
                        .furtherTanFollows(false)
                        .tanMediumName("TANMEDIUMNAME_1234122_:?'")
                        .build();

        // when
        String serializedSegment = segment.toFinTsFormat();

        // then
        assertThat(serializedSegment)
                .isEqualTo(
                        "HKTAN:1:6+4+HKKAZ++TASKHASHVALUE_123421513+TASKREFERENCE_124372588+N+++++TANMEDIUMNAME_1234122_?:???'");
    }

    @Test
    public void shouldSerializeProperlyWithSegmentNameAndMediumName() {
        // given
        BaseRequestPart segment =
                HKTANv6.builder()
                        .tanProcess("4")
                        .segmentType(SegmentType.HKTAB)
                        .tanMediumName("DUMMY")
                        .build();

        // when
        String serializedSegment = segment.toFinTsFormat();

        // then
        assertThat(serializedSegment).isEqualTo("HKTAN:1:6+4+HKTAB++++N+++++DUMMY");
    }
}
