package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.List;
import java.util.Optional;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;

public class FinTsResponseTest {

    private static final String EXAMPLE_UNSUCCESSFUL_RESPONSE =
            "HNHBK:1:3+000000000361+300+asdf+1+asdf:1'HNVSK:998:3+PIN:2+998+1+2::asdf+1+2:2:13:@8@00000000:6:1+280:12345:123456789:V:0:0+0'HNVSD:999:1+@123@HIRMG:2:2:+9050::Nachricht teilweise fehlerhaft.'HIRMS:3:2:5+9010::Der gewünschte Geschäftsvorfall wird nicht unterstützt.''HNHBS:4:1+1'";
    private static final String EXAMPLE_SUCCESSFUL_RESPONSE =
            "HNHBK:1:3+000000000472+300+asdfg=+5+asdfg=:5'HNVSK:998:3+PIN:2+998+1+2::asdfzxcv+1:20200221:153243+2:2:13:@8@:5:1+280:70150000:1005031263:V:0:0+0'HNVSD:999:1+@200@HNSHK:2:4+PIN:2+921+2060861052+1+1+2::asdfzxcv+1+1:20200221:153243+1:999:1+6:10:16+280:70150000:1005031263:S:0:0'HIRMG:3:2+0010::Nachricht entgegengenommen.+0100::Dialog beendet.'HNSHA:4:2+2060861052''HNHBS:5:1+5'";
    private static final String EXAMPLE_JUST_HEADERS_RESPONSE =
            "HNHBK:1:3+000000000361+300+asdf+1+asdf:1'HNHBK:1:3+000000000361+300+asdf+1+asdf:1'HNHBK:1:3+000000000361+300+asdf+1+asdf:1'";

    private class UnknownSegment extends BaseResponsePart {
        UnknownSegment(RawSegment rawSegment) {
            super(rawSegment);
        }

        @Override
        protected List<Integer> getSupportedVersions() {
            return null;
        }
    }

    @Test
    public void shouldThrowWhenAskedAboutUnsupportedSegments() {
        // given
        FinTsResponse response = new FinTsResponse(EXAMPLE_UNSUCCESSFUL_RESPONSE);

        // when
        Throwable throwable = catchThrowable(() -> response.findSegments(UnknownSegment.class));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Provided type is not supported. Please add an entry in FinTsResponse class.");
    }

    @Test
    public void shouldThrowWhenAskedAboutUnsupportedSegment() {
        // given
        FinTsResponse response = new FinTsResponse(EXAMPLE_UNSUCCESSFUL_RESPONSE);

        // when
        Throwable throwable = catchThrowable(() -> response.findSegment(UnknownSegment.class));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Provided type is not supported. Please add an entry in FinTsResponse class.");
    }

    @Test
    public void shouldReturnListWithSomeElementsForSupportedSegment() {
        // given
        FinTsResponse response = new FinTsResponse(EXAMPLE_JUST_HEADERS_RESPONSE);

        // when
        List<Header> segments = response.findSegments(Header.class);

        // then
        assertThat(segments).hasSize(3);
    }

    @Test
    public void shouldReturnFilledOptionalWhenAskedForJustOneSegment() {
        // given
        FinTsResponse response = new FinTsResponse(EXAMPLE_JUST_HEADERS_RESPONSE);

        // when
        Optional<Header> maybeSegment = response.findSegment(Header.class);

        // then
        assertThat(maybeSegment.isPresent()).isTrue();
    }

    @Test
    public void shouldReturnNoElementsIfSegmentNotPresentInMessage() {
        // given
        FinTsResponse response = new FinTsResponse(EXAMPLE_JUST_HEADERS_RESPONSE);

        // when
        List<TanContext> maybeSegment = response.findSegments(TanContext.class);

        // then
        assertThat(maybeSegment).hasSize(0);
    }

    @Test
    public void shouldReturnEmptyOptionalIfSegmentNotPresentInMessage() {
        // given
        FinTsResponse response = new FinTsResponse(EXAMPLE_JUST_HEADERS_RESPONSE);

        // when
        Optional<TanContext> maybeSegment = response.findSegment(TanContext.class);

        // then
        assertThat(maybeSegment.isPresent()).isFalse();
    }

    @Test
    public void shouldReturnElementsFromWithinEncryptionEnvelope() {
        // given
        FinTsResponse response = new FinTsResponse(EXAMPLE_UNSUCCESSFUL_RESPONSE);

        // when
        Optional<MessageStatus> maybeSegment = response.findSegment(MessageStatus.class);

        // then
        assertThat(maybeSegment.isPresent()).isTrue();
    }

    @Test
    public void shouldThrowWhenAskedAboutNull() {
        // given
        FinTsResponse response = new FinTsResponse(EXAMPLE_UNSUCCESSFUL_RESPONSE);

        // when
        Throwable throwable = catchThrowable(() -> response.findSegments(null));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Provided type is not supported. Please add an entry in FinTsResponse class.");
    }

    @Test
    public void shouldJudgeMessageToBeNotSuccessful() {
        // given
        FinTsResponse response = new FinTsResponse(EXAMPLE_UNSUCCESSFUL_RESPONSE);

        // when
        boolean isSuccess = response.isSuccess();

        // then
        assertThat(isSuccess).isFalse();
    }

    @Test
    public void shouldJudgeMessageToBeSuccessful() {
        // given
        FinTsResponse response = new FinTsResponse(EXAMPLE_SUCCESSFUL_RESPONSE);

        // when
        boolean isSuccess = response.isSuccess();

        // then
        assertThat(isSuccess).isTrue();
    }

    @Test
    public void shouldReturnTrue() {
        // given
        FinTsResponse response = new FinTsResponse(EXAMPLE_SUCCESSFUL_RESPONSE);

        // when
        boolean isSuccess = response.isSuccess();

        // then
        assertThat(isSuccess).isTrue();
    }

    @Test
    public void shouldReportOnStatusCodePresenceCorrectly() {
        // given
        FinTsResponse response =
                new FinTsResponse(
                        "HIRMG:1:2+3060::MSG001'HIRMS:2:2:6+0020::MSG002'HIRMS:3:2:4+3050::MSG003+3920::MSG004:921+0020::MSG005'HIRMS:4:2:5+3076::MSG006'");

        // when
        boolean hasStatus3060 = response.hasStatusCodeOf("3060");
        boolean hasStatus0020 = response.hasStatusCodeOf("0020");
        boolean hasStatus3050 = response.hasStatusCodeOf("3050");
        boolean hasStatus3920 = response.hasStatusCodeOf("3920");
        boolean hasStatus9999 = response.hasStatusCodeOf("9999");
        boolean anyPresentPass =
                response.hasAnyOfStatusCodes("3060", "0020", "3050", "3920", "9999");
        boolean anyPresentFail = response.hasAnyOfStatusCodes("a", "b", "c", "d", "e");

        // then
        assertThat(hasStatus3060).isTrue();
        assertThat(hasStatus0020).isTrue();
        assertThat(hasStatus3050).isTrue();
        assertThat(hasStatus3920).isTrue();
        assertThat(hasStatus9999).isFalse();
        assertThat(anyPresentPass).isTrue();
        assertThat(anyPresentFail).isFalse();
    }
}
