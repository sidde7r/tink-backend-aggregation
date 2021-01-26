package se.tink.backend.aggregation.agents.framework;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.contexts.ProviderSessionCacheContext;
import se.tink.backend.aggregation.agents.framework.testserverclient.AgentTestServerClient;

public final class AgentTestServerSessionCacheProvider implements ProviderSessionCacheContext {

    private static final Logger log =
            LoggerFactory.getLogger(AgentTestServerSupplementalRequester.class);

    private final Provider provider;
    private final AgentTestServerClient agentTestServerClient;

    @Inject
    public AgentTestServerSessionCacheProvider(
            Provider provider, AgentTestServerClient agentTestServerClient) {
        this.provider = provider;
        this.agentTestServerClient = agentTestServerClient;
    }

    @Override
    public String getProviderSessionCache() {
        log.info(
                "Requesting provider session cache info for Financial institution id: {} from client.",
                provider.getFinancialInstitutionId());
        return agentTestServerClient.getProviderSessionCache(provider.getFinancialInstitutionId());
    }

    @Override
    public void setProviderSessionCache(String value, int expiredTimeInSeconds) {
        agentTestServerClient.setProviderSessionCache(
                provider.getFinancialInstitutionId(), value, expiredTimeInSeconds);
    }
}
