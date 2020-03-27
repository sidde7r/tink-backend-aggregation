package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.detail.RawSegmentComposer;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;

public class HIKAZTest {

    @Test
    public void shouldParseSegmentCorrectly() {
        // given
        String[][] arr =
                new String[][] {
                    new String[] {"HIKAZ", "6", "5", "4"},
                    new String[] {"TransactionsInMT940FormatBooked"},
                    new String[] {"TransactionsInMT940FormatNotBooked"}
                };
        RawSegment rawSegment = RawSegmentComposer.compose(arr);

        // when
        HIKAZ segment = new HIKAZ(rawSegment);

        // then
        assertThat(segment.getSegmentName()).isEqualTo("HIKAZ");
        assertThat(segment.getSegmentVersion()).isEqualTo(5);
        assertThat(segment.getSegmentPosition()).isEqualTo(6);
        assertThat(segment.getReferencedSegmentPosition()).isEqualTo(4);
        assertThat(segment.getBooked()).isEqualTo("TransactionsInMT940FormatBooked");
        assertThat(segment.getNotBooked()).isEqualTo("TransactionsInMT940FormatNotBooked");
    }

    @Test
    public void shouldParseSegmentCorrectlyWithoutNotBookedTransactions() {
        // given
        String[][] arr =
                new String[][] {
                    new String[] {"HIKAZ", "6", "5", "4"},
                    new String[] {"TransactionsInMT940FormatBooked"}
                };
        RawSegment rawSegment = RawSegmentComposer.compose(arr);

        // when
        HIKAZ segment = new HIKAZ(rawSegment);

        // then
        assertThat(segment.getSegmentName()).isEqualTo("HIKAZ");
        assertThat(segment.getSegmentVersion()).isEqualTo(5);
        assertThat(segment.getSegmentPosition()).isEqualTo(6);
        assertThat(segment.getReferencedSegmentPosition()).isEqualTo(4);
        assertThat(segment.getBooked()).isEqualTo("TransactionsInMT940FormatBooked");
        assertThat(segment.getNotBooked()).isNull();
    }
}
