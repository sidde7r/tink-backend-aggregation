package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.toggle;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import no.finn.unleash.UnleashContext;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.unleashclient.UnleashClientProvider;
import se.tink.libraries.unleash.model.Toggle;
import se.tink.libraries.unleash.strategies.aggregation.providersidsandexcludeappids.Constants.Context;

public class UkOpenBankingToggleModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(String.class)
                .annotatedWith(Names.named("featureToggleName"))
                .toInstance(ToggleConstants.FEATURE_TOGGLE_NAME);
    }

    @Provides
    @Singleton
    @Inject
    UkOpenBankingFlowService ukOpenBankingFlowToggle(
            UnleashClientProvider unleashClientProvider,
            AgentComponentProvider componentProvide,
            @Named("featureToggleName") String toggleName) {
        Toggle toggle =
                Toggle.of(toggleName)
                        .context(unleashContextForUkStrategy(componentProvide))
                        .build();
        return new UkOpenBankingFlowService(unleashClientProvider.getUnleashClient(), toggle);
    }

    private static UnleashContext unleashContextForUkStrategy(
            AgentComponentProvider componentProvider) {
        String providerName = componentProvider.getCredentialsRequest().getProvider().getName();
        String appId = componentProvider.getContext().getAppId();
        return UnleashContext.builder()
                .addProperty(Context.PROVIDER_NAME.getValue(), providerName)
                .addProperty(Context.APP_ID.getValue(), appId)
                .build();
    }

    private static final class ToggleConstants {
        static final String FEATURE_TOGGLE_NAME = "UkOpenBankingContextManager";

        private ToggleConstants() {
            throw new UnsupportedOperationException();
        }
    }
}
