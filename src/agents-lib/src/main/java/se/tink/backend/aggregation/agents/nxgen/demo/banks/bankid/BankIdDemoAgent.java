package se.tink.backend.aggregation.agents.nxgen.demo.banks.bankid;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.nxgen.agents.demo.NextGenerationDemoAgent;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.common.config.SignatureKeyPair;

public class BankIdDemoAgent extends NextGenerationDemoAgent {
    public BankIdDemoAgent(CredentialsRequest request,
            AgentContext context,
            SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }
}
