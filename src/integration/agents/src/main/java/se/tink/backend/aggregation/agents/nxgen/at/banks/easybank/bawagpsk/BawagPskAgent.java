package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.authenticator.BawagPskPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.fetcher.BawagPskTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.fetcher.creditcard.BawagPskCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.fetcher.investment.BawagPskInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.fetcher.loan.BawagPskLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.fetcher.transactional.BawagPskTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.session.BawagPskSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
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
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BawagPskAgent extends NextGenerationAgent {

    private final BawagPskApiClient apiClient;

    public BawagPskAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        configureHttpClient(client);
        this.apiClient =
                new BawagPskApiClient(
                        this.client, sessionStorage, persistentStorage, request.getProvider());
    }

    protected void configureHttpClient(TinkHttpClient client) {
        BawagPskUtils.trustIntermediateCertificate(client);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(new BawagPskPasswordAuthenticator(apiClient));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        final BawagPskTransactionFetcher<TransactionalAccount> transactionFetcher =
                new BawagPskTransactionFetcher<TransactionalAccount>(apiClient);
        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        new BawagPskTransactionalAccountFetcher(apiClient),
                        new TransactionFetcherController<>(
                                this.transactionPaginationHelper,
                                // TODO Alternatively, implement a custom pagination controller
                                // which keeps fetching
                                // transactions until AccountStatementItem/Position equals 1
                                // (signifying the earliest entry)
                                new TransactionDatePaginationController<>(transactionFetcher))));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        return Optional.of(
                new CreditCardRefreshController(
                        metricRefreshController,
                        updateController,
                        new BawagPskCreditCardFetcher(apiClient),
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionDatePaginationController<>(
                                        new BawagPskTransactionFetcher<>(apiClient)))));
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.of(
                new InvestmentRefreshController(
                        metricRefreshController,
                        updateController,
                        new BawagPskInvestmentFetcher(apiClient)));
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.of(
                new LoanRefreshController(
                        metricRefreshController,
                        updateController,
                        new BawagPskLoanFetcher(apiClient),
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionDatePaginationController<>(
                                        new BawagPskTransactionFetcher<>(apiClient)))));
    }

    @Override
    protected Optional<EInvoiceRefreshController> constructEInvoiceRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<TransferDestinationRefreshController>
            constructTransferDestinationRefreshController() {
        return Optional.empty();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new BawagPskSessionHandler(apiClient);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
