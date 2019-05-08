package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza;

import java.util.Optional;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator.AvanzaBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.AvanzaInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.transactionalaccount.AvanzaTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.session.AvanzaSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
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
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.identitydata.countries.SeIdentityData;

public final class AvanzaAgent extends NextGenerationAgent implements RefreshIdentityDataExecutor {

    private final AvanzaAuthSessionStorage authSessionStorage;
    private final AvanzaApiClient apiClient;
    private final TemporaryStorage temporaryStorage;

    public AvanzaAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        this.authSessionStorage = new AvanzaAuthSessionStorage();
        this.apiClient = new AvanzaApiClient(client, authSessionStorage);
        this.temporaryStorage = new TemporaryStorage();
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {}

    @Override
    protected Authenticator constructAuthenticator() {
        return new BankIdAuthenticationController<>(
                supplementalRequester,
                new AvanzaBankIdAuthenticator(apiClient, authSessionStorage, temporaryStorage));
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        final AvanzaTransactionalAccountFetcher accountFetcher =
                new AvanzaTransactionalAccountFetcher(
                        apiClient, authSessionStorage, temporaryStorage);

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
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<InvestmentRefreshController> constructInvestmentRefreshController() {
        return Optional.of(
                new InvestmentRefreshController(
                        metricRefreshController,
                        updateController,
                        new AvanzaInvestmentFetcher(
                                apiClient, authSessionStorage, temporaryStorage)));
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
        return new AvanzaSessionHandler(apiClient, authSessionStorage);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return new FetchIdentityDataResponse(
                SeIdentityData.of("", credentials.getField(Field.Key.USERNAME)));
    }
}
