package se.tink.backend.aggregation.agents.demo;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.nxgen.agents.demo.NextGenerationDemoAgent;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.common.config.SignatureKeyPair;

public class TestDemoAgent extends NextGenerationDemoAgent {

    public TestDemoAgent(CredentialsRequest request, AgentContext context,
            SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }
}
