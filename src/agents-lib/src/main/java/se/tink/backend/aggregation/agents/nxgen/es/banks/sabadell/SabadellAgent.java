package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.authenticator.SabadellAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.authenticator.entities.InitiateSessionRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.SabadellCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.SabadellCreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.SabadellInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.loans.SabadellLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.SabadellAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.SabadellTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.session.SabadellSessionHandler;
import se.tink.backend.aggregation.agents.utils.encoding.messagebodywriter.NoEscapeOfBackslashMessageBodyWriter;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials_requests.CredentialsRequest;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;

public class SabadellAgent extends NextGenerationAgent {
    private final SabadellApiClient apiClient;

    public SabadellAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.apiClient = new SabadellApiClient(client);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
        client.addMessageWriter(new NoEscapeOfBackslashMessageBodyWriter(InitiateSessionRequestEntity.class));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(new SabadellAuthenticator(apiClient));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        return Optional.of(
                new TransactionalAccountRefreshController(metricRefreshController, updateController,
                        new SabadellAccountFetcher(apiClient),
                        new TransactionFetcherController<>(transactionPaginationHelper,
                                new TransactionKeyPaginationController<>(
                                        new SabadellTransactionFetcher(apiClient))))
        );
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        return Optional.of(
                new CreditCardRefreshController(metricRefreshController, updateController,
                        new SabadellCreditCardFetcher(apiClient),
                        new TransactionFetcherController<>(transactionPaginationHelper,
                                new TransactionPagePaginationController<>(
                                        new SabadellCreditCardTransactionFetcher(apiClient), 1)))
        );
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.of(
                new InvestmentRefreshController(metricRefreshController, updateController,
                        new SabadellInvestmentFetcher(apiClient))
        );
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.of(
                new LoanRefreshController(metricRefreshController, updateController,
                        new SabadellLoanFetcher(apiClient))
        );
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
        return new SabadellSessionHandler(apiClient);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }

}
