package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.password;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.ErsteBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.credit.ErsteBankCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.transactional.ErsteBankAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.transactional.ErsteBankTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.password.authenticator.ErsteBankPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.session.ErsteBankSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class ErsteBankPasswordAgent extends NextGenerationAgent
        implements RefreshCreditCardAccountsExecutor {

    private final ErsteBankApiClient ersteBankApiClient;

    private final CreditCardRefreshController creditCardRefreshController;

    public ErsteBankPasswordAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.ersteBankApiClient = new ErsteBankApiClient(this.client, persistentStorage);

        this.creditCardRefreshController = constructCreditCardRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                new ErsteBankPasswordAuthenticator(ersteBankApiClient));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        new ErsteBankAccountFetcher(ersteBankApiClient),
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionPagePaginationController<>(
                                        new ErsteBankTransactionFetcher(ersteBankApiClient), 0))));
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
        ErsteBankCreditCardFetcher creditFetcher =
                new ErsteBankCreditCardFetcher(ersteBankApiClient);

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                creditFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(creditFetcher, 0)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new ErsteBankSessionHandler(ersteBankApiClient);
    }
}
