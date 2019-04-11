package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30;

import java.util.Optional;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.authenticator.NordeaCodesAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.creditcard.NordeaCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.investment.NordeaInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.loan.NordeaLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.transactionalaccount.NordeaTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.transactionalaccount.NordeaTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.session.NordeaSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.index.TransactionIndexPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class NordeaFiAgent extends NextGenerationAgent {

    private final NordeaFiApiClient apiClient;

    public NordeaFiAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.apiClient =
                new NordeaFiApiClient(
                        client, sessionStorage, credentials.getField(Field.Key.USERNAME));
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {}

    @Override
    protected Authenticator constructAuthenticator() {
        return new ThirdPartyAppAuthenticationController<>(
                new NordeaCodesAuthenticator(apiClient, sessionStorage),
                supplementalInformationHelper);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        NordeaTransactionalAccountFetcher accountFetcher =
                new NordeaTransactionalAccountFetcher(apiClient);

        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        accountFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionIndexPaginationController<>(
                                        new NordeaTransactionFetcher(apiClient)))));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {

        NordeaCreditCardFetcher creditCardFetcher = new NordeaCreditCardFetcher(apiClient);
        return Optional.of(
                new CreditCardRefreshController(
                        metricRefreshController,
                        updateController,
                        creditCardFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionIndexPaginationController<>(creditCardFetcher))));
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
        return Optional.empty();
    }

    @Override
    protected Optional<TransferDestinationRefreshController>
            constructTransferDestinationRefreshController() {
        return Optional.empty();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new NordeaSessionHandler(apiClient);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
