package se.tink.backend.aggregation.agents.nxgen.be.openbanking.cph;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.Transactions.DEFAULT_AMOUNT_TO_FETCH;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.Transactions.DEFAULT_CONSECUTIVE_EMPTY_PAGES_LIMIT;

import com.google.inject.Inject;
import java.time.temporal.ChronoUnit;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersTransactionalAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.Xs2aDevelopersTransactionalAccountFetcher;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;

public final class CphAgent extends Xs2aDevelopersTransactionalAgent {

    @Inject
    public CphAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, "https://api.psd2.cph.be");
    }

    @Override
    protected TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final Xs2aDevelopersTransactionalAccountFetcher accountFetcher =
                new Xs2aDevelopersTransactionalAccountFetcher(apiClient, oauth2Authenticator);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(
                                accountFetcher,
                                DEFAULT_CONSECUTIVE_EMPTY_PAGES_LIMIT,
                                DEFAULT_AMOUNT_TO_FETCH,
                                ChronoUnit.DAYS)));
    }
}
