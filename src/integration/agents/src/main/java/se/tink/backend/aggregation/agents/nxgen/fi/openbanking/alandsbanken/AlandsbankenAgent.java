package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.alandsbanken;

import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.configuration.CrosskeyMarketConfiguration;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class AlandsbankenAgent extends CrosskeyBaseAgent {

    private static final CrosskeyMarketConfiguration ALANDSBANKEN_FI_CONFIGURATION =
            new CrosskeyMarketConfiguration(
                    "alandsbanken-fi",
                    "https://api.alandsbanken.fi",
                    "https://open.alandsbanken.fi");

    public AlandsbankenAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, ALANDSBANKEN_FI_CONFIGURATION);
    }
}
