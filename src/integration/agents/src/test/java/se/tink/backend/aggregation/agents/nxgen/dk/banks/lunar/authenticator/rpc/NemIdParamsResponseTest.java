package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.rpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class NemIdParamsResponseTest {

    @Test
    @Parameters({
        "challenge=1234567890123, 1234567890123",
        "challenge=1, 1",
        "challenge=test, test",
        "challenge=123test, 123test",
        "testochallenge=123, 123",
        "test challenge=123, 123",
    })
    public void shouldGetChallenge(String signProperties, String challenge) {
        // given
        NemIdParamsResponse nemIdParamsResponse = new NemIdParamsResponse();
        nemIdParamsResponse.setSignProperties(signProperties);

        // when
        String result = nemIdParamsResponse.getChallenge();

        // then
        assertThat(result).isEqualTo(challenge);
    }

    @Test
    @Parameters(method = "incorrectChallengeParameters")
    public void shouldThrowLoginErrorWhenCannotFetchNemIdParamsResponse(String signProperties) {
        // given
        NemIdParamsResponse nemIdParamsResponse = new NemIdParamsResponse();
        nemIdParamsResponse.setSignProperties(signProperties);

        // when
        Throwable result = catchThrowable(() -> nemIdParamsResponse.getChallenge());

        // then
        assertThat(result)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Response does not contain challenge!");
    }

    private Object[] incorrectChallengeParameters() {
        return new Object[] {
            new Object[] {"challenge"},
            new Object[] {"challenge="},
            new Object[] {null},
            new Object[] {""},
            new Object[] {"test"},
            new Object[] {"challenge = 123"},
        };
    }
}
