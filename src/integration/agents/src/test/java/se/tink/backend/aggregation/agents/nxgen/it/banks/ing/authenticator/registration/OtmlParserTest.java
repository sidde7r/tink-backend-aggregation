package se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.TestFixtures;

public class OtmlParserTest {

    private OtmlParser tested = new OtmlParser();

    @Test
    public void getPinKeyboardImagesShouldReturnMapWhenGivenDocumentIsValid() {
        // given
        String otmlDocument = TestFixtures.givenLogin1OtmlDatasources();

        // when
        List<String> images = tested.getPinKeyboardImages(otmlDocument);

        // then
        assertThat(images).isEqualTo(TestFixtures.givenKeyboardImagesBase64());
    }

    @Test
    public void getPinKeyboardImagesShouldThrowRuntimeExceptionWhenGivenDocumentIsInvalid() {
        // given
        String otmlDocument = "invalid doc";

        // when
        Throwable t = catchThrowable(() -> tested.getPinKeyboardImages(otmlDocument));

        // then
        assertThat(t)
                .isInstanceOf(RuntimeException.class)
                .hasMessageStartingWith("OTML Parsing Error");
    }

    @Test
    public void getPinPositionsShouldReturnPositionsListWhenGivenDocumentIsValid() {
        // given
        String otmlDocument = TestFixtures.givenLogin1OtmlDatasources();

        // when
        List<Integer> images = tested.getPinPositions(otmlDocument);

        // then
        assertThat(images).isEqualTo(TestFixtures.givenPinPositions());
    }

    @Test
    public void getPinPositionsShouldThrowRuntimeExceptionWhenGivenDocumentIsInvalid() {
        // given
        String otmlDocument = "invalid doc";

        // when
        Throwable t = catchThrowable(() -> tested.getPinKeyboardImages(otmlDocument));

        // then
        assertThat(t)
                .isInstanceOf(RuntimeException.class)
                .hasMessageStartingWith("OTML Parsing Error");
    }
}
