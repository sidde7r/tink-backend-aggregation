package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.detail.RawSegmentComposer;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HITAB.TanMedia;

public class HITABTest {
    @Test
    public void shouldParseSegmentProperly() {
        // given
        String[][] arr =
                new String[][] {
                    new String[] {"HITAB", "5", "4", "3"},
                    new String[] {"0"},
                    new String[] {
                        "A",
                        "1",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "Google Phone",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        ""
                    },
                    new String[] {
                        "A",
                        "1",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "Redmi Note 7 by Xiaomi",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        ""
                    }
                };
        RawSegment segment = RawSegmentComposer.compose(arr);

        // when
        HITAB hitab = new HITAB(segment);

        // then
        assertThat(hitab.getTanUsageOption()).isEqualTo(0);
        assertThat(hitab.getSegmentName()).isEqualTo("HITAB");
        assertThat(hitab.getSegmentVersion()).isEqualTo(4);
        assertThat(hitab.getSegmentPosition()).isEqualTo(5);
        assertThat(hitab.getTanMediaList()).containsOnlyElementsOf(getExpectedTanMediaList());
    }

    private List<TanMedia> getExpectedTanMediaList() {
        return Arrays.asList(
                TanMedia.builder()
                        .tanMediumClass("A")
                        .tanMediumStatus(1)
                        .tanMediumName("Google Phone")
                        .build(),
                TanMedia.builder()
                        .tanMediumClass("A")
                        .tanMediumStatus(1)
                        .tanMediumName("Redmi Note 7 by Xiaomi")
                        .build());
    }
}
