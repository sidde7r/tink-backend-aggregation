package se.tink.backend.aggregation.agents.nxgen.be.openbanking.axa;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.axa.authenticator.AxaAuthenticatorHelper;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.axa.fetcher.AxaTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersTransactionalAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.Xs2aDevelopersAuthenticatorHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.Xs2aDevelopersTransactionalAccountFetcher;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionKeyWithInitDateFromFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@AgentCapabilities({CHECKING_ACCOUNTS})
public final class AxaAgent extends Xs2aDevelopersTransactionalAgent {

    @Inject
    public AxaAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, "https://api-dailybanking.axabank.be");
    }

    @Override
    protected Xs2aDevelopersAuthenticatorHelper constructXs2aAuthenticator(
            AgentComponentProvider componentProvider) {
        return new AxaAuthenticatorHelper(
                apiClient,
                persistentStorage,
                configuration,
                componentProvider.getLocalDateTimeSource(),
                credentials);
    }

    protected TransactionalAccountRefreshController constructTransactionalAccountRefreshController(
            AgentComponentProvider agentComponentProvider) {
        final Xs2aDevelopersTransactionalAccountFetcher accountFetcher =
                new Xs2aDevelopersTransactionalAccountFetcher(apiClient, authenticatorHelper);

        final TransactionFetcher<TransactionalAccount> transactionFetcher =
                new TransactionKeyWithInitDateFromFetcherController<>(
                        request,
                        new AxaTransactionsFetcher(
                                apiClient,
                                agentComponentProvider.getLocalDateTimeSource(),
                                request.isManual()));

        return new TransactionalAccountRefreshController(
                metricRefreshController, updateController, accountFetcher, transactionFetcher);
    }
}
