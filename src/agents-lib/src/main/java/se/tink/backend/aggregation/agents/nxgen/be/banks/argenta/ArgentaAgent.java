package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.ArgentaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.fetcher.transactional.ArgentaTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.fetcher.transactional.ArgentaTransactionalTransactionFetcher;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.common.config.SignatureKeyPair;
import static org.apache.http.client.config.CookieSpecs.IGNORE_COOKIES;

public class ArgentaAgent extends NextGenerationAgent {

    private final ArgentaApiClient apiClient;

    public ArgentaAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.apiClient = new ArgentaApiClient(this.client, new ArgentaSessionStorage(this.sessionStorage));
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
        // Argenta tries to set "out of domain cookies", to avoid a warning for each request just
        // ignore cookies.
        client.setCookieSpec(IGNORE_COOKIES);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        ArgentaPersistentStorage argentaPersistentStorage =
                new ArgentaPersistentStorage(this.persistentStorage);
        ArgentaAuthenticator argentaAuthenticator =
                new ArgentaAuthenticator(
                        argentaPersistentStorage,
                        apiClient,
                        supplementalInformationController,
                        catalog,
                        credentials);

        return new AutoAuthenticationController(
                request, context, argentaAuthenticator, argentaAuthenticator);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        String deviceId = new ArgentaPersistentStorage(this.persistentStorage).getDeviceId();
        ArgentaTransactionalAccountFetcher transactionalAccountFetcher =
                new ArgentaTransactionalAccountFetcher(apiClient, deviceId);
        ArgentaTransactionalTransactionFetcher argentaTransactionalTransactionFetcher =
                new ArgentaTransactionalTransactionFetcher(apiClient, deviceId);

        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        transactionalAccountFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                argentaTransactionalTransactionFetcher)));
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
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
        return new ArgentaSessionHandler(
                apiClient, new ArgentaPersistentStorage(this.persistentStorage));
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
