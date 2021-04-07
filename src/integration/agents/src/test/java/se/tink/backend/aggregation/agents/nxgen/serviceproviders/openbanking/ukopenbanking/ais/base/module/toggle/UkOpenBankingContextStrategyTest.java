package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.toggle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import no.finn.unleash.UnleashContext;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.toggle.AgentRegisteredProperties.Constants;

public class UkOpenBankingContextStrategyTest {
    private static final String STRATEGY_NAME = "STRATEGY_NAME";
    private static final String EXAMPLE_PROVIDER_NAME = "uk-aib-oauth2";
    private static final String DIFFERENT_PROVIDER_NAME = "uk-aib-corporate-oauth2";
    private static final String ALLOWED_APP_ID = "allowed_app_id";
    private static final String EXCLUDED_APP_ID = "tink";
    private static final String EXCLUDED_APP_IDS = EXCLUDED_APP_ID + ",tink2";

    private UkOpenBankingContextStrategy strategy;

    @Before
    public void setUp() throws Exception {
        this.strategy = new UkOpenBankingContextStrategy(STRATEGY_NAME);
    }

    @Test
    public void shouldAllowWhenProviderIsMatchingAndAppIdIsAllowed() {
        // given
        UnleashContext unleashContext = prepareUnleashContext();
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put(
                UkOpenBankingStrategyProperties.Constants.EXCLUDED_APP_IDS.getValue(),
                ALLOWED_APP_ID);
        parameters.put(
                UkOpenBankingStrategyProperties.Constants.PROVIDER_NAME.getValue(),
                EXAMPLE_PROVIDER_NAME);
        // when
        boolean enabled = strategy.isEnabled(parameters, unleashContext);

        // then
        assertThat(enabled).isTrue();
    }

    @Test
    public void shouldNotAllowWhenProviderIsMatchingButAppIdIsExcluded() {
        // given
        UnleashContext unleashContext = prepareUnleashContext();
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put(
                UkOpenBankingStrategyProperties.Constants.EXCLUDED_APP_IDS.getValue(),
                EXCLUDED_APP_IDS);
        parameters.put(
                UkOpenBankingStrategyProperties.Constants.PROVIDER_NAME.getValue(),
                EXAMPLE_PROVIDER_NAME);
        // when
        boolean enabled = strategy.isEnabled(parameters, unleashContext);

        // then
        assertThat(enabled).isFalse();
    }

    @Test
    public void shouldNotAllowWhenProviderIsNotMatchingAndAppIdIsExcluded() {
        // given
        UnleashContext unleashContext = prepareUnleashContext();
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put(
                UkOpenBankingStrategyProperties.Constants.EXCLUDED_APP_IDS.getValue(),
                EXCLUDED_APP_IDS);
        parameters.put(
                UkOpenBankingStrategyProperties.Constants.PROVIDER_NAME.getValue(),
                DIFFERENT_PROVIDER_NAME);

        // when
        boolean enabled = strategy.isEnabled(parameters, unleashContext);

        // then
        assertThat(enabled).isFalse();
    }

    private UnleashContext prepareUnleashContext() {
        UnleashContext unleashContext = mock(UnleashContext.class);
        HashMap<String, String> properties = new HashMap<>();
        properties.put(Constants.PROVIDER_NAME.getValue(), EXAMPLE_PROVIDER_NAME);
        properties.put(Constants.APP_ID.getValue(), EXCLUDED_APP_ID);

        when(unleashContext.getProperties()).thenReturn(properties);
        return unleashContext;
    }
}
