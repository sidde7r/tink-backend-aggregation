package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquetransatlantique;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BanqueTransatlantiqueAgent extends EuroInformationAgent {

    public BanqueTransatlantiqueAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(
                request,
                context,
                signatureKeyPair,
                new BanqueTransatlantiqueConfiguration(),
                new BanqueTransatlantiqueApiClientFactory());
    }
}
