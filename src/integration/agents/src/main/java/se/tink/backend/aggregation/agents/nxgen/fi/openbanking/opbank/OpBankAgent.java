package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.OpBankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.configuration.OpBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.transactionalaccount.OpBankTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class OpBankAgent extends NextGenerationAgent {

    private final String clientName;
    private final OpBankApiClient apiClient;

    public OpBankAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new OpBankApiClient(client, persistentStorage);
        clientName = request.getProvider().getPayload();
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {
        client.setDebugProxy("https://localhost:9022");
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        apiClient.setConfiguration(getClientConfiguration());
    }

    protected OpBankConfiguration getClientConfiguration() {
        return new OpBankConfiguration(
                "wBt4qeh628xkOXSw0HdQ",
                "XjNz8DLZkeLinrUL4ZC7",
                "https://localhost:7357/api/v1/thirdparty/callback",
                "AGR0JMwdsJFhdiEM3xbF2wuXHqdWJG8A");
        /*
        return configuration
            .getIntegrations()
            .getClientConfiguration(
                OpBankConstants.INTEGRATION_NAME, clientName, OPBankConfiguration.class)
            .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
            */
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new OpBankAuthenticator(
                                apiClient, persistentStorage, getClientConfiguration()));

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        final OpBankTransactionalAccountFetcher accountFetcher =
                new OpBankTransactionalAccountFetcher(apiClient);

        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        accountFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionKeyPaginationController<>(accountFetcher))));
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
        return SessionHandler.alwaysFail();
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
