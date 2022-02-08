package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.module.toggle;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.libraries.unleash.model.Toggle;
import se.tink.libraries.unleash.model.UnleashContextWrapper;
import se.tink.libraries.unleash.provider.UnleashClientProvider;

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
            AgentComponentProvider componentProvider,
            @Named("featureToggleName") String toggleName) {

        String providerName = componentProvider.getCredentialsRequest().getProvider().getName();
        String appId = componentProvider.getContext().getAppId();

        Toggle toggle =
                Toggle.of(toggleName)
                        .unleashContextWrapper(
                                UnleashContextWrapper.builder()
                                        .appId(appId)
                                        .providerName(providerName)
                                        .build())
                        .build();
        return new UkOpenBankingFlowService(unleashClientProvider.getUnleashClient(), toggle);
    }

    private static final class ToggleConstants {
        static final String FEATURE_TOGGLE_NAME = "UkOpenBankingContextManager";

        private ToggleConstants() {
            throw new UnsupportedOperationException();
        }
    }
}
