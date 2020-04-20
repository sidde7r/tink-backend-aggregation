package se.tink.backend.aggregation.workers.commands.state;

import java.io.IOException;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.agentfactory.iface.AgentFactory;
import se.tink.backend.aggregation.agents.framework.wiremock.WireMockTestServer;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.provider.FakeBankAapFile;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.provider.socket.MutableFakeBankSocket;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.AapFileParser;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.ResourceFileReader;

public class InstantiateAgentWorkerCommandFakeBankState
        implements InstantiateAgentWorkerCommandState {

    private final Logger logger =
            LoggerFactory.getLogger(InstantiateAgentWorkerCommandFakeBankState.class);

    private final AgentFactory agentFactory;
    private final MutableFakeBankSocket fakeBankSocket;
    private final String fakeBankAapFile;
    private WireMockTestServer server;

    @Inject
    public InstantiateAgentWorkerCommandFakeBankState(
            AgentFactory agentFactory,
            MutableFakeBankSocket fakeBankSocket,
            @FakeBankAapFile String fakeBankAapFile) {
        this.agentFactory = agentFactory;
        this.fakeBankSocket = fakeBankSocket;
        this.fakeBankAapFile = fakeBankAapFile;
    }

    public AgentFactory getAgentFactory() {
        return agentFactory;
    }

    @Override
    public void doRightBeforeInstantiation() {

        server = new WireMockTestServer();

        fakeBankSocket.set("localhost:" + server.getHttpsPort());

        server.prepareMockServer(new AapFileParser(new ResourceFileReader().read(fakeBankAapFile)));
    }

    @Override
    public void doAtInstantiationPostProcess() {
        fakeBankSocket.set(null); // Shouldn't be needed but for defensiveness sake
        if (server.hadEncounteredAnError()) {
            try {
                logger.error(server.createErrorLogForFailedRequest());
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        server.shutdown();
    }
}
