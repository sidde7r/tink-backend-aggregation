package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.detail.RawSegmentComposer;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;

public class TransactionsCamtTest {
    @Test
    public void shouldParseSegmentCorrectly() {
        // given
        String[][] arr =
                new String[][] {
                    new String[] {"HICAZ", "6", "1", "3"},
                    new String[] {"IBAN_468673867", "BIC_398275832"},
                    new String[] {"urn?:iso?:std?:iso?:20022?:tech?:xsd?:camt.052.001.02'"},
                    new String[] {
                        "BookedTransactions001",
                        "BookedTransactions002",
                        "BookedTransactions003",
                        "BookedTransactions004"
                    },
                    new String[] {"NotBookedTransactions"}
                };
        RawSegment rawSegment = RawSegmentComposer.compose(arr);

        // when
        TransactionsCamt segment = new TransactionsCamt(rawSegment);

        // then
        assertThat(segment.getSegmentName()).isEqualTo("HICAZ");
        assertThat(segment.getSegmentVersion()).isEqualTo(1);
        assertThat(segment.getSegmentPosition()).isEqualTo(6);
        assertThat(segment.getReferencedSegmentPosition()).isEqualTo(3);
        assertThat(segment.getCamtFormat())
                .isEqualTo("urn?:iso?:std?:iso?:20022?:tech?:xsd?:camt.052.001.02'");
        assertThat(segment.getCamtFiles())
                .containsExactly(
                        "BookedTransactions001",
                        "BookedTransactions002",
                        "BookedTransactions003",
                        "BookedTransactions004",
                        "NotBookedTransactions");
    }
}
