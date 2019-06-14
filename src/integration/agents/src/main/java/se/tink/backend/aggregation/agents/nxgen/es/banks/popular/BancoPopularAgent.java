package se.tink.backend.aggregation.agents.nxgen.es.banks.popular;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.authenticator.BancoPopularAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.BancoPopularAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.BancoPopularInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.BancoPopularLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.BancoPopularTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.sessionhandler.BancoPopularSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BancoPopularAgent extends NextGenerationAgent implements RefreshIdentityDataExecutor {

    private final BancoPopularApiClient bankClient;
    private final BancoPopularPersistentStorage popularPersistentStorage;

    public BancoPopularAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        bankClient = new BancoPopularApiClient(client, sessionStorage);
        popularPersistentStorage = new BancoPopularPersistentStorage(persistentStorage);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                new BancoPopularAuthenticator(bankClient, popularPersistentStorage));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        new BancoPopularAccountFetcher(bankClient, popularPersistentStorage),
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionDatePaginationController<>(
                                        new BancoPopularTransactionFetcher(
                                                bankClient, popularPersistentStorage)))));
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.of(
                new InvestmentRefreshController(
                        metricRefreshController,
                        updateController,
                        new BancoPopularInvestmentFetcher(bankClient, popularPersistentStorage)));
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.of(
                new LoanRefreshController(
                        metricRefreshController,
                        updateController,
                        new BancoPopularLoanFetcher(bankClient, popularPersistentStorage)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new BancoPopularSessionHandler(bankClient);
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return bankClient.fetchIdentityData(popularPersistentStorage);
    }
}
