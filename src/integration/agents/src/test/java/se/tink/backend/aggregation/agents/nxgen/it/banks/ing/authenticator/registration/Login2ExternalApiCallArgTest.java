package se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.it.banks.ing.authenticator.registration.Login2ExternalApiCall.Arg;

public class Login2ExternalApiCallArgTest {

    @Test
    public void getMappedPinValueTest() {
        // given
        int givenPinDigit1 = 1;
        int givenPinDigit2 = 2;
        int givenPinDigit3 = 3;

        int givenValue1 = 9;
        int givenValue2 = 8;
        int givenValue3 = 7;

        Map<Integer, Integer> givenPinKeyboardMap =
                ImmutableMap.of(
                        0,
                        0,
                        givenPinDigit1,
                        givenValue1,
                        givenPinDigit2,
                        givenValue2,
                        givenPinDigit3,
                        givenValue3);

        List<Integer> givenPinPositions = asList(2, 4, 6);

        String givenPin =
                format("%s%s%s%s%s%s", 0, givenPinDigit1, 0, givenPinDigit2, 0, givenPinDigit3);

        Arg tested =
                new Arg()
                        .setPinPositions(givenPinPositions)
                        .setPin(givenPin)
                        .setPinKeyboardMap(givenPinKeyboardMap);

        // when
        String result1 = tested.getMappedPinValue(1);
        String result2 = tested.getMappedPinValue(2);
        String result3 = tested.getMappedPinValue(3);

        // then
        assertThat(result1).isEqualTo(String.valueOf(givenValue1));
        assertThat(result2).isEqualTo(String.valueOf(givenValue2));
        assertThat(result3).isEqualTo(String.valueOf(givenValue3));
    }
}
