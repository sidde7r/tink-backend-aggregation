package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.toggle;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import no.finn.unleash.DefaultUnleash;
import no.finn.unleash.UnleashContext;
import no.finn.unleash.util.UnleashConfig;
import org.apache.commons.lang3.Validate;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.UnleashConfiguration;

public class UkOpenBankingToggleFactory implements Provider<UkOpenBankingFlowToggle> {
    /**
     * We don't want to retry when we weren't able to call to the service. We need this information
     * at the very beginning so the future retries are redundant
     */
    private static final long MAX_INTERVALS = Long.MAX_VALUE;

    private final UnleashContext unleashContext;
    private final AgentsServiceConfiguration configuration;
    private final String flagName;
    private final String applicationName;
    private final UkOpenBankingContextStrategy ukOpenBankingContextStrategy;

    @Inject
    public UkOpenBankingToggleFactory(
            UnleashContext unleashContext,
            AgentsServiceConfiguration configuration,
            @Named("featureToggleName") String toggleName,
            @Named("applicationName") String applicationName,
            UkOpenBankingContextStrategy ukOpenBankingContextStrategy) {
        this.unleashContext = unleashContext;
        this.configuration = configuration;
        this.flagName = toggleName;
        this.applicationName = applicationName;
        this.ukOpenBankingContextStrategy = ukOpenBankingContextStrategy;
    }

    @Override
    public UkOpenBankingFlowToggle get() {
        UnleashConfiguration unleashConfiguration = configuration.getUnleashConfiguration();
        String baseApiUrl = unleashConfiguration.getBaseApiUrl();
        Validate.notBlank(baseApiUrl);

        DefaultUnleash unleash =
                new DefaultUnleash(
                        UnleashConfig.builder()
                                .appName(applicationName)
                                .unleashAPI(baseApiUrl)
                                .fetchTogglesInterval(MAX_INTERVALS)
                                .build(),
                        ukOpenBankingContextStrategy);
        return new UkOpenBankingFlowToggle(unleash, unleashContext, flagName);
    }
}
