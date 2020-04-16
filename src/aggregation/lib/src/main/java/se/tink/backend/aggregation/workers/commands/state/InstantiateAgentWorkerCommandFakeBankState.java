package se.tink.backend.aggregation.workers.commands.state;

import java.net.InetSocketAddress;
import javax.inject.Inject;
import se.tink.backend.aggregation.agents.agentfactory.iface.AgentFactory;
import se.tink.backend.aggregation.agents.framework.wiremock.WireMockTestServer;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.provider.FakeBankSocket;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.AapFileParser;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.ResourceFileReader;

public class InstantiateAgentWorkerCommandFakeBankState
        implements InstantiateAgentWorkerCommandState {

    private final AgentFactory agentFactory;
    private final InetSocketAddress fakeBankSocket;
    private WireMockTestServer server;

    @Inject
    public InstantiateAgentWorkerCommandFakeBankState(
            AgentFactory agentFactory, @FakeBankSocket InetSocketAddress fakeBankSocket) {
        this.agentFactory = agentFactory;
        this.fakeBankSocket = fakeBankSocket;
    }

    public AgentFactory getAgentFactory() {
        return agentFactory;
    }

    @Override
    public void doRightAfterInstantiation() {

        server = new WireMockTestServer(10001, fakeBankSocket.getPort());

        final String file =
                "src/aggregation/fakebank/src/main/java/se/tink/backend/fakebank/resources/amex.aap";

        server.prepareMockServer(new AapFileParser(new ResourceFileReader().read(file)));
    }

    @Override
    public void doAtInstantiationPostProcess() {
        server.shutdown();
    }
}
