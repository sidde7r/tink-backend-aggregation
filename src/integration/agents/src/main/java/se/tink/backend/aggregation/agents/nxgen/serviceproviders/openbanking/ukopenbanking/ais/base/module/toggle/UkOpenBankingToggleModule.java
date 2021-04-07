package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.toggle;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import no.finn.unleash.UnleashContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.toggle.AgentRegisteredProperties.Constants;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class UkOpenBankingToggleModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(String.class)
                .annotatedWith(Names.named("featureToggleName"))
                .toInstance(ToggleConstants.FEATURE_TOGGLE_NAME);
        bind(String.class)
                .annotatedWith(Names.named("applicationName"))
                .toInstance(ToggleConstants.APPLICATION_NAME);
        bind(String.class)
                .annotatedWith(Names.named("strategyName"))
                .toInstance(ToggleConstants.STRATEGY_NAME);
        bind(UkOpenBankingFlowToggle.class).toProvider(UkOpenBankingToggleFactory.class);
    }

    @Provides
    @Singleton
    UkOpenBankingContextStrategy ukContextManagerStrategy(
            @Named("strategyName") String strategyName) {
        return new UkOpenBankingContextStrategy(strategyName);
    }

    @Provides
    @Singleton
    UnleashContext unleashContextForUkStrategy(AgentComponentProvider componentProvider) {
        String providerName = componentProvider.getCredentialsRequest().getProvider().getName();
        String appId = componentProvider.getContext().getAppId();
        return UnleashContext.builder()
                .addProperty(Constants.PROVIDER_NAME.getValue(), providerName)
                .addProperty(Constants.APP_ID.getValue(), appId)
                .build();
    }

    private static final class ToggleConstants {
        static final String APPLICATION_NAME = "UK_OPENBANKING_CONTEXT_MANAGER";
        static final String FEATURE_TOGGLE_NAME = "UkOpenBankingContextManager";
        static final String STRATEGY_NAME = "enableEidasProxy";

        private ToggleConstants() {
            throw new UnsupportedOperationException();
        }
    }
}
