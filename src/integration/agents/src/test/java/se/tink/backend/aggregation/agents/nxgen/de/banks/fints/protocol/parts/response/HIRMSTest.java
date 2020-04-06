package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.detail.RawSegmentComposer;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;

public class HIRMSTest {
    String a =
            "HIRMS:5:2:4+3050::BPD nicht mehr aktuell, aktuelle Version enthalten.+3920::Zugelassene Zwei-Schritt-Verfahren für den Benutzer.:921+0020::Der Auftrag wurde ausgeführt.'";

    @Test
    public void shouldParseSegmentCorrectly() {
        // given
        String[][] arr =
                new String[][] {
                    new String[] {"HIRMS", "5", "2", "4"},
                    new String[] {
                        "3050", "1111", "BPD nicht mehr aktuell, aktuelle Version enthalten."
                    },
                    new String[] {
                        "3920", "", "Zugelassene Zwei-Schritt-Verfahren für den Benutzer.", "921"
                    },
                    new String[] {
                        "0020", "", "Der Auftrag wurde ausgeführt.", "9912", "999", "910", "123"
                    }
                };
        RawSegment rawSegment = RawSegmentComposer.compose(arr);

        // when
        HIRMS segment = new HIRMS(rawSegment);

        // then
        assertThat(segment.getSegmentName()).isEqualTo("HIRMS");
        assertThat(segment.getSegmentVersion()).isEqualTo(2);
        assertThat(segment.getSegmentPosition()).isEqualTo(5);
        assertThat(segment.getReferencedSegmentPosition()).isEqualTo(4);

        assertThat(segment.getResponses()).hasSize(3);
        HIRMS.Response response = segment.getResponses().get(0);
        assertThat(response.getResultCode()).isEqualTo("3050");
        assertThat(response.getReferenceElement()).isEqualTo("1111");
        assertThat(response.getText())
                .isEqualTo("BPD nicht mehr aktuell, aktuelle Version enthalten.");
        assertThat(response.getParameters()).isEmpty();

        response = segment.getResponses().get(1);
        assertThat(response.getResultCode()).isEqualTo("3920");
        assertThat(response.getReferenceElement()).isNull();
        assertThat(response.getText())
                .isEqualTo("Zugelassene Zwei-Schritt-Verfahren für den Benutzer.");
        assertThat(response.getParameters()).containsExactly("921");

        response = segment.getResponses().get(2);
        assertThat(response.getResultCode()).isEqualTo("0020");
        assertThat(response.getReferenceElement()).isNull();
        assertThat(response.getText()).isEqualTo("Der Auftrag wurde ausgeführt.");
        assertThat(response.getParameters()).containsExactly("9912", "999", "910", "123");
    }
}
