package se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebankbranches.sparebank1gudbrandsdal;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SpareBank1GudbrandsdalAgent extends SparebankAgent {

    public SpareBank1GudbrandsdalAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getBaseUrl() {
        return SpareBank1GudbrandsdalConstants.BASE_URL;
    }
}
