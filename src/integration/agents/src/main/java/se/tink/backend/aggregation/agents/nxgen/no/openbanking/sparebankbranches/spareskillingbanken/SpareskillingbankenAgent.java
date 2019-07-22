package se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebankbranches.spareskillingbanken;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SpareskillingbankenAgent extends SparebankAgent {

    public SpareskillingbankenAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getBaseUrl() {
        return SpareskillingbankenConstants.BASE_URL;
    }
}
