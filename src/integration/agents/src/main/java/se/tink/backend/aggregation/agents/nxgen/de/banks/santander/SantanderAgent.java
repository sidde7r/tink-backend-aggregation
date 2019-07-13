package se.tink.backend.aggregation.agents.nxgen.de.banks.santander;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.authenticator.SantanderPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.credit.SantanderCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.SantanderAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.SantanderTransactionFetcher;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SantanderAgent extends NextGenerationAgent
        implements RefreshCreditCardAccountsExecutor {

    private final SantanderApiClient santanderApiClient;

    private final CreditCardRefreshController creditCardRefreshController;

    public SantanderAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        santanderApiClient = new SantanderApiClient(this.client, sessionStorage);

        creditCardRefreshController = constructCreditCardRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                new SantanderPasswordAuthenticator(santanderApiClient));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        new SantanderAccountFetcher(santanderApiClient),
                        new TransactionFetcherController<>(
                                this.transactionPaginationHelper,
                                new TransactionDatePaginationController<>(
                                        new SantanderTransactionFetcher(santanderApiClient)))));
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
                new SantanderCreditCardFetcher(santanderApiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new SantanderCreditCardFetcher(santanderApiClient))));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new SantanderSessionHandler(santanderApiClient);
    }
}
