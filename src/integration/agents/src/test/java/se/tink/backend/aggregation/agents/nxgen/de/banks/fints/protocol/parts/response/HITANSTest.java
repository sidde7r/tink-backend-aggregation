package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.detail.RawSegmentComposer;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;

public class HITANSTest {

    @Test
    public void shouldParseAuthMethods() {
        // given
        String[][] arr = {
            {"HITANS", "14", "6", "4"},
            {"1"},
            {"1"},
            {"0"},
            {
                "N",
                "N",
                "0",
                "910",
                "2",
                "HHD1.3.2OPT",
                "HHDOPT1",
                "1.3.2",
                "chipTAN optisch HHD1.3.2",
                "6",
                "1",
                "Challenge",
                "999",
                "N",
                "1",
                "N",
                "0",
                "2",
                "N",
                "J",
                "00",
                "2",
                "N",
                "9",
                "911",
                "2",
                "HHD1.3.2",
                "HHD",
                "1.3.2",
                "chipTAN manuell HHD1.3.2",
                "6",
                "1",
                "Challenge",
                "999",
                "N",
                "1",
                "N",
                "0",
                "2",
                "N",
                "J",
                "00",
                "2",
                "N",
                "9",
                "912",
                "2",
                "HHD1.4OPT",
                "HHDOPT1",
                "1.4",
                "chipTAN optisch HHD1.4",
                "6",
                "1",
                "Challenge",
                "999",
                "N",
                "1",
                "N",
                "0",
                "2",
                "N",
                "J",
                "00",
                "2",
                "N",
                "9",
                "913",
                "2",
                "HHD1.4",
                "HHD",
                "1.4",
                "chipTAN manuell HHD1.4",
                "6",
                "1",
                "Challenge",
                "999",
                "N",
                "1",
                "N",
                "0",
                "2",
                "N",
                "J",
                "00",
                "2",
                "N",
                "9",
                "920",
                "2",
                "BestSign",
                "BestSign",
                "",
                "BestSign",
                "6",
                "2",
                "BestSign",
                "999",
                "N",
                "1",
                "N",
                "0",
                "2",
                "N",
                "J",
                "00",
                "2",
                "N",
                "9",
                "930",
                "2",
                "mobileTAN",
                "mobileTAN",
                "",
                "mobileTAN",
                "6",
                "2",
                "mobileTAN",
                "999",
                "N",
                "1",
                "N",
                "0",
                "2",
                "N",
                "J",
                "00",
                "2",
                "N",
                "9"
            }
        };
        RawSegment rawSegment = RawSegmentComposer.compose(arr);

        // when
        HITANS segment = new HITANS(rawSegment);

        // then
        assertThat(segment).isNotNull();
        assertThat(segment.getSupportedVersions()).hasSize(1).contains(6);
        assertThat(segment.getAllowedScaMethods())
                .hasSize(6)
                .containsEntry("930", "mobileTAN")
                .containsEntry("920", "BestSign")
                .containsEntry("910", "chipTAN optisch HHD1.3.2")
                .containsEntry("911", "chipTAN manuell HHD1.3.2")
                .containsEntry("912", "chipTAN optisch HHD1.4")
                .containsEntry("913", "chipTAN manuell HHD1.4");
    }
}
