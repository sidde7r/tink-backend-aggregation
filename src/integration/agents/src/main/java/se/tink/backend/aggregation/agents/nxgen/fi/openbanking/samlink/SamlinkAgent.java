package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink.configuration.SamlinkConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink.fetcher.transactionalaccount.SamlinkTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.BerlinGroupTransactionFetcher;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class SamlinkAgent extends BerlinGroupAgent<SamlinkApiClient, SamlinkConfiguration> {
    private final SamlinkApiClient apiClient;

    public SamlinkAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        apiClient = new SamlinkApiClient(client, sessionStorage);
    }

    @Override
    protected SamlinkApiClient getApiClient() {
        return apiClient;
    }

    @Override
    protected String getIntegrationName() {
        return SamlinkConstants.INTEGRATION_NAME;
    }

    @Override
    protected Class<SamlinkConfiguration> getConfigurationClassDescription() {
        return SamlinkConfiguration.class;
    }

    @Override
    protected BerlinGroupTransactionFetcher getTransactionFetcher() {
        return new SamlinkTransactionFetcher(getApiClient(), getConfiguration());
    }
}
