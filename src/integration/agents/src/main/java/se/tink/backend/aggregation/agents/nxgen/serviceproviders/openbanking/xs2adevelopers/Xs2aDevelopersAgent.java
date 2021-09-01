package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.Xs2aDevelopersTransactionDateFromFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.creditcard.Xs2aDevelopersCreditCardAccountFetcher;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionKeyWithInitDateFromFetcherController;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public abstract class Xs2aDevelopersAgent extends Xs2aDevelopersTransactionalAgent
        implements RefreshCreditCardAccountsExecutor {

    private final CreditCardRefreshController creditCardRefreshController;

    @Inject
    protected Xs2aDevelopersAgent(AgentComponentProvider componentProvider, String baseUrl) {
        super(componentProvider, baseUrl);
        this.creditCardRefreshController = constructCreditCardRefreshController(componentProvider);
    }

    protected CreditCardRefreshController constructCreditCardRefreshController(
            AgentComponentProvider agentComponentProvider) {
        final Xs2aDevelopersCreditCardAccountFetcher accountFetcher =
                new Xs2aDevelopersCreditCardAccountFetcher(apiClient, authenticatorHelper);

        final TransactionFetcher<CreditCardAccount> transactionFetcher =
                new TransactionKeyWithInitDateFromFetcherController<>(
                        request,
                        new Xs2aDevelopersTransactionDateFromFetcher<CreditCardAccount>(
                                apiClient,
                                agentComponentProvider.getLocalDateTimeSource(),
                                request.getUserAvailability().isUserPresent()));

        return new CreditCardRefreshController(
                metricRefreshController, updateController, accountFetcher, transactionFetcher);
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }
}
