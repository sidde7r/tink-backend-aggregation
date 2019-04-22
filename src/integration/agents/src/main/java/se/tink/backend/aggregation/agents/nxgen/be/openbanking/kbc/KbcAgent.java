package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.configuration.BerlinGroupConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.utils.BerlinGroupUtils;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class KbcAgent extends BerlinGroupAgent<KbcApiClient, BerlinGroupConfiguration> {
    private KbcApiClient apiClient;

    public KbcAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        Credentials credentials = request.getCredentials();
        apiClient = new KbcApiClient(client, sessionStorage, credentials);
    }

    @Override
    protected void setupClient(TinkHttpClient client) {
        client.setSslClientCertificate(
                BerlinGroupUtils.readFile(getConfiguration().getClientKeyStorePath()),
                getConfiguration().getClientKeyStorePassword());
    }

    @Override
    protected KbcApiClient getApiClient() {
        return apiClient;
    }

    @Override
    protected String getIntegrationName() {
        return KbcConstants.INTEGRATION_NAME;
    }

    @Override
    protected Class<BerlinGroupConfiguration> getConfigurationClassDescription() {
        return BerlinGroupConfiguration.class;
    }
}
