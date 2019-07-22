package se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebankbranches.sparebank1lom;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SpareBank1LomAgent extends SparebankAgent {

    public SpareBank1LomAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getBaseUrl() {
        return SpareBank1LomConstants.BASE_URL;
    }
}
