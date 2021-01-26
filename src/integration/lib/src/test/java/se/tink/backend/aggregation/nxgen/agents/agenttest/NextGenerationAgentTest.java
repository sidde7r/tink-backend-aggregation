package se.tink.backend.aggregation.nxgen.agents.agenttest;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.RefreshExecutorUtils;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.framework.context.AgentTestContext;
import se.tink.backend.aggregation.agents.framework.legacy.AbstractAgentTest;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.provider.ProviderDto.ProviderTypes;

@Deprecated
public abstract class NextGenerationAgentTest<T extends NextGenerationAgent>
        extends AbstractAgentTest<T> {
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

        credentials.setStatus(CredentialsStatus.UPDATING);

        for (RefreshableItem item : RefreshableItem.sort(RefreshableItem.REFRESHABLE_ITEMS_ALL)) {
            RefreshExecutorUtils.executeSegregatedRefresher(agent, item, testContext);
        }
    }

    private Agent createAgent(RefreshInformationRequest refreshInformationRequest)
            throws Exception {
        testContext = new AgentTestContext(refreshInformationRequest.getCredentials());
        testContext.setTestContext(true);
        return factory.create(cls, refreshInformationRequest, testContext);
    }
}
