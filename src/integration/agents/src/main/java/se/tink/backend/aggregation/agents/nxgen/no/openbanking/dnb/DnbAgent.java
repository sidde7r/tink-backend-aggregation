package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.DnbAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.DnbAuthenticatorController;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.DnbAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.DnbTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.configuration.BerlinGroupConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.BerlinGroupAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.BerlinGroupTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.utils.BerlinGroupUtils;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class DnbAgent extends BerlinGroupAgent<DnbApiClient, BerlinGroupConfiguration> {

    private final DnbApiClient apiClient;

    public DnbAgent(
            final CredentialsRequest request,
            final AgentContext context,
            final SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        final Credentials credentials = request.getCredentials();
        apiClient = new DnbApiClient(client, sessionStorage, credentials);
    }

    @Override
    protected void setupClient(final TinkHttpClient client) {
        client.setSslClientCertificate(
                BerlinGroupUtils.readFile(getConfiguration().getClientKeyStorePath()),
                getConfiguration().getClientKeyStorePassword());
    }

    @Override
    protected DnbApiClient getApiClient() {
        return apiClient;
    }

    @Override
    protected String getIntegrationName() {
        return DnbConstants.INTEGRATION_NAME;
    }

    @Override
    protected Class<BerlinGroupConfiguration> getConfigurationClassDescription() {
        return BerlinGroupConfiguration.class;
    }

    @Override
    protected DnbAuthenticator getAgentAuthenticator() {
        return new DnbAuthenticator(getApiClient());
    }

    @Override
    protected BerlinGroupAccountFetcher getAccountFetcher() {
        return new DnbAccountFetcher(getApiClient());
    }

    @Override
    protected BerlinGroupTransactionFetcher getTransactionFetcher() {
        return new DnbTransactionFetcher(getApiClient());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final DnbAuthenticatorController controller =
                new DnbAuthenticatorController(
                        supplementalInformationHelper, getAgentAuthenticator());

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
    }
}
