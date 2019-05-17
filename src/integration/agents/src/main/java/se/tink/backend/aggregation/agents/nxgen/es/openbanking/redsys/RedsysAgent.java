package se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.RedsysConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.authenticator.AuthenticationWithConsentController;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.authenticator.RedsysAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.authenticator.RedsysConsentController;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.configuration.RedsysConfiguration;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.fetcher.transactionalaccount.RedsysTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
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

public final class RedsysAgent extends NextGenerationAgent {

    private final String clientName;
    private final RedsysApiClient apiClient;

    public RedsysAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new RedsysApiClient(client, persistentStorage);
        clientName = request.getProvider().getPayload();
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {}

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        apiClient.setConfiguration(getClientConfiguration());
    }

    protected RedsysConfiguration getClientConfiguration() {
        return configuration
                .getIntegrations()
                .getClientConfiguration(
                        RedsysConstants.INTEGRATION_NAME, clientName, RedsysConfiguration.class)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new RedsysAuthenticator(
                                apiClient, persistentStorage, getClientConfiguration()));
        final RedsysConsentController consenter =
                new RedsysConsentController(apiClient, persistentStorage);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new AuthenticationWithConsentController(
                        controller, consenter, supplementalInformationHelper),
                controller);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        final RedsysTransactionalAccountFetcher accountFetcher =
                new RedsysTransactionalAccountFetcher(apiClient);

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
