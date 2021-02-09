package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard;

import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.authenticator.EnterCardAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.fetcher.EnterCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.fetcher.EnterCardIdentityFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.fetcher.EnterCardTransactionFetcher;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class EnterCardAgent extends NextGenerationAgent
        implements RefreshIdentityDataExecutor, RefreshCreditCardAccountsExecutor {

    private final EnterCardApiClient apiClient;
    private final EnterCardConfiguration config;

    private final CreditCardRefreshController creditCardRefreshController;

    protected EnterCardAgent(
            CredentialsRequest request,
            AgentContext context,
            SignatureKeyPair signatureKeyPair,
            EnterCardConfiguration config) {
        super(request, context, signatureKeyPair);

        this.apiClient = new EnterCardApiClient(client, config);
        this.config = config;

        this.creditCardRefreshController = constructCreditCardRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new BankIdAuthenticationController<>(
                supplementalInformationController,
                new EnterCardAuthenticator(apiClient, config),
                persistentStorage,
                credentials);
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    private CreditCardRefreshController constructCreditCardRefreshController() {
        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                new EnterCardAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(
                                new EnterCardTransactionFetcher(apiClient), 1)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        EnterCardIdentityFetcher fetcher = new EnterCardIdentityFetcher(apiClient, credentials);
        return new FetchIdentityDataResponse(fetcher.fetchIdentityData());
    }
}
