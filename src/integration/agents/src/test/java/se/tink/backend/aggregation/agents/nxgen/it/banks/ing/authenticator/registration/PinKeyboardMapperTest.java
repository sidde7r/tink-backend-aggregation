package se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.TestFixtures;

public class PinKeyboardMapperTest {

    private PinKeyboardMapper tested = new PinKeyboardMapper();

    @Test
    public void toPinKeyboardMapShouldReturnMapWhenGivenValuesAreKnown() {
        // given
        List<String> listOfImages = TestFixtures.givenKeyboardImagesBase64();

        // when
        Map<Integer, Integer> pinKeyboardMap = tested.toPinKeyboardMap(listOfImages);

        // then
        assertThat(pinKeyboardMap).isEqualTo(TestFixtures.givenMapOfKeyboardImageValueToIndex());
    }

    @Test(expected = RuntimeException.class)
    public void toPinKeyboardMapShouldThrowRuntimeExceptionWhenGivenValuesAreUnknown() {
        // given
        List<String> unknownImages = ImmutableList.of("a", "b", "c");

        // when
        Map<Integer, Integer> pinKeyboardMap = tested.toPinKeyboardMap(unknownImages);
    }
}
