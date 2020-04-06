package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.detail.RawSegmentComposer;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;

public class HISYNTest {

    @Test
    public void shouldParseSegmentCorrectly() {
        // given
        String[][] arr =
                new String[][] {
                    new String[] {"HISYN", "175", "4", "6"},
                    new String[] {"SYSTEMID_12375983295"}
                };
        RawSegment rawSegment = RawSegmentComposer.compose(arr);

        // when
        HISYN segment = new HISYN(rawSegment);

        // then
        assertThat(segment.getSegmentName()).isEqualTo("HISYN");
        assertThat(segment.getSegmentVersion()).isEqualTo(4);
        assertThat(segment.getSegmentPosition()).isEqualTo(175);
        assertThat(segment.getReferencedSegmentPosition()).isEqualTo(6);
        assertThat(segment.getSystemId()).isEqualTo("SYSTEMID_12375983295");
    }
}
