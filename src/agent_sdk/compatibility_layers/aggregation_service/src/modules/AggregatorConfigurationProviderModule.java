package src.agent_sdk.compatibility_layers.aggregation_service.src.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import java.util.Map;
import se.tink.agent.runtime.operation.aggregator_configuration.AggregatorConfigurationImpl;
import se.tink.agent.sdk.operation.aggregator_configuration.AggregatorConfiguration;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.nxgen.controllers.configuration.iface.AgentConfigurationControllerable;

public class AggregatorConfigurationProviderModule extends AbstractModule {

    @Singleton
    @Provides
    public AggregatorConfiguration provideAggregatorConfiguration(
            CompositeAgentContext compositeAgentContext) {
        AgentConfigurationControllerable agentConfigurationControllerable =
                compositeAgentContext.getAgentConfigurationController();
        Map<String, Object> secretsConfiguration =
                agentConfigurationControllerable.getSecretsConfiguration();
        String qwaCertificate = getConfigurationValue(secretsConfiguration, "qwac");
        String qsealCertificate = getConfigurationValue(secretsConfiguration, "qsealc");
        String redirectUrl = getConfigurationValue(secretsConfiguration, "redirectUrl");
        String clientId =
                getConfigurationValue(secretsConfiguration, "clientId", "clientid", "client_id");
        String clientSecret =
                getConfigurationValue(
                        secretsConfiguration, "clientSecret", "clientsecret", "client_secret");
        String keyId = getConfigurationValue(secretsConfiguration, "keyId", "keyid", "key_id");

        return new AggregatorConfigurationImpl(
                qwaCertificate,
                qsealCertificate,
                redirectUrl,
                clientId,
                clientSecret,
                keyId,
                secretsConfiguration);
    }

    private String getConfigurationValue(Map<String, Object> configuration, String... keys) {
        for (String key : keys) {
            if (configuration.containsKey(key)) {
                return (String) configuration.getOrDefault(key, null);
            }
        }

        return null;
    }
}
