package se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.authenticator.SpareBank1Authenticator;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.configuration.SpareBank1Configuration;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.fetcher.transactionalaccount.SpareBank1TransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.session.SpareBank1SessionHandler;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class SpareBank1Agent extends NextGenerationAgent {
    private final SpareBank1ApiClient apiClient;

    public SpareBank1Agent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new SpareBank1ApiClient(client, sessionStorage, persistentStorage);
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        final SpareBank1Configuration sebConfiguration =
                configuration
                        .getIntegrations()
                        .getClientConfiguration(
                                SpareBank1Constants.Market.INTEGRATION_NAME,
                                request.getProvider().getPayload(),
                                SpareBank1Configuration.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "SpareBank1 configuration missing."));

        persistentStorage.put(
                SpareBank1Constants.StorageKeys.CLIENT_ID, sebConfiguration.getClientId());
        persistentStorage.put(
                SpareBank1Constants.StorageKeys.CLIENT_SECRET, sebConfiguration.getClientSecret());
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {}

    @Override
    protected Authenticator constructAuthenticator() {
        return new SpareBank1Authenticator(apiClient, sessionStorage, persistentStorage);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        SpareBank1TransactionalAccountFetcher accountFetcher =
                new SpareBank1TransactionalAccountFetcher(apiClient);
        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController, updateController, accountFetcher, accountFetcher));
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
        return new SpareBank1SessionHandler(apiClient);
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
