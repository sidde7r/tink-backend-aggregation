package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.detail.RawSegmentComposer;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;

public class TanContextTest {
    @Test
    public void shouldParseSegmentCorrectly() {
        // given
        String[][] arr =
                new String[][] {
                    new String[] {"HITAN", "5", "6", "4"},
                    new String[] {"4"},
                    new String[] {"TASKHASH_1288"},
                    new String[] {"4859-03-18-12.37.05.400190"},
                    new String[] {"Bitte geben Sie die pushTAN ein."},
                    new String[] {"HDUCCHALLENGE_35789"},
                    new String[] {"VALIDUNTIL_209387"},
                    new String[] {"MEDIUMNAME_1234"}
                };
        RawSegment rawSegment = RawSegmentComposer.compose(arr);

        // when
        TanContext segment = new TanContext(rawSegment);

        // then
        assertThat(segment.getSegmentName()).isEqualTo("HITAN");
        assertThat(segment.getSegmentVersion()).isEqualTo(6);
        assertThat(segment.getSegmentPosition()).isEqualTo(5);
        assertThat(segment.getReferencedSegmentPosition()).isEqualTo(4);

        assertThat(segment.getTanProcess()).isEqualTo("4");
        assertThat(segment.getTaskHashValue()).isEqualTo("TASKHASH_1288");
        assertThat(segment.getTaskReference()).isEqualTo("4859-03-18-12.37.05.400190");
        assertThat(segment.getChallenge()).isEqualTo("Bitte geben Sie die pushTAN ein.");
        assertThat(segment.getChallengeHhduc()).isEqualTo("HDUCCHALLENGE_35789");
        assertThat(segment.getChallengeValidUntil()).isEqualTo("VALIDUNTIL_209387");
        assertThat(segment.getTanMediumName()).isEqualTo("MEDIUMNAME_1234");
    }

    @Test
    public void shouldParseSegmentCorrectlySimpleUseCase() {
        // given
        String[][] arr =
                new String[][] {
                    new String[] {"HITAN", "5", "6", "4"},
                    new String[] {"4"},
                    new String[] {""},
                    new String[] {"noref"},
                    new String[] {"nochallenge"}
                };
        RawSegment rawSegment = RawSegmentComposer.compose(arr);

        // when
        TanContext segment = new TanContext(rawSegment);

        // then
        assertThat(segment.getSegmentName()).isEqualTo("HITAN");
        assertThat(segment.getSegmentVersion()).isEqualTo(6);
        assertThat(segment.getSegmentPosition()).isEqualTo(5);
        assertThat(segment.getReferencedSegmentPosition()).isEqualTo(4);

        assertThat(segment.getTanProcess()).isEqualTo("4");
        assertThat(segment.getTaskHashValue()).isNull();
        assertThat(segment.getTaskReference()).isEqualTo("noref");
        assertThat(segment.getChallenge()).isEqualTo("nochallenge");
        assertThat(segment.getChallengeHhduc()).isNull();
        assertThat(segment.getChallengeValidUntil()).isNull();
        assertThat(segment.getTanMediumName()).isNull();
    }
}
