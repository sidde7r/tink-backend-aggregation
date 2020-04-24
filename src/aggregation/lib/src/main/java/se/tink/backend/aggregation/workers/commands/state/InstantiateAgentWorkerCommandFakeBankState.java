package se.tink.backend.aggregation.workers.commands.state;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.agentfactory.iface.AgentFactory;
import se.tink.backend.aggregation.agents.framework.wiremock.WireMockTestServer;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.provider.socket.MutableFakeBankSocket;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.AapFileParser;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.RequestResponseParser;
import se.tink.backend.aggregation.agents.framework.wiremock.utils.ResourceFileReader;
import se.tink.backend.aggregation.workers.commands.state.configuration.AapFileProvider;

public class InstantiateAgentWorkerCommandFakeBankState
        implements InstantiateAgentWorkerCommandState {

    private final Logger logger =
            LoggerFactory.getLogger(InstantiateAgentWorkerCommandFakeBankState.class);

    private final AgentFactory agentFactory;
    private final MutableFakeBankSocket fakeBankSocket;
    private final AapFileProvider fakeBankAapFileMapper;
    private WireMockTestServer server;

    @Inject
    private InstantiateAgentWorkerCommandFakeBankState(
            AgentFactory agentFactory,
            MutableFakeBankSocket fakeBankSocket,
            AapFileProvider fakeBankAapFileMapper) {
        this.agentFactory = agentFactory;
        this.fakeBankSocket = fakeBankSocket;
        this.fakeBankAapFileMapper = fakeBankAapFileMapper;
    }

    public AgentFactory getAgentFactory() {
        return agentFactory;
    }

    @Override
    public void doRightBeforeInstantiation(String providerName) {
        ImmutableSet<RequestResponseParser> parsers =
                fakeBankAapFileMapper.getAapFilePaths(providerName).stream()
                        .map(filePath -> new AapFileParser(new ResourceFileReader().read(filePath)))
                        .collect(ImmutableSet.toImmutableSet());

        server = new WireMockTestServer(parsers);
        fakeBankSocket.set("localhost:" + server.getHttpsPort());
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
