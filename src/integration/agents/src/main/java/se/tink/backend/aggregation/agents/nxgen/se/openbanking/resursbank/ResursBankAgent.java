package se.tink.backend.aggregation.agents.nxgen.se.openbanking.resursbank;

import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.configuration.CrosskeyMarketConfiguration;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class ResursBankAgent extends CrosskeyBaseAgent {

    private static final CrosskeyMarketConfiguration RESURSBANK_CONFIGURATION =
            new CrosskeyMarketConfiguration(
                    "resursbank",
                    "https://open-banking.resurs.com",
                    "https://open-banking-identification.resurs.com");

    public ResursBankAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, RESURSBANK_CONFIGURATION);
    }
}
