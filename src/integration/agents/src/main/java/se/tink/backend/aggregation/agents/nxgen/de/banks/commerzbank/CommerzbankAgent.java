package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.CommerzbankPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.account.CommerzbankAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.credit.CommerzbankCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.CommerzbankTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.session.CommerzbankSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class CommerzbankAgent extends NextGenerationAgent
        implements RefreshCreditCardAccountsExecutor {

    private final CommerzbankApiClient apiClient;
    private final CreditCardRefreshController creditCardRefreshController;

    public CommerzbankAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        apiClient = new CommerzbankApiClient(client);

        creditCardRefreshController = constructCreditCardRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                new CommerzbankPasswordAuthenticator(apiClient));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        new CommerzbankAccountFetcher(apiClient),
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionDatePaginationController<>(
                                        new CommerzbankTransactionFetcher(apiClient)))));
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
                new CommerzbankCreditCardFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(
                                new CommerzbankCreditCardFetcher(apiClient))));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new CommerzbankSessionHandler(apiClient);
    }
}
