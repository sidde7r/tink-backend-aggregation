package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.detail.RawSegmentComposer;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;

public class HISPATest {

    @Test
    public void shouldParseSegmentCorrectly() {
        // given
        String[][] arr =
                new String[][] {
                    new String[] {"HISPA", "4", "1", "3"},
                    new String[] {
                        "J", "DE13100177788997", "PBNKDEFF", "123545754", "", "280", "10010010"
                    },
                    new String[] {
                        "N", "DE28370177788997", "PBNKDEFF", "765876875", "1234", "280", "37011000"
                    }
                };
        RawSegment rawSegment = RawSegmentComposer.compose(arr);

        // when
        HISPA segment = new HISPA(rawSegment);

        // then
        assertThat(segment.getSegmentName()).isEqualTo("HISPA");
        assertThat(segment.getSegmentVersion()).isEqualTo(1);
        assertThat(segment.getSegmentPosition()).isEqualTo(4);
        assertThat(segment.getReferencedSegmentPosition()).isEqualTo(3);

        assertThat(segment.getAccountDetails()).hasSize(2);

        HISPA.Detail details = segment.getAccountDetails().get(0);
        assertThat(details.getIsSepaAccount()).isTrue();
        assertThat(details.getIban()).isEqualTo("DE13100177788997");
        assertThat(details.getBic()).isEqualTo("PBNKDEFF");
        assertThat(details.getAccountNumber()).isEqualTo("123545754");
        assertThat(details.getSubAccountNumber()).isNull();
        assertThat(details.getCountryCode()).isEqualTo("280");
        assertThat(details.getBlz()).isEqualTo("10010010");

        details = segment.getAccountDetails().get(1);
        assertThat(details.getIsSepaAccount()).isFalse();
        assertThat(details.getIban()).isEqualTo("DE28370177788997");
        assertThat(details.getBic()).isEqualTo("PBNKDEFF");
        assertThat(details.getAccountNumber()).isEqualTo("765876875");
        assertThat(details.getSubAccountNumber()).isEqualTo("1234");
        assertThat(details.getCountryCode()).isEqualTo("280");
        assertThat(details.getBlz()).isEqualTo("37011000");
    }
}
