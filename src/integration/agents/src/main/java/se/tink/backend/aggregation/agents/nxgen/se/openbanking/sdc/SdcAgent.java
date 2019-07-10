package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.SdcConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.authenticator.SdcAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.authenticator.SdcAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.configuration.SdcConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.fetcher.transactionalaccount.SdcTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.fetcher.transactionalaccount.SdcTransactionalAccountTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.utils.BerlinGroupUtils;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class SdcAgent extends NextGenerationAgent {

    private final String clientName;
    private final SdcApiClient apiClient;

    public SdcAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new SdcApiClient(client, persistentStorage, request.getCredentials());
        clientName = request.getProvider().getPayload();
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        apiClient.setConfiguration(getClientConfiguration());

        client.setSslClientCertificate(
                BerlinGroupUtils.readFile(getClientConfiguration().getClientKeyStorePath()),
                getClientConfiguration().getClientKeyStorePassword());
    }

    protected SdcConfiguration getClientConfiguration() {
        return configuration
                .getIntegrations()
                .getClientConfiguration(
                        SdcConstants.INTEGRATION_NAME, clientName, SdcConfiguration.class)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    protected Authenticator constructAuthenticator() {

        final SdcAuthenticationController controller =
                new SdcAuthenticationController(
                        supplementalInformationHelper,
                        new SdcAuthenticator(apiClient, persistentStorage));

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);

        //        TODO https://github.com/tink-ab/tink-backend-integration-thirdparties/issues/189
        //        final OAuth2AuthenticationController controller =
        //                new OAuth2AuthenticationController(
        //                        persistentStorage,
        //                        supplementalInformationHelper,
        //                        new SdcAuthenticator(apiClient, persistentStorage));
        //
        //        return new AutoAuthenticationController(
        //                request,
        //                systemUpdater,
        //                new ThirdPartyAppAuthenticationController<>(
        //                        controller, supplementalInformationHelper),
        //                controller);
    }

    @Override
    protected Optional<TransactionalAccountRefreshController>
            constructTransactionalAccountRefreshController() {
        final SdcTransactionalAccountFetcher accountFetcher =
                new SdcTransactionalAccountFetcher(apiClient);

        final SdcTransactionalAccountTransactionFetcher transactionFetcher =
                new SdcTransactionalAccountTransactionFetcher(apiClient);

        return Optional.of(
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        accountFetcher,
                        new TransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionDatePaginationController<>(transactionFetcher))));
    }

    @Override
    public Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        return Optional.empty();
    }

    @Override
    protected Optional<LoanRefreshController> constructLoanRefreshController() {
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
