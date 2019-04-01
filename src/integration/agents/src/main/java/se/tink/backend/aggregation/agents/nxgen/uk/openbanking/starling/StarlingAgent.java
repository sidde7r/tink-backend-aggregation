package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.authenticator.StarlingAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.configuration.StarlingConfiguration;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.configuration.entity.ClientConfigurationEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.transfer.StarlingTransferExecutor;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.StarlingTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.StarlingTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transfer.StarlingTransferDestinationFetcher;
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
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

import java.security.PrivateKey;
import java.util.Optional;

/** Starling documentation is available at https://api-sandbox.starlingbank.com/api/swagger.yaml */
public class StarlingAgent extends NextGenerationAgent {
    private final String clientName;
    private final StarlingApiClient apiClient;

    private ClientConfigurationEntity aisConfiguration;
    private ClientConfigurationEntity pisConfiguration;
    private String signingKeyUid;
    private PrivateKey signingKey;

    public StarlingAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        clientName = request.getProvider().getPayload();
        apiClient = new StarlingApiClient(client, persistentStorage);
    }

    @Override
    protected void configureHttpClient(TinkHttpClient client) {}

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        StarlingConfiguration starlingConfiguration =
                configuration
                        .getIntegrations()
                        .getClientConfiguration(
                                StarlingConstants.INTEGRATION_NAME,
                                clientName,
                                StarlingConfiguration.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                String.format(
                                                        "No Starling client configured for name: %s",
                                                        clientName)));

        aisConfiguration = starlingConfiguration.getAisConfiguration();
        pisConfiguration = starlingConfiguration.getPisConfiguration();
        signingKeyUid = starlingConfiguration.getKeyUid();
        signingKey = starlingConfiguration.getSigningKey();
    }

    @Override
    protected Authenticator constructAuthenticator() {

        OAuth2AuthenticationController oauthController =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new StarlingAuthenticator(apiClient, aisConfiguration));

        ThirdPartyAppAuthenticationController<String> thirdPartyController =
                new ThirdPartyAppAuthenticationController<>(
                        oauthController, supplementalInformationHelper);

        return new AutoAuthenticationController(
                request, systemUpdater, thirdPartyController, oauthController);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        new StarlingTransactionalAccountFetcher(apiClient),
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionDatePaginationController<>(
                                        new StarlingTransactionFetcher(apiClient)))));
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
        return Optional.of(
                new TransferDestinationRefreshController(
                        metricRefreshController,
                        new StarlingTransferDestinationFetcher(apiClient)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
//        return Optional.of(
//                new TransferController(
//                        null,
//                        new StarlingTransferExecutor(
//                                apiClient,
//                                pisConfiguration,
//                                signingKeyUid,
//                                signingKey,
//                                credentials,
//                                supplementalInformationHelper),
//                        null,
//                        null));
    }
}
