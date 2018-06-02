package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.BelfiusTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.BelfiusTransferExecutor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.BelfiusAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.credit.BelfiusCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.BelfiusTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.sessionhandler.BelfiusSessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.onetimecode.OneTimeActivationCodeAuthenticationController;
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
import se.tink.backend.aggregation.rpc.CredentialsRequest;

public class BelfiusAgent extends NextGenerationAgent {

    private final BelfiusApiClient apiClient;
    private final BelfiusSessionStorage belfiusSessionStorage;

    public BelfiusAgent(CredentialsRequest request, AgentContext context) {
        super(request, context);
        this.belfiusSessionStorage = new BelfiusSessionStorage(this.sessionStorage);
        this.apiClient = new BelfiusApiClient(this.client, belfiusSessionStorage);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
    }

    @Override
    protected Authenticator constructAuthenticator() {
        BelfiusAuthenticator authenticator = new BelfiusAuthenticator(this.apiClient, this.credentials,
                this.persistentStorage,
                this.supplementalInformationController,
                belfiusSessionStorage);

        return new AutoAuthenticationController(
                this.request,
                this.context,
                new OneTimeActivationCodeAuthenticationController(authenticator),
                authenticator);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        BelfiusTransactionalAccountFetcher transactionalAccountFetcher
                = new BelfiusTransactionalAccountFetcher(this.apiClient);
        return Optional.of(
                new TransactionalAccountRefreshController(
                        this.metricRefreshController,
                        this.updateController,
                        transactionalAccountFetcher,
                        new TransactionFetcherController<>(
                                this.transactionPaginationHelper,
                                new TransactionDatePaginationController<>(transactionalAccountFetcher))));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        BelfiusCreditCardFetcher accountFetcher = new BelfiusCreditCardFetcher(this.apiClient);
        return Optional.of(
                new CreditCardRefreshController(
                        this.metricRefreshController,
                        this.updateController,
                        accountFetcher,
                        new TransactionFetcherController<>(
                                this.transactionPaginationHelper,
                                new TransactionDatePaginationController<>(accountFetcher))));
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
    protected Optional<TransferDestinationRefreshController> constructTransferDestinationRefreshController() {
        return Optional.of(
                new TransferDestinationRefreshController(
                        metricRefreshController,
                        updateController,
                        new BelfiusTransferDestinationFetcher(apiClient)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new BelfiusSessionHandler(this.apiClient);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.of(
                new TransferController(
                        null,
                        new BelfiusTransferExecutor(
                                apiClient,
                                this.supplementalInformationController,
                                belfiusSessionStorage,
                                context.getCatalog()),
                        null,
                        null));
    }
}
