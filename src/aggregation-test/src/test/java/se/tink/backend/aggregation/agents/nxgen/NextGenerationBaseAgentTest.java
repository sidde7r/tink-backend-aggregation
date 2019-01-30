package se.tink.backend.aggregation.agents.nxgen;

import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.agents.rpc.ProviderTypes;
import se.tink.backend.aggregation.agents.AbstractAgentTest;
import se.tink.backend.aggregation.agents.Agent;
import se.tink.backend.aggregation.agents.AgentTestContext;
import se.tink.backend.aggregation.agents.RefreshableItemExecutor;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.credentials.service.RefreshableItem;

public abstract class NextGenerationBaseAgentTest<T extends NextGenerationAgent> extends AbstractAgentTest<T> {
    protected NextGenerationBaseAgentTest(Class<T> cls) {
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
        testContext = new AgentTestContext(refreshInformationRequest.getCredentials());
        testContext.setTestContext(true);
        return factory.create(cls, refreshInformationRequest, testContext);
    }
}
