package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.sparekassenvendsyssel;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class SparekassenVendsysselAgent extends BankdataAgent {

    public SparekassenVendsysselAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getBaseUrl() {
        return SparekassenVendsysselConstants.BASE_URL;
    }

    @Override
    protected String getBaseAuthUrl() {
        return SparekassenVendsysselConstants.BASE_AUTH_URL;
    }
}
