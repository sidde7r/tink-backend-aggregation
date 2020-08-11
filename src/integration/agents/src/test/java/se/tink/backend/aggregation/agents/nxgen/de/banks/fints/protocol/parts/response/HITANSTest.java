package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.detail.RawSegmentComposer;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;

public class HITANSTest {

    @Test
    public void shouldParseAuthMethodsV6() {
        // given
        String[][] arr = HITANSTestResponse.genHITANSResponseV6();
        RawSegment rawSegment = RawSegmentComposer.compose(arr);

        // when
        HITANS segment = new HITANS(rawSegment);

        // then
        assertThat(segment).isNotNull();
        assertThat(segment.getAllowedScaMethods())
                .hasSize(6)
                .containsEntry("930", "mobileTAN")
                .containsEntry("920", "BestSign")
                .containsEntry("910", "chipTAN optisch HHD1.3.2")
                .containsEntry("911", "chipTAN manuell HHD1.3.2")
                .containsEntry("912", "chipTAN optisch HHD1.4")
                .containsEntry("913", "chipTAN manuell HHD1.4");
    }

    @Test
    public void shouldParseAuthMethodsV1() {
        // given
        String[][] arr = HITANSTestResponse.genHITANSResponseV1();
        RawSegment rawSegment = RawSegmentComposer.compose(arr);

        // when
        HITANS segment = new HITANS(rawSegment);

        // then
        assertThat(segment).isNotNull();
        assertThat(segment.getAllowedScaMethods())
                .hasSize(2)
                .containsEntry("900", "iTAN")
                .containsEntry("930", "mobileTAN");
    }

    @Test
    public void shouldParseAuthMethodsV2() {
        // given
        String[][] arr = HITANSTestResponse.genHITANSResponseV2();
        RawSegment rawSegment = RawSegmentComposer.compose(arr);

        // when
        HITANS segment = new HITANS(rawSegment);

        // then
        assertThat(segment).isNotNull();
        assertThat(segment.getAllowedScaMethods())
                .hasSize(2)
                .containsEntry("900", "iTAN")
                .containsEntry("930", "mobileTAN");
    }

    @Test
    public void shouldParseAuthMethodsV3() {
        // given
        String[][] arr = HITANSTestResponse.genHITANSResponseV3();
        RawSegment rawSegment = RawSegmentComposer.compose(arr);

        // when
        HITANS segment = new HITANS(rawSegment);

        // then
        assertThat(segment).isNotNull();
        assertThat(segment.getAllowedScaMethods())
                .hasSize(2)
                .containsEntry("900", "iTAN")
                .containsEntry("930", "mobileTAN");
    }

    @Test
    public void shouldParseAuthMethodsV4() {
        // given
        String[][] arr = HITANSTestResponse.genHITANSResponseV4();
        RawSegment rawSegment = RawSegmentComposer.compose(arr);

        // when
        HITANS segment = new HITANS(rawSegment);

        // then
        assertThat(segment).isNotNull();
        assertThat(segment.getAllowedScaMethods())
                .hasSize(2)
                .containsEntry("900", "iTAN")
                .containsEntry("930", "mobileTAN");
    }

    @Test
    public void shouldParseAuthMethodsV5() {
        // given
        String[][] arr = HITANSTestResponse.genHITANSResponseV5();
        RawSegment rawSegment = RawSegmentComposer.compose(arr);

        // when
        HITANS segment = new HITANS(rawSegment);

        // then
        assertThat(segment).isNotNull();
        assertThat(segment.getAllowedScaMethods())
                .hasSize(2)
                .containsEntry("900", "iTAN")
                .containsEntry("930", "mobileTAN");
    }
}
