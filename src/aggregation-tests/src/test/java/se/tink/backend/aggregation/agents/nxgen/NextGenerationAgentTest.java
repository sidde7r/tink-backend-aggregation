package se.tink.backend.aggregation.agents.nxgen;

import se.tink.backend.aggregation.agents.AbstractAgentTest;
import se.tink.backend.aggregation.agents.Agent;
import se.tink.backend.aggregation.agents.AgentTestContext;
import se.tink.backend.aggregation.agents.RefreshableItemExecutor;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.backend.aggregation.rpc.ProviderTypes;
import se.tink.backend.aggregation.rpc.RefreshInformationRequest;
import se.tink.backend.aggregation.rpc.RefreshableItem;

public abstract class NextGenerationAgentTest<T extends NextGenerationAgent> extends AbstractAgentTest<T> {
    protected NextGenerationAgentTest(Class<T> cls) {
        super(cls);
    }

    @Override
    public Provider constructProvider() {
        Provider provider = super.constructProvider();

        provider.setName(cls.getSimpleName());
        provider.setClassName(cls.getName());
        provider.setType(ProviderTypes.TEST);
        provider.setMarket("TEST");
        provider.setCurrency(getCurrency());

        return provider;
    }

    public abstract String getCurrency();

    protected void testLogin(Credentials credentials) throws Exception {
        Agent agent = createAgent(createRefreshInformationRequest(credentials));

        agent.login();
    }

    protected void testRefresh(Credentials credentials) throws Exception {
        Agent agent = createAgent(createRefreshInformationRequest(credentials));

        agent.login();

        if (!(agent instanceof RefreshableItemExecutor)) {
            throw new AssertionError(String.format("%s is not an instance of RefreshExecutor",
                    agent.getClass().getSimpleName()));
        }

        credentials.setStatus(CredentialsStatus.UPDATING);

        RefreshableItemExecutor refreshExecutor = (RefreshableItemExecutor) agent;
        for (RefreshableItem item : RefreshableItem.values()) {
            refreshExecutor.refresh(item);
        }
    }

    private Agent createAgent(RefreshInformationRequest refreshInformationRequest) throws Exception {
        testContext = new AgentTestContext(this, refreshInformationRequest.getCredentials());
        testContext.setTestContext(true);
        return factory.create(cls, refreshInformationRequest, testContext);
    }
}
