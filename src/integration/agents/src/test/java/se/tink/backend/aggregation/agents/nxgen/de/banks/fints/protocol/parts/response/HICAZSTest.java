package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.detail.RawSegmentComposer;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;

public class HICAZSTest {
    @Test
    public void shouldParseSegmentCorrectly() {
        // given
        String[][] arr =
                new String[][] {
                    new String[] {"HICAZS", "95", "1", "31"},
                    new String[] {"10"},
                    new String[] {"20"},
                    new String[] {"30"},
                    new String[] {
                        "450",
                        "N",
                        "J",
                        "urn?:iso?:std?:iso?:20022?:tech?:xsd?:camt.052.001.02",
                        "CAMTFORMAT_ANOTHER"
                    }
                };
        RawSegment rawSegment = RawSegmentComposer.compose(arr);

        // when
        HICAZS segment = new HICAZS(rawSegment);

        // then
        assertThat(segment.getSegmentName()).isEqualTo("HICAZS");
        assertThat(segment.getSegmentVersion()).isEqualTo(1);
        assertThat(segment.getSegmentPosition()).isEqualTo(95);
        assertThat(segment.getReferencedSegmentPosition()).isEqualTo(31);
        assertThat(segment.getMaxNumberOfTasks()).isEqualTo(10);
        assertThat(segment.getMinNumberSignatures()).isEqualTo(20);
        assertThat(segment.getSecurityClass()).isEqualTo(30);
        assertThat(segment.getStoragePeriod()).isEqualTo(450);
        assertThat(segment.getCanLimitNumberOfEntries()).isFalse();
        assertThat(segment.getCanQueryAboutAllAcounts()).isTrue();
        assertThat(segment.getSupportedCamtFormats())
                .containsExactly(
                        "urn?:iso?:std?:iso?:20022?:tech?:xsd?:camt.052.001.02",
                        "CAMTFORMAT_ANOTHER");
    }
}
