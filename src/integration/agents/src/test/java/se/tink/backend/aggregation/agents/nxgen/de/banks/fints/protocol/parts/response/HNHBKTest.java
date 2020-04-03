package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.detail.RawSegmentComposer;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;

public class HNHBKTest {

    @Test
    public void shouldParseSegmentCorrectly() {
        // given
        String[][] arr =
                new String[][] {
                    new String[] {"HNHBK", "1", "3"},
                    new String[] {"000000010880"},
                    new String[] {"300"},
                    new String[] {"DIALOGID_120479281359"},
                    new String[] {"1"},
                    new String[] {"DIALOGID_120479281359", "1"}
                };
        RawSegment rawSegment = RawSegmentComposer.compose(arr);

        // when
        HNHBK segment = new HNHBK(rawSegment);

        // then
        assertThat(segment.getSegmentName()).isEqualTo("HNHBK");
        assertThat(segment.getSegmentVersion()).isEqualTo(3);
        assertThat(segment.getSegmentPosition()).isEqualTo(1);
        assertThat(segment.getDialogId()).isEqualTo("DIALOGID_120479281359");
    }
}
