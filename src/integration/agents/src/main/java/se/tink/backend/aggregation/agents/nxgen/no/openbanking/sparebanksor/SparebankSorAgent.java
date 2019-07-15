package se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebanksor;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebanksor.SparebankSorConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.configuration.SparebankConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class SparebankSorAgent extends SparebankAgent {

    private final String clientName;
    private final SparebankApiClient apiClient;

    public SparebankSorAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new SparebankApiClient(client, sessionStorage);
        clientName = request.getProvider().getPayload();
    }

    @Override
    protected SparebankConfiguration getClientConfiguration() {
        return configuration
                .getIntegrations()
                .getClientConfiguration(
                        SparebankSorConstants.INTEGRATION_NAME,
                        clientName,
                        SparebankConfiguration.class)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }
}
