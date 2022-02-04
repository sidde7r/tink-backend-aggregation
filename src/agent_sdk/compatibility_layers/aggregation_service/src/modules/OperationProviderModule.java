package src.agent_sdk.compatibility_layers.aggregation_service.src.modules;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map.Entry;
import lombok.extern.slf4j.Slf4j;
import se.tink.agent.sdk.environment.Operation;
import se.tink.agent.sdk.operation.Provider;
import se.tink.agent.sdk.operation.StaticBankCredentials;
import se.tink.agent.sdk.operation.User;
import se.tink.agent.sdk.operation.aggregator_configuration.AggregatorConfiguration;
import se.tink.agent.sdk.storage.SerializableStorage;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.http_proxy.ProxyProfilesProvider;
import src.agent_sdk.runtime.src.environment.OperationImpl;
import src.agent_sdk.runtime.src.storage.RawAgentStorage;

@Slf4j
public class OperationProviderModule extends AbstractModule {

    @Singleton
    @Provides
    public Operation provideOperation(
            RawAgentStorage rawAgentStorage,
            User user,
            Provider provider,
            StaticBankCredentials staticBankCredentials,
            AggregatorConfiguration aggregatorConfiguration,
            AgentsServiceConfiguration agentServiceConfiguration,
            ProxyProfilesProvider proxyProfilesProvider) {

        SerializableStorage agentStorage = createAgentStorage(rawAgentStorage);

        return new OperationImpl(
                agentStorage,
                user,
                provider,
                staticBankCredentials,
                aggregatorConfiguration,
                agentServiceConfiguration.getEidasProxy(),
                proxyProfilesProvider.getProxyProfiles());
    }

    private SerializableStorage createAgentStorage(RawAgentStorage rawAgentStorage) {
        HashMap<String, String> agentStorageData = new HashMap<>();

        rawAgentStorage
                .getValue(
                        Field.Key.PERSISTENT_STORAGE,
                        new TypeReference<HashMap<String, String>>() {})
                .ifPresent(rawStorage -> combineMaps(agentStorageData, rawStorage));

        rawAgentStorage
                .getValue(
                        Field.Key.SESSION_STORAGE, new TypeReference<HashMap<String, String>>() {})
                .ifPresent(rawStorage -> combineMaps(agentStorageData, rawStorage));

        return SerializableStorage.from(agentStorageData);
    }

    private void combineMaps(HashMap<String, String> targetMap, HashMap<String, String> sourceMap) {
        for (Entry<String, String> entry : sourceMap.entrySet()) {
            if (targetMap.containsKey(entry.getKey())) {
                log.warn("Duplicate keys in agent storage: {}", entry.getKey());
                continue;
            }

            targetMap.put(entry.getKey(), entry.getValue());
        }
    }
}
