package se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.toggle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.libraries.unleash.UnleashClient;

@RunWith(JUnitParamsRunner.class)
public class UnicreditEmbeddedFlowToggleTest {

    private final UnleashClient client = mock(UnleashClient.class);
    private final UnicreditEmbeddedFlowToggle embeddedFlowToggle =
            new UnicreditEmbeddedFlowToggle(client);

    @Test
    @Parameters({"true", "false"})
    public void isEnabledShouldReturnToggleState(boolean expectedResult) {
        // given
        given(client.isToggleEnabled(any())).willReturn(expectedResult);
        // when
        boolean result = embeddedFlowToggle.isEnabled();
        // then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void isEnabledShouldReturnFalseWhenExceptionIsThrown() {
        // given
        given(client.isToggleEnabled(any())).willThrow(RuntimeException.class);
        // when
        boolean enabled = embeddedFlowToggle.isEnabled();
        // then
        assertThat(enabled).isFalse();
    }
}
