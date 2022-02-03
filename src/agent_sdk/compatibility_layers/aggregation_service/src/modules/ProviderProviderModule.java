package src.agent_sdk.compatibility_layers.aggregation_service.src.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import se.tink.agent.sdk.operation.Provider;
import se.tink.libraries.credentials.service.CredentialsRequest;
import src.agent_sdk.runtime.src.operation.ProviderImpl;

public class ProviderProviderModule extends AbstractModule {

    @Singleton
    @Provides
    public Provider provideProvider(CredentialsRequest credentialsRequest) {
        return mapToProvider(credentialsRequest);
    }

    public static Provider mapToProvider(CredentialsRequest credentialsRequest) {
        if (credentialsRequest == null) {
            return null;
        }

        se.tink.backend.agents.rpc.Provider rpcProvider = credentialsRequest.getProvider();

        if (rpcProvider == null) {
            return new ProviderImpl(null, null, null, null);
        }

        return new ProviderImpl(
                rpcProvider.getMarket(),
                rpcProvider.getName(),
                rpcProvider.getCurrency(),
                rpcProvider.getPayload());
    }
}
