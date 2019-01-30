package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.BbvaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.BbvaCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.BbvaInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.loan.BbvaLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.BbvaAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.BbvaTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.session.BbvaSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials_requests.CredentialsRequest;

public class BbvaAgent extends NextGenerationAgent {

    private BbvaApiClient apiClient;

    public BbvaAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.apiClient = new BbvaApiClient(client);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
    }

    @Override
    protected Authenticator constructAuthenticator() {
        BbvaAuthenticator authenticator = new BbvaAuthenticator(apiClient, sessionStorage);

        return new PasswordAuthenticationController(authenticator);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        BbvaAccountFetcher transactionalAccountFetcher = new BbvaAccountFetcher(apiClient, sessionStorage);
        BbvaTransactionFetcher transactionFetcher = new BbvaTransactionFetcher(apiClient);

        return Optional.of(new TransactionalAccountRefreshController(metricRefreshController,
                updateController, transactionalAccountFetcher,
                new TransactionFetcherController<>(transactionPaginationHelper,
                        new TransactionPagePaginationController<>(transactionFetcher, 0))
                ));
    }
    @Override
    protected SessionHandler constructSessionHandler() {
        return new BbvaSessionHandler(apiClient);
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        BbvaCreditCardFetcher creditCardFetcher = new BbvaCreditCardFetcher(apiClient);

        return Optional.of(new CreditCardRefreshController(metricRefreshController, updateController,
                creditCardFetcher, creditCardFetcher));
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.of(new InvestmentRefreshController(metricRefreshController, updateController,
                new BbvaInvestmentFetcher(apiClient, sessionStorage)));
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.of(new LoanRefreshController(metricRefreshController, updateController,
                new BbvaLoanFetcher(apiClient)));
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
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
