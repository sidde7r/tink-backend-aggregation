package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.exception.FinTsParseException;

public class FinTsParserTest {

    @Test
    public void shouldReturnOneSegmentWithOneGroupWithOneEmptyStringElement() {
        // given
        String rawMessage = "'";

        // when
        List<RawSegment> segments = FinTsParser.parse(rawMessage);

        // then
        assertThat(segments).hasSize(1);
        assertThat(segments.get(0).getGroup(0).getString(0)).isEqualTo(null);
    }

    @Test
    public void shouldUnescapeSpecialCharacters() {
        // given
        String rawMessage = "asdf?:zxcv???'???:?@zxcv'";

        // when
        List<RawSegment> segments = FinTsParser.parse(rawMessage);

        // then
        assertThat(segments).hasSize(1);
        assertThat(segments.get(0).getGroup(0).asList()).containsExactly("asdf:zxcv?'?:@zxcv");
    }

    @Test
    public void shouldNotUnescapeSpecialCharactersInBinaryContent() {
        // given
        String rawMessage = "@24@asdf?:zxcv???'???:?@zxcv'";

        // when
        List<RawSegment> segments = FinTsParser.parse(rawMessage);

        // then
        assertThat(segments).hasSize(1);
        assertThat(segments.get(0).getGroup(0).asList())
                .containsExactly("asdf?:zxcv???'???:?@zxcv");
    }

    @Test
    public void shouldNotParseBinaryContentIntoSegments() {
        // given
        String rawMessage = "ASDF:1:2:@12@ZXCV:1:2+200'";

        // when
        List<RawSegment> segments = FinTsParser.parse(rawMessage);

        // then
        assertThat(segments).hasSize(1);
        assertThat(segments.get(0).getGroup(0).asList())
                .containsExactly("ASDF", "1", "2", "ZXCV:1:2+200");
    }

    @Test
    public void shouldParseSmallArtificialMessageCorrectly() {
        // given
        String rawMessage = "RA??Z:DWA+TRZY'CZTERY+@4@PIEC'";

        // when
        List<RawSegment> segments = FinTsParser.parse(rawMessage);

        // then
        assertThat(segments).hasSize(2);
        RawSegment s = segments.get(0);
        assertThat(s.getGroup(0).asList()).containsExactly("RA?Z", "DWA");
        assertThat(s.getGroup(1).asList()).containsExactly("TRZY");

        s = segments.get(1);
        assertThat(s.getGroup(0).asList()).containsExactly("CZTERY");
        assertThat(s.getGroup(1).asList()).containsExactly("PIEC");
    }

    @Test
    public void shouldParseThisBigExampleCorrectly() {
        // given
        String rawMessage =
                "HNHBK:1:3+000000000431+300+438842995982=307388524622BLA9=+5'HNVSK:998:3+PIN:2+998+1+1::06NebYu?+qnABAAANpNOVwVkXrAQA+1:20200305:150541+2:2:13:@8@00000000:5:1+280:75050000:1234567890:v:0:0+0'HNVSD:999:1+@211@HNSHK:2:4+PIN:2+921+9302018+1+1+1::06NebYu?+qnABAAANpNOVwVkXrAQA+1+1:20200305:150541+1:999:1+6:10:16+280:75050000:1234567890:S:0:0'HKTAN:3:6+2+++8809-03-05-16.04.44.308950+N+++++'HNSHA:4:2+9302018++12345:041254''HNHBS:5:1+5'";

        // when
        List<RawSegment> segments = FinTsParser.parse(rawMessage);

        // then
        assertThat(segments).hasSize(4);
        RawSegment s = segments.get(0);
        assertThat(s.getGroup(0).asList()).containsExactly("HNHBK", "1", "3");
        assertThat(s.getGroup(1).asList()).containsExactly("000000000431");
        assertThat(s.getGroup(2).asList()).containsExactly("300");
        assertThat(s.getGroup(3).asList()).containsExactly("438842995982=307388524622BLA9=");
        assertThat(s.getGroup(4).asList()).containsExactly("5");

        s = segments.get(1);
        assertThat(s.getGroup(0).asList()).containsExactly("HNVSK", "998", "3");
        assertThat(s.getGroup(1).asList()).containsExactly("PIN", "2");
        assertThat(s.getGroup(2).asList()).containsExactly("998");
        assertThat(s.getGroup(3).asList()).containsExactly("1");
        assertThat(s.getGroup(4).asList()).containsExactly("1", "", "06NebYu+qnABAAANpNOVwVkXrAQA");
        assertThat(s.getGroup(5).asList()).containsExactly("1", "20200305", "150541");
        assertThat(s.getGroup(6).asList()).containsExactly("2", "2", "13", "00000000", "5", "1");
        assertThat(s.getGroup(7).asList())
                .containsExactly("280", "75050000", "1234567890", "v", "0", "0");
        assertThat(s.getGroup(8).asList()).containsExactly("0");

        s = segments.get(2);
        assertThat(s.getGroup(0).asList()).containsExactly("HNVSD", "999", "1");
        assertThat(s.getGroup(1).asList())
                .containsExactly(
                        "HNSHK:2:4+PIN:2+921+9302018+1+1+1::06NebYu?+qnABAAANpNOVwVkXrAQA+1+1:20200305:150541+1:999:1+6:10:16+280:75050000:1234567890:S:0:0'HKTAN:3:6+2+++8809-03-05-16.04.44.308950+N+++++'HNSHA:4:2+9302018++12345:041254'");

        s = segments.get(3);
        assertThat(s.getGroup(0).asList()).containsExactly("HNHBS", "5", "1");
        assertThat(s.getGroup(1).asList()).containsExactly("5");
    }

    @Test
    public void shouldThrowParseExceptionWhenProvidedWithEmptyMessage() {
        // given
        String rawMessage = "";

        // when
        Throwable throwable = catchThrowable(() -> FinTsParser.parse(rawMessage));

        // then
        assertThat(throwable)
                .isInstanceOf(FinTsParseException.class)
                .hasMessage("Could not find regular element starting at position: 0");
    }

    @Test
    public void shouldThrowParseExceptionWhenWrongDelimiterUsed() {
        // given
        String rawMessage = "ASDF:1:2+asdf+@10@1234567890_asdf";

        // when
        Throwable throwable = catchThrowable(() -> FinTsParser.parse(rawMessage));

        // then
        assertThat(throwable)
                .isInstanceOf(FinTsParseException.class)
                .hasMessage("Unexpected delimiter: --> _ <-- at position: 28");
    }

    @Test
    public void shouldThrowParseExceptionWhenBinaryElementLengthExceedsMessageLength() {
        // given
        String rawMessage = "ASDF:1:2+asdf+@32@1234567890_asdf";

        // when
        Throwable throwable = catchThrowable(() -> FinTsParser.parse(rawMessage));

        // then
        assertThat(throwable)
                .isInstanceOf(FinTsParseException.class)
                .hasMessage(
                        "Binary content at position: 18 reported length exceeds remaining message length. Binary element would end at: 50 , message length: 33");
    }

    @Test
    public void shouldReportSixthGroupAsEmpty() {
        // given
        String rawMessage =
                "HISAL:4:6:3+123456789::280:76030080+Name of account+EUR+D:44,25:EUR:20200316++0,:EUR+0,:EUR'";

        // when
        List<RawSegment> segments = FinTsParser.parse(rawMessage);

        // then
        assertThat(segments.get(0).getGroup(5).isEmpty()).isTrue();
    }
}
