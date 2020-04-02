package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.detail.RawSegmentComposer;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;

public class EncryptedEnvelopeTest {

    @Test
    public void shouldParseSegmentCorrectly() {
        // given
        String[][] arr =
                new String[][] {
                    new String[] {"HNVSD", "1", "1"},
                    new String[] {"HNHBS:5:1+1'HNHBS:5:1+1'HNHBS:5:1+1'HNHBS:5:1+1'HNHBS:5:1+1'"}
                };
        RawSegment rawSegment = RawSegmentComposer.compose(arr);

        // when
        EncryptedEnvelope segment = new EncryptedEnvelope(rawSegment);

        // then
        assertThat(segment.getSegmentName()).isEqualTo("HNVSD");
        assertThat(segment.getSegmentVersion()).isEqualTo(1);
        assertThat(segment.getSegmentPosition()).isEqualTo(1);
        assertThat(segment.getRawSegments()).hasSize(5);
        RawSegment innerSegment =
                RawSegmentComposer.compose(
                        new String[][] {new String[] {"HNHBS", "5", "1"}, new String[] {"1"}});
        for (int i = 0; i < 5; i++) {
            assertThat(segment.getRawSegments().get(i)).isEqualTo(innerSegment);
        }
    }
}
