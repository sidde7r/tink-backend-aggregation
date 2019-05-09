package se.tink.backend.aggregation.agents.nxgen.be.banks.ing;

import static se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants.Headers.USER_AGENT;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.IngAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.IngCardReaderAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.controller.IngCardReaderAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.executor.IngTransferExecutor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.executor.IngTransferHelper;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.creditcard.IngCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.IngTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.IngTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transferdestination.IngTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.filter.IngHttpFilter;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.session.IngSessionHandler;
import se.tink.backend.aggregation.annotations.ProgressiveAuth;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.BankTransferExecutor;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

@ProgressiveAuth
public class IngAgent extends NextGenerationAgent {
    private final IngApiClient apiClient;
    private final IngHelper ingHelper;
    private final IngTransferHelper ingTransferHelper;

    public IngAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        configureHttpClient(client);
        this.apiClient = new IngApiClient(client);
        this.ingHelper = new IngHelper(sessionStorage);
        this.ingTransferHelper = new IngTransferHelper(catalog);
    }

    protected void configureHttpClient(TinkHttpClient client) {
        client.setUserAgent(USER_AGENT);
        client.setFollowRedirects(false);
        client.addFilter(new IngHttpFilter());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new IngCardReaderAuthenticationController(
                        new IngCardReaderAuthenticator(apiClient, persistentStorage, ingHelper),
                        supplementalInformationFormer),
                new IngAutoAuthenticator(apiClient, persistentStorage, ingHelper));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        IngTransactionFetcher transactionFetcher =
                new IngTransactionFetcher(credentials, apiClient, ingHelper);

        TransactionPagePaginationController<TransactionalAccount>
                transactionPagePaginationController =
                        new TransactionPagePaginationController<>(
                                transactionFetcher, IngConstants.Fetcher.START_PAGE);

        TransactionFetcherController<TransactionalAccount> transactionFetcherController =
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        transactionPagePaginationController,
                        transactionFetcher);

        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        new IngTransactionalAccountFetcher(apiClient, ingHelper),
                        transactionFetcherController));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        IngCreditCardFetcher creditCardFetcher = new IngCreditCardFetcher(ingHelper);

        return Optional.of(
                new CreditCardRefreshController(
                        metricRefreshController,
                        updateController,
                        creditCardFetcher,
                        creditCardFetcher));
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
        return Optional.of(
                new TransferDestinationRefreshController(
                        metricRefreshController,
                        new IngTransferDestinationFetcher(apiClient, ingHelper)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new IngSessionHandler(apiClient, ingHelper);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        BankTransferExecutor bankTransferExecutor =
                new IngTransferExecutor(apiClient, persistentStorage, ingHelper, ingTransferHelper);

        return Optional.of(new TransferController(null, bankTransferExecutor, null, null));
    }
}
