package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.handelsbanken;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.handelsbanken.authenticator.HandelsbankenAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.handelsbanken.fetcher.transactionalaccount.HandelsbankenTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.handelsbanken.session.HandelsbankenSessionHandler;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.HandelsbankenBaseAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
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

public final class HandelsbankenAgent extends HandelsbankenBaseAgent {

    private final HandelsbankenApiClient apiClient;

    public HandelsbankenAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new HandelsbankenApiClient(client, persistentStorage, sessionStorage);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {}

    @Override
    protected Authenticator constructAuthenticator() {
        return new HandelsbankenAuthenticator(apiClient, sessionStorage);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        HandelsbankenTransactionalAccountFetcher accountFetcher =
                new HandelsbankenTransactionalAccountFetcher(apiClient);

        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        accountFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionDatePaginationController<>(accountFetcher))));
    }

    @Override
    public Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.empty();
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
        return new HandelsbankenSessionHandler(sessionStorage);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
