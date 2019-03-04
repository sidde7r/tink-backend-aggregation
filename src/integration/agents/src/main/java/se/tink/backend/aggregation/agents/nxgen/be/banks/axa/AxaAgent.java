package se.tink.backend.aggregation.agents.nxgen.be.banks.axa;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.AxaAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.AxaManualAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.AxaAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.AxaTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.session.AxaSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

import java.util.Optional;

public final class AxaAgent extends NextGenerationAgent {

    private final AxaApiClient apiClient;

    public AxaAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.apiClient = new AxaApiClient(client);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
        client.setUserAgent(AxaConstants.Request.USER_AGENT);
        client.setCipherSuites(AxaConstants.CIPHER_SUITES);
    }

    private AxaStorage makeStorage() {
        return new AxaStorage(sessionStorage, persistentStorage);
    }

    @Override
    protected Authenticator constructAuthenticator() {

        MultiFactorAuthenticator manualAuthenticator =
                new AxaManualAuthenticator(
                        catalog, apiClient, makeStorage(), supplementalInformationHelper);
        AutoAuthenticator autoAuthenticator = new AxaAutoAuthenticator(apiClient, makeStorage());

        return new AutoAuthenticationController(
                request, context, manualAuthenticator, autoAuthenticator);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        final AccountFetcher<TransactionalAccount> accountFetcher =
                new AxaAccountFetcher(apiClient, makeStorage());
        final TransactionFetcher<TransactionalAccount> transactionFetcher =
                new AxaTransactionFetcher();
        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        accountFetcher,
                        transactionFetcher));
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
        return new AxaSessionHandler(apiClient, makeStorage());
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
