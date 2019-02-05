package se.tink.backend.aggregation.agents.nxgen.es.banks.popular;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.authenticator.BancoPopularAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.BancoPopularAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.BancoPopularInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.BancoPopularLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.BancoPopularTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.sessionhandler.BancoPopularSessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;

public class BancoPopularAgent extends NextGenerationAgent {

    private final BancoPopularApiClient bankClient;
    private final BancoPopularPersistentStorage popularPersistentStorage;

    public BancoPopularAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        bankClient = new BancoPopularApiClient(client, sessionStorage);
        popularPersistentStorage = new BancoPopularPersistentStorage(persistentStorage);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                new BancoPopularAuthenticator(bankClient, popularPersistentStorage)
        );
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        return Optional.of(new TransactionalAccountRefreshController(metricRefreshController, updateController,
                new BancoPopularAccountFetcher(bankClient, popularPersistentStorage),
                new TransactionFetcherController<>(transactionPaginationHelper,
                        new TransactionDatePaginationController<>(
                                new BancoPopularTransactionFetcher(bankClient, popularPersistentStorage)))));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.of(new InvestmentRefreshController(metricRefreshController, updateController,
                new BancoPopularInvestmentFetcher(bankClient, popularPersistentStorage)));
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.of(new LoanRefreshController(metricRefreshController, updateController,
                new BancoPopularLoanFetcher(bankClient, popularPersistentStorage)));
    }

    @Override
    protected Optional<EInvoiceRefreshController> constructEInvoiceRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<TransferDestinationRefreshController> constructTransferDestinationRefreshController() {
        return Optional.empty();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new BancoPopularSessionHandler(bankClient);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
