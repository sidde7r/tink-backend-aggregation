package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bpost;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bpost.authenticator.BPostAuthenticatorHelper;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bpost.fetcher.BpostTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersTransactionalAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.Xs2aDevelopersAuthenticatorHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.Xs2aDevelopersTransactionalAccountFetcher;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionKeyWithInitDateFromFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@AgentCapabilities({CHECKING_ACCOUNTS})
public final class BpostAgent extends Xs2aDevelopersTransactionalAgent {

    @Inject
    public BpostAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, "https://api.psd2.bpostbank.be");
    }

    @Override
    protected Xs2aDevelopersAuthenticatorHelper constructXs2aAuthenticator(
            AgentComponentProvider componentProvider) {
        return new BPostAuthenticatorHelper(
                apiClient,
                persistentStorage,
                sessionStorage,
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
                        new BpostTransactionsFetcher(
                                apiClient,
                                agentComponentProvider.getLocalDateTimeSource(),
                                agentComponentProvider.getUser().isPresent()));

        return new TransactionalAccountRefreshController(
                metricRefreshController, updateController, accountFetcher, transactionFetcher);
    }
}
