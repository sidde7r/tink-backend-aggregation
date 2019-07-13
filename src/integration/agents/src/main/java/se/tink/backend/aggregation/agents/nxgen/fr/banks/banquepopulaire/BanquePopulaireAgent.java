package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.BanquePopulaireAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.fetcher.creditcard.BanquePopulaireCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.fetcher.loan.BanquePopulaireLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.fetcher.transactionalaccounts.BanquePopulaireTransactionalAccountsFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.session.BanquePopulaireSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BanquePopulaireAgent extends NextGenerationAgent
        implements RefreshLoanAccountsExecutor, RefreshCreditCardAccountsExecutor {
    private BanquePopulaireApiClient apiClient;
    private final LoanRefreshController loanRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;

    public BanquePopulaireAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        configureHttpClient(client);

        apiClient =
                new BanquePopulaireApiClient(
                        client, sessionStorage, request.getProvider().getPayload());

        loanRefreshController =
                new LoanRefreshController(
                        metricRefreshController,
                        updateController,
                        new BanquePopulaireLoanFetcher(apiClient));

        creditCardRefreshController = constructCreditCardRefreshController();
    }

    protected void configureHttpClient(TinkHttpClient client) {
        client.addRedirectHandler(new BanquePopulaireRedirectHandler(sessionStorage));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        BanquePopulaireAuthenticator authenticator =
                new BanquePopulaireAuthenticator(apiClient, sessionStorage);

        return new PasswordAuthenticationController(authenticator);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        BanquePopulaireTransactionalAccountsFetcher transactionalAccountFetcher =
                new BanquePopulaireTransactionalAccountsFetcher(apiClient);

        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        transactionalAccountFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionKeyPaginationController<>(
                                        transactionalAccountFetcher))));
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
        BanquePopulaireCreditCardFetcher creditCardFetcher =
                new BanquePopulaireCreditCardFetcher(apiClient);

        CreditCardRefreshController creditCardController =
                new CreditCardRefreshController(
                        metricRefreshController,
                        updateController,
                        creditCardFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionKeyPaginationController<>(creditCardFetcher)));

        return creditCardController;
    }

    @Override
    public FetchLoanAccountsResponse fetchLoanAccounts() {
        return loanRefreshController.fetchLoanAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchLoanTransactions() {
        return loanRefreshController.fetchLoanTransactions();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new BanquePopulaireSessionHandler(apiClient, sessionStorage);
    }
}
