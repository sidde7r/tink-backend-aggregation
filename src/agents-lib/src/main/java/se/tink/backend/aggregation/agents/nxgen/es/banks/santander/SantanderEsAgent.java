package se.tink.backend.aggregation.agents.nxgen.es.banks.santander;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.authenticator.SantanderEsAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.creditcards.CreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.SantanderEsInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.transactionalaccounts.SantanderEsAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.transactionalaccounts.SantanderEsTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.session.SantanderEsSessionHandler;
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
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials_requests.CredentialsRequest;

public class SantanderEsAgent extends NextGenerationAgent {
    private final SantanderEsApiClient apiClient;

    public SantanderEsAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.apiClient = new SantanderEsApiClient(client);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(new SantanderEsAuthenticator(apiClient, sessionStorage));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController> constructTransactionalAccountRefreshController() {
        return Optional.of(
                new TransactionalAccountRefreshController(metricRefreshController, updateController,
                        new SantanderEsAccountFetcher(sessionStorage),
                        new TransactionFetcherController<>(transactionPaginationHelper,
                                new TransactionKeyPaginationController<>(
                                        new SantanderEsTransactionFetcher(apiClient))))
        );
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        CreditCardFetcher creditcardFetcher = new CreditCardFetcher(apiClient, sessionStorage);

        return Optional.of(
                new CreditCardRefreshController(metricRefreshController, updateController,
                        creditcardFetcher, new TransactionFetcherController<>(transactionPaginationHelper,
                        new TransactionDatePaginationController<>(creditcardFetcher)))
        );
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        SantanderEsInvestmentFetcher investmentFetcher = new SantanderEsInvestmentFetcher(apiClient, sessionStorage);

        return Optional
                .of(new InvestmentRefreshController(metricRefreshController, updateController, investmentFetcher));
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
        return Optional.empty();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new SantanderEsSessionHandler(apiClient);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
