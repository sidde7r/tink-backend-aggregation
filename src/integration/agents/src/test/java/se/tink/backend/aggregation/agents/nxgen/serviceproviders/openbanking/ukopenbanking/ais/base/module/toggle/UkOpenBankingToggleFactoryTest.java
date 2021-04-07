package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.toggle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.finn.unleash.UnleashContext;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.UnleashConfiguration;

@RunWith(MockitoJUnitRunner.class)
public class UkOpenBankingToggleFactoryTest {
    private static final String TOGGLE_NAME = "toggle_name";
    private static final String APPLICATION_NAME = "application_name";

    @Mock private UnleashContext unleashContext;

    @Mock private AgentsServiceConfiguration configuration;

    @Mock private UkOpenBankingContextStrategy ukOpenBankingContextStrategy;

    private UkOpenBankingToggleFactory toggleFactory;

    @Before
    public void setUp() throws Exception {
        this.toggleFactory =
                new UkOpenBankingToggleFactory(
                        unleashContext,
                        configuration,
                        TOGGLE_NAME,
                        APPLICATION_NAME,
                        ukOpenBankingContextStrategy);
    }

    @Test
    public void shouldInitializeUkOpenBankingFlowToggle() {
        // given
        UnleashConfiguration unleashConfiguration = mock(UnleashConfiguration.class);
        when(configuration.getUnleashConfiguration()).thenReturn(unleashConfiguration);
        when(unleashConfiguration.getBaseApiUrl()).thenReturn("http://localhost");

        // when
        UkOpenBankingFlowToggle flowToggle = toggleFactory.get();

        // then
        assertThat(flowToggle).isNotNull();
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenBaseUrlIsBlank() {
        // given
        UnleashConfiguration unleashConfiguration = mock(UnleashConfiguration.class);
        when(configuration.getUnleashConfiguration()).thenReturn(unleashConfiguration);
        when(unleashConfiguration.getBaseApiUrl()).thenReturn("");

        // when
        ThrowingCallable throwingCallable = () -> toggleFactory.get();

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldThrowNullPointerExceptionWhenBaseUrlIsNull() {
        // given
        UnleashConfiguration unleashConfiguration = mock(UnleashConfiguration.class);
        when(configuration.getUnleashConfiguration()).thenReturn(unleashConfiguration);
        when(unleashConfiguration.getBaseApiUrl()).thenReturn(null);

        // when
        ThrowingCallable throwingCallable = () -> toggleFactory.get();

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(NullPointerException.class);
    }
}
