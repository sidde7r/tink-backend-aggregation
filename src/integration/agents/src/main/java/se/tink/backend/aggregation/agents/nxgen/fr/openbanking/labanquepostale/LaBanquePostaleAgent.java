package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.authenticator.LaBanquePostaleAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.configuration.LaBanquePostaleConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.LaBanquePostaleTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.BerlinGroupAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.BerlinGroupTransactionFetcher;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class LaBanquePostaleAgent
    extends BerlinGroupAgent<LaBanquePostaleApiClient, LaBanquePostaleConfiguration> {

    private final String clientName;
    private final LaBanquePostaleApiClient apiClient;

    public LaBanquePostaleAgent(
            final CredentialsRequest request,
            final AgentContext context,
            final SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        client.setDebugOutput(true);
        clientName = request.getProvider().getPayload();

        apiClient = new LaBanquePostaleApiClient(client, sessionStorage);
    }

    @Override
    protected LaBanquePostaleApiClient getApiClient() {
        return apiClient;
    }

    @Override
    public String getIntegrationName() {
        return LaBanquePostaleConstants.INTEGRATION_NAME;
    }

    @Override
    protected Class<LaBanquePostaleConfiguration> getConfigurationClassDescription() {
        return LaBanquePostaleConfiguration.class;
    }

    @Override
    protected LaBanquePostaleAuthenticator getAgentAuthenticator() {
        return new LaBanquePostaleAuthenticator(getApiClient(), sessionStorage);
    }

    @Override
    protected Authenticator constructAuthenticator(){
        return new LaBanquePostaleAuthenticator(getApiClient(), sessionStorage);
    }

    @Override
    protected BerlinGroupAccountFetcher getAccountFetcher() {
        return new BerlinGroupAccountFetcher(getApiClient());
    }

    @Override
    protected BerlinGroupTransactionFetcher getTransactionFetcher(){
        return new LaBanquePostaleTransactionFetcher(getApiClient());
    }


    @Override
    public String getClientName() {
        return clientName;
    }
}
