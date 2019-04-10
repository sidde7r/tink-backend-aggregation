package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.CrossKeyInvestmentsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.CrossKeyTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.CrossKeyTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.creditcard.CrossKeyCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.loan.CrossKeyLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.sessionhandler.CrossKeySessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
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

public abstract class CrossKeyAgent extends NextGenerationAgent {

    protected final CrossKeyApiClient apiClient;
    protected final CrossKeyConfiguration agentConfiguration;
    protected final CrossKeyPersistentStorage agentPersistentStorage;

    public CrossKeyAgent(
            CredentialsRequest request,
            AgentContext context,
            SignatureKeyPair signatureKeyPair,
            CrossKeyConfiguration agentConfiguration) {
        super(request, context, signatureKeyPair);
        this.agentConfiguration = agentConfiguration;
        this.apiClient = new CrossKeyApiClient(this.client, agentConfiguration);
        agentPersistentStorage = new CrossKeyPersistentStorage(this.persistentStorage);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {}

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        return Optional.of(
                new TransactionalAccountRefreshController(
                        this.metricRefreshController,
                        this.updateController,
                        new CrossKeyTransactionalAccountFetcher(this.apiClient, agentConfiguration),
                        new TransactionFetcherController<>(
                                this.transactionPaginationHelper,
                                new TransactionDatePaginationController<>(
                                        new CrossKeyTransactionFetcher(
                                                this.apiClient, agentConfiguration)))));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        CrossKeyCreditCardFetcher creditCardFetcher =
                new CrossKeyCreditCardFetcher(this.apiClient, agentPersistentStorage);
        return Optional.of(
                new CreditCardRefreshController(
                        this.metricRefreshController,
                        this.updateController,
                        creditCardFetcher,
                        new TransactionFetcherController<>(
                                this.transactionPaginationHelper,
                                new TransactionDatePaginationController<>(creditCardFetcher))));
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.of(
                new InvestmentRefreshController(
                        this.metricRefreshController,
                        this.updateController,
                        new CrossKeyInvestmentsFetcher(this.apiClient, agentConfiguration)));
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
        return Optional.of(
                new LoanRefreshController(
                        this.metricRefreshController,
                        this.updateController,
                        new CrossKeyLoanFetcher(this.apiClient, agentConfiguration)));
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
        return new CrossKeySessionHandler(this.apiClient);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
