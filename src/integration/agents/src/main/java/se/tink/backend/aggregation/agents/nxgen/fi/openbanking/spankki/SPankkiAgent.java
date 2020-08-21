package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.spankki;

import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.configuration.CrosskeyMarketConfiguration;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class SPankkiAgent extends CrosskeyBaseAgent {

    private static final CrosskeyMarketConfiguration SPANKKI_CONFIGURATION =
            new CrosskeyMarketConfiguration(
                    "s-pankki", "https://api.s-pankki.fi", "https://openbanking.s-pankki.fi");

    public SPankkiAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, SPANKKI_CONFIGURATION);
    }
}
