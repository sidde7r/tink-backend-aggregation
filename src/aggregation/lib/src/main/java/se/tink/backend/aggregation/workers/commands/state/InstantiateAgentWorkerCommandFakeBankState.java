package se.tink.backend.aggregation.workers.commands.state;

import java.net.InetSocketAddress;
import javax.inject.Inject;
import se.tink.backend.aggregation.agents.agentfactory.iface.AgentFactory;
import se.tink.backend.aggregation.agents.framework.wiremock.WireMockTestServer;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.provider.FakeBankAapFile;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.provider.FakeBankSocket;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.AapFileParser;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.ResourceFileReader;

public class InstantiateAgentWorkerCommandFakeBankState
        implements InstantiateAgentWorkerCommandState {

    private final AgentFactory agentFactory;
    private final InetSocketAddress fakeBankSocket;
    private final String fakeBankAapFile;
    private WireMockTestServer server;

    @Inject
    public InstantiateAgentWorkerCommandFakeBankState(
            AgentFactory agentFactory,
            @FakeBankSocket InetSocketAddress fakeBankSocket,
            @FakeBankAapFile String fakeBankAapFile) {
        this.agentFactory = agentFactory;
        this.fakeBankSocket = fakeBankSocket;
        this.fakeBankAapFile = fakeBankAapFile;
    }

    public AgentFactory getAgentFactory() {
        return agentFactory;
    }

    @Override
    public void doRightAfterInstantiation() {

        server = new WireMockTestServer(10001, fakeBankSocket.getPort());

        server.prepareMockServer(new AapFileParser(new ResourceFileReader().read(fakeBankAapFile)));
    }

    @Override
    public void doAtInstantiationPostProcess() {
        server.shutdown();
    }
}
