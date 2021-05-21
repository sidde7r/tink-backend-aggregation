package se.tink.backend.aggregation.agents.nxgen.it.openbanking.postepay;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.BancoPostaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.authenticator.BancoPostaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.CbiGlobeTransactionalAccountFetcher;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;

@AgentCapabilities({CHECKING_ACCOUNTS})
public final class PostePayAgent extends CbiGlobeAgent {

    @Inject
    public PostePayAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
    }

    @Override
    protected CbiGlobeApiClient getApiClient(boolean requestManual) {
        return new BancoPostaApiClient(
                client,
                persistentStorage,
                sessionStorage,
                requestManual,
                temporaryStorage,
                getProviderConfiguration(),
                psuIpAddress);
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        if (authenticator == null) {
            authenticator =
                    new BancoPostaAuthenticator(
                            apiClient,
                            strongAuthenticationState,
                            userState,
                            getAgentConfiguration().getProviderSpecificConfiguration());
        }
        return authenticator;
    }

    @Override
    protected TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final CbiGlobeTransactionalAccountFetcher accountFetcher =
                CbiGlobeTransactionalAccountFetcher.createFromBooked(apiClient, persistentStorage);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(accountFetcher, 1)));
    }
}
