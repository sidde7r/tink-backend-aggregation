package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.toggle;

import static org.assertj.core.api.Assertions.assertThat;

import no.finn.unleash.DefaultUnleash;
import no.finn.unleash.FakeUnleash;
import no.finn.unleash.UnleashContext;
import no.finn.unleash.util.UnleashConfig;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.toggle.AgentRegisteredProperties.Constants;

public class UkOpenBankingFlowTest {

    private static final String FEATURE_TOGGLE_NAME = "UkOpenBankingContextManager";
    private static final String CURRENT_PROVIDER_NAME = "providerName";
    private static final String CURRENT_APP_ID = "appId";

    private FakeUnleash toggleService;

    private UnleashContext unleashContext;

    private UkOpenBankingFlowToggle toggle;

    @Before
    public void setUp() throws Exception {
        this.toggleService = new FakeUnleash();
        this.unleashContext =
                UnleashContext.builder()
                        .addProperty(Constants.PROVIDER_NAME.getValue(), CURRENT_PROVIDER_NAME)
                        .addProperty(Constants.APP_ID.getValue(), CURRENT_APP_ID)
                        .build();
        this.toggle =
                new UkOpenBankingFlowToggle(toggleService, unleashContext, FEATURE_TOGGLE_NAME);
    }

    @Test
    public void shouldReturnEidasProxyFlowWhenTheToggleWasEnable() {
        // given
        toggleService.enable(FEATURE_TOGGLE_NAME);

        // when
        UkOpenBankingFlow ukOpenBankingFlow = toggle.takeFlow();

        // then
        assertThat(ukOpenBankingFlow).isEqualTo(UkOpenBankingFlow.EIDAS_PROXY);
    }

    @Test
    public void shouldReturnSecretServiceFlowWhenTheToggleWasUnable() {
        // given
        toggleService.disable(FEATURE_TOGGLE_NAME);

        // when
        UkOpenBankingFlow ukOpenBankingFlow = toggle.takeFlow();

        // then
        assertThat(ukOpenBankingFlow).isEqualTo(UkOpenBankingFlow.SECRET_SERVICE);
    }

    @Test
    public void shouldReturnDefaultSettingsWhenTheServiceIsNotResponding() {
        // given
        String applicationName = "appName";
        DefaultUnleash productionVersion =
                new DefaultUnleash(
                        UnleashConfig.builder()
                                .appName(applicationName)
                                .unleashAPI("http://localhost:4242")
                                .build());
        this.toggle =
                new UkOpenBankingFlowToggle(productionVersion, unleashContext, FEATURE_TOGGLE_NAME);

        // when
        UkOpenBankingFlow ukOpenBankingFlow = toggle.takeFlow();

        // then
        assertThat(ukOpenBankingFlow).isEqualTo(UkOpenBankingFlow.SECRET_SERVICE);
    }
}
