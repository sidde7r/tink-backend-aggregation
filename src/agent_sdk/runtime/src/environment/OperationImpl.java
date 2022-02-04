package src.agent_sdk.runtime.src.environment;

import lombok.AllArgsConstructor;
import se.tink.agent.sdk.environment.Operation;
import se.tink.agent.sdk.operation.Provider;
import se.tink.agent.sdk.operation.StaticBankCredentials;
import se.tink.agent.sdk.operation.User;
import se.tink.agent.sdk.operation.aggregator_configuration.AggregatorConfiguration;
import se.tink.agent.sdk.operation.http.ProxyProfiles;
import se.tink.agent.sdk.storage.Storage;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;

@AllArgsConstructor
public class OperationImpl implements Operation {
    private final Storage agentStorage;
    private final User user;
    private final Provider provider;
    private final StaticBankCredentials staticBankCredentials;
    private final AggregatorConfiguration aggregatorConfiguration;
    private final EidasProxyConfiguration eidasProxyConfiguration;
    private final ProxyProfiles proxyProfiles;

    @Override
    public Storage getAgentStorage() {
        return this.agentStorage;
    }

    @Override
    public User getUser() {
        return this.user;
    }

    @Override
    public Provider getProvider() {
        return this.provider;
    }

    @Override
    public StaticBankCredentials getStaticBankCredentials() {
        return this.staticBankCredentials;
    }

    @Override
    public AggregatorConfiguration getAggregatorConfiguration() {
        return this.aggregatorConfiguration;
    }

    @Override
    public EidasProxyConfiguration getEidasProxyConfiguration() {
        return this.eidasProxyConfiguration;
    }

    @Override
    public ProxyProfiles getProxyProfiles() {
        return this.proxyProfiles;
    }
}
