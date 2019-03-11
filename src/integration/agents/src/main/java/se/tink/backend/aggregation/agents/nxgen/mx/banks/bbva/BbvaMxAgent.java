package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.BbvaMxAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.credit.BbvaMxCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.loan.BbvaMxLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.transactional.BbvaMxTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
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

public class BbvaMxAgent extends NextGenerationAgent {

    private final BbvaMxApiClient bbvaApiClient;

    public BbvaMxAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.bbvaApiClient = new BbvaMxApiClient(client, persistentStorage);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
    }

    @Override
    protected Authenticator constructAuthenticator() {
        BbvaMxAuthenticator bbvaAuthenticator =
                new BbvaMxAuthenticator(bbvaApiClient, persistentStorage);
        return new AutoAuthenticationController(
                request, context, bbvaAuthenticator, bbvaAuthenticator);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        BbvaMxTransactionalAccountFetcher transactionalAccountFetcher =
                new BbvaMxTransactionalAccountFetcher(bbvaApiClient, persistentStorage);

        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        transactionalAccountFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionDatePaginationController<>(
                                        transactionalAccountFetcher),
                                transactionalAccountFetcher)));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        BbvaMxCreditCardFetcher creditCardFetcher = new BbvaMxCreditCardFetcher(bbvaApiClient);

        return Optional.of(
                new CreditCardRefreshController(
                        metricRefreshController,
                        updateController,
                        creditCardFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionDatePaginationController<>(
                                        creditCardFetcher))));

    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.of(
                new LoanRefreshController(
                        metricRefreshController,
                        updateController,
                        new BbvaMxLoanFetcher(bbvaApiClient)));
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
        return new BbvaMxSessionHandler(bbvaApiClient);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
