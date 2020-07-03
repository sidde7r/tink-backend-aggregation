package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.unicre;

import java.util.Collections;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsProgressiveBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.fetcher.creditcards.SibsCreditCardFetcher;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class UnicreAgent extends SibsProgressiveBaseAgent
        implements RefreshCreditCardAccountsExecutor {

    private final CreditCardRefreshController creditCardRefreshController;

    public UnicreAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration configuration) {
        super(request, context, configuration);
        SibsCreditCardFetcher fetcher = new SibsCreditCardFetcher(apiClient);
        creditCardRefreshController =
                new CreditCardRefreshController(
                        metricRefreshController, updateController, fetcher, fetcher);
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return new FetchAccountsResponse(Collections.emptyList());
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return new FetchTransactionsResponse(Collections.emptyMap());
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return new FetchAccountsResponse(Collections.emptyList());
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return new FetchTransactionsResponse(Collections.emptyMap());
    }
}
