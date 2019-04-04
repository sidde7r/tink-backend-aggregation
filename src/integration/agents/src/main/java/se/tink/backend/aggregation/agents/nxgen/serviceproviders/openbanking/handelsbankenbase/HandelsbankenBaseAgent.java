package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.HandelsbankenBaseConstants.Configuration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.HandelsbankenBaseConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.authenticator.HandelsbankenBaseAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.configuration.HandelsbankenBaseConfiguration;
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

public abstract class HandelsbankenBaseAgent extends NextGenerationAgent {

    private final HandelsbankenBaseApiClient apiClient;

    public HandelsbankenBaseAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        apiClient = new HandelsbankenBaseApiClient(client, persistentStorage, sessionStorage);
    }

    protected abstract Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController();

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        HandelsbankenBaseConfiguration handelsBankenBaseConfiguration =
                configuration
                        .getIntegrations()
                        .getClientConfiguration(
                                Configuration.INTEGRATION_NAME,
                                Configuration.CLIENT_NAME,
                                HandelsbankenBaseConfiguration.class)
                        .orElseThrow(IllegalStateException::new);
        persistentStorage.put(StorageKeys.CLIENT_ID, handelsBankenBaseConfiguration.getClientId());
        persistentStorage.put(
                StorageKeys.TPP_REQUEST_ID, handelsBankenBaseConfiguration.getTppRequestId());
        persistentStorage.put(
                StorageKeys.TPP_TRANSACTION_ID,
                handelsBankenBaseConfiguration.getTppTransactionId());
        persistentStorage.put(
                StorageKeys.PSU_IP_ADDRESS, handelsBankenBaseConfiguration.getPsuIpAddress());
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {}

    @Override
    protected Authenticator constructAuthenticator() {
        return new HandelsbankenBaseAuthenticator(apiClient, sessionStorage);
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
