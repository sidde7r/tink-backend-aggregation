package se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebankbranches.sparebankenost;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SparebankenOstAgent extends SparebankAgent {

    public SparebankenOstAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getBaseUrl() {
        return SparebankenOstConstants.BASE_URL;
    }
}
