package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.authenticator.NordeaBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.NordeaBankTransferExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.NordeaExecutorHelper;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.NordeaPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.einvoice.NordeaApproveEInvoiceExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.creditcard.NordeaCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.creditcard.NordeaCreditCardTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.einvoice.NordeaEInvoiceFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.investment.NordeaInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.loan.NordeaLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount.NordeaTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount.NordeaTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transfer.NordeaTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.session.NordeaSESessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.index.TransactionIndexPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class NordeaSEAgent extends NextGenerationAgent {
    private final NordeaSEApiClient apiClient;

    public NordeaSEAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        apiClient = new NordeaSEApiClient(client, sessionStorage);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {}

    @Override
    protected Authenticator constructAuthenticator() {
        return new BankIdAuthenticationController<>(
                context, new NordeaBankIdAuthenticator(apiClient, sessionStorage));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        new NordeaTransactionalAccountFetcher(apiClient),
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionIndexPaginationController<>(
                                        new NordeaTransactionFetcher(apiClient)))));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        return Optional.of(
                new CreditCardRefreshController(
                        metricRefreshController,
                        updateController,
                        new NordeaCreditCardFetcher(apiClient),
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionPagePaginationController<>(
                                        new NordeaCreditCardTransactionsFetcher(apiClient),
                                        NordeaSEConstants.Fetcher.START_PAGE))));
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.of(
                new InvestmentRefreshController(
                        metricRefreshController,
                        updateController,
                        new NordeaInvestmentFetcher(apiClient)));
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.of(
                new LoanRefreshController(
                        metricRefreshController,
                        updateController,
                        new NordeaLoanFetcher(apiClient)));
    }

    @Override
    protected Optional<EInvoiceRefreshController> constructEInvoiceRefreshController() {
        return Optional.of(
                new EInvoiceRefreshController(
                        metricRefreshController, new NordeaEInvoiceFetcher(apiClient)));
    }

    @Override
    protected Optional<TransferDestinationRefreshController>
            constructTransferDestinationRefreshController() {
        return Optional.of(
                new TransferDestinationRefreshController(
                        metricRefreshController, new NordeaTransferDestinationFetcher(apiClient)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new NordeaSESessionHandler(apiClient);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        NordeaExecutorHelper executorHelper =
                new NordeaExecutorHelper(
                        context, catalog, supplementalInformationHelper, apiClient);

        return Optional.of(
                new TransferController(
                        new NordeaPaymentExecutor(apiClient, catalog, executorHelper),
                        new NordeaBankTransferExecutor(apiClient, catalog, executorHelper),
                        new NordeaApproveEInvoiceExecutor(apiClient, catalog, executorHelper),
                        null));
    }
}
