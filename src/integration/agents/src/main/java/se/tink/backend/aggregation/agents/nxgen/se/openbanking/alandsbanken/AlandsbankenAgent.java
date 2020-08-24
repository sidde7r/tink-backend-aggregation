package se.tink.backend.aggregation.agents.nxgen.se.openbanking.alandsbanken;

import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.configuration.CrosskeyMarketConfiguration;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class AlandsbankenAgent extends CrosskeyBaseAgent {

    private static final CrosskeyMarketConfiguration ALANDSBANKEN_SE_CONFIGURATION =
            new CrosskeyMarketConfiguration(
                    "alandsbanken-se",
                    "https://api.alandsbanken.se",
                    "https://open.alandsbanken.se");

    public AlandsbankenAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, ALANDSBANKEN_SE_CONFIGURATION);
    }
}
