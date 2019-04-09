package se.tink.backend.aggregation.agents.nxgen.be.banks.beobank;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BeobankAgent extends EuroInformationAgent {
    public BeobankAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new BeobankConfiguration());
    }
}
