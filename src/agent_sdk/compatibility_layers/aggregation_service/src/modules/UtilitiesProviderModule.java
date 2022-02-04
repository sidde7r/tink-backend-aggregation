package src.agent_sdk.compatibility_layers.aggregation_service.src.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import se.tink.agent.runtime.environment.UtilitiesImpl;
import se.tink.agent.sdk.environment.Utilities;
import se.tink.agent.sdk.utils.RandomGenerator;
import se.tink.agent.sdk.utils.Sleeper;
import se.tink.agent.sdk.utils.SupplementalInformationHelper;
import se.tink.agent.sdk.utils.TimeGenerator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.eidas.QSealcSignerProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.TinkHttpClientProvider;

public class UtilitiesProviderModule extends AbstractModule {

    @Singleton
    @Provides
    public Utilities provideUtilities(
            RandomGenerator randomGenerator,
            TimeGenerator timeGenerator,
            Sleeper sleeper,
            TinkHttpClientProvider tinkHttpClientProvider,
            SupplementalInformationHelper supplementalInformationHelper,
            QSealcSignerProvider qSealcSignerProvider) {

        return new UtilitiesImpl(
                randomGenerator,
                timeGenerator,
                sleeper,
                tinkHttpClientProvider.getTinkHttpClient(),
                supplementalInformationHelper,
                qSealcSignerProvider.getQsealcSigner());
    }
}
