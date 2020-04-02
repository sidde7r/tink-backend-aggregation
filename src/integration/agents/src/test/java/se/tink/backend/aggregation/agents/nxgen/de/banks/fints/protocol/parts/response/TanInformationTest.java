package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.detail.RawSegmentComposer;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;

public class TanInformationTest {

    @Test
    public void shouldParseSegmentCorrectly() {
        // given
        String[][] arr =
                new String[][] {
                    new String[] {"HIPINS", "174", "1", "4"},
                    new String[] {"10"},
                    new String[] {"20"},
                    new String[] {"30"},
                    new String[] {
                        "5", "38", "6", "USERID", "CUSTID", "HKAUB", "J", "HKBME", "J", "HKBSE",
                        "J", "HKCAZ", "J", "HKCCM", "J", "HKCCS", "J", "HKCDB", "N"
                    }
                };
        RawSegment rawSegment = RawSegmentComposer.compose(arr);

        // when
        TanInformation segment = new TanInformation(rawSegment);

        // then
        assertThat(segment.getSegmentName()).isEqualTo("HIPINS");
        assertThat(segment.getSegmentVersion()).isEqualTo(1);
        assertThat(segment.getSegmentPosition()).isEqualTo(174);
        assertThat(segment.getReferencedSegmentPosition()).isEqualTo(4);

        assertThat(segment.getMaxNumberOfTasks()).isEqualTo(10);
        assertThat(segment.getMinNumberSignatures()).isEqualTo(20);
        assertThat(segment.getSecurityClass()).isEqualTo(30);

        assertThat(segment.getMinPinLength()).isEqualTo(5);
        assertThat(segment.getMaxPinLength()).isEqualTo(38);
        assertThat(segment.getMaxTanLength()).isEqualTo(6);
        assertThat(segment.getUserIdFieldText()).isEqualTo("USERID");
        assertThat(segment.getCustomerIdFieldText()).isEqualTo("CUSTID");

        assertThat(segment.getOperations())
                .containsExactly(
                        Pair.of("HKAUB", true),
                        Pair.of("HKBME", true),
                        Pair.of("HKBSE", true),
                        Pair.of("HKCAZ", true),
                        Pair.of("HKCCM", true),
                        Pair.of("HKCCS", true),
                        Pair.of("HKCDB", false));
    }
}
