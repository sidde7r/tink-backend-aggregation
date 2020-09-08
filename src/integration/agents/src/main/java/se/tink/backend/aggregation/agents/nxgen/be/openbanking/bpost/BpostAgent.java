package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bpost;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersTransactionalAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.Xs2aDevelopersTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.Xs2aDevelopersTransactionalAccountTransactionDateFromFetcher;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionKeyWithInitDateFromFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public final class BpostAgent extends Xs2aDevelopersTransactionalAgent {

    @Inject
    public BpostAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, "https://api.psd2.bpostbank.be");
    }

    @Override
    protected TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final AccountFetcher accountFetcher =
                new Xs2aDevelopersTransactionalAccountFetcher(apiClient, oauth2Authenticator);

        final TransactionFetcher<TransactionalAccount> transactionFetcher =
                new TransactionKeyWithInitDateFromFetcherController<>(
                        request,
                        new Xs2aDevelopersTransactionalAccountTransactionDateFromFetcher(
                                apiClient, oauth2Authenticator));

        return new TransactionalAccountRefreshController(
                metricRefreshController, updateController, accountFetcher, transactionFetcher);
    }
}
