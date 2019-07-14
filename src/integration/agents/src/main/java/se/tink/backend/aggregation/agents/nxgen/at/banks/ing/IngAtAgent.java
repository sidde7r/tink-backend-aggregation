package se.tink.backend.aggregation.agents.nxgen.at.banks.ing;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.authenticator.IngAtPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.fetcher.credit.IngAtCreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.fetcher.credit.IngAtCreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.fetcher.transactional.IngAtTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.fetcher.transactional.IngAtTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.session.IngAtSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class IngAtAgent extends NextGenerationAgent implements RefreshCreditCardAccountsExecutor {
    private final IngAtSessionStorage ingAtSessionStorage;
    private final IngAtApiClient apiClient;

    private final CreditCardRefreshController creditCardRefreshController;

    public IngAtAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        configureHttpClient(client);
        this.ingAtSessionStorage = new IngAtSessionStorage(sessionStorage);
        this.apiClient =
                new IngAtApiClient(this.client, request.getProvider(), this.ingAtSessionStorage);

        this.creditCardRefreshController = constructCreditCardRefreshController();
    }

    protected void configureHttpClient(TinkHttpClient client) {
        client.setUserAgent(IngAtConstants.Header.USER_AGENT);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                new IngAtPasswordAuthenticator(apiClient, ingAtSessionStorage));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        new IngAtTransactionalAccountFetcher(apiClient, ingAtSessionStorage),
                        new TransactionFetcherController<>(
                                this.transactionPaginationHelper,
                                new TransactionDatePaginationController<>(
                                        new IngAtTransactionFetcher(
                                                apiClient, ingAtSessionStorage)))));
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
                new IngAtCreditCardAccountFetcher(apiClient, ingAtSessionStorage),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(
                                new IngAtCreditCardTransactionFetcher(
                                        apiClient, ingAtSessionStorage))));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new IngAtSessionHandler(apiClient, ingAtSessionStorage);
    }
}
