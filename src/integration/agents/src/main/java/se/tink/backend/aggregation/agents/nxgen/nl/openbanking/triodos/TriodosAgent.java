package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.configuration.TriodosConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.utils.BerlinGroupUtils;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class TriodosAgent extends BerlinGroupAgent<TriodosApiClient, TriodosConfiguration> {
    private final TriodosApiClient apiClient;

    public TriodosAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new TriodosApiClient(client, sessionStorage);
    }

    @Override
    protected TriodosApiClient getApiClient() {
        apiClient.setCredentials(request.getCredentials());
        return apiClient;
    }

    @Override
    protected void setupClient(TinkHttpClient client) {
        client.setSslClientCertificate(
                BerlinGroupUtils.readFile(getConfiguration().getClientKeyStorePath()),
                getConfiguration().getClientKeyStorePassword());
    }

    @Override
    protected String getIntegrationName() {
        return TriodosConstants.INTEGRATION_NAME;
    }

    @Override
    protected Class<TriodosConfiguration> getConfigurationClassDescription() {
        return TriodosConfiguration.class;
    }
}
