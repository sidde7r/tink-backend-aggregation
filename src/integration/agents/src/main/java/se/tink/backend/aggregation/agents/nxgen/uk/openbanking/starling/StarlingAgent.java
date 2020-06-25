package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling;

import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.configuration.StarlingConfiguration;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.configuration.entity.ClientConfigurationEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.transfer.StarlingTransferExecutor;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.StarlingTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.StarlingTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transfer.StarlingTransferDestinationFetcher;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.ProductionAgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.SupplementalInformationProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.SupplementalInformationProviderImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.oauth.progressive.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.libraries.credentials.service.CredentialsRequest;

/** Starling documentation is available at https://api-sandbox.starlingbank.com/api/swagger.yaml */
public final class StarlingAgent extends SubsequentProgressiveGenerationAgent
        implements RefreshTransferDestinationExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {
    private StarlingApiClient apiClient;
    private final TransferDestinationRefreshController transferDestinationRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    private ClientConfigurationEntity aisConfiguration;
    private ClientConfigurationEntity pisConfiguration;
    private String redirectUrl;
    private OAuth2Authenticator authenticator;

    public StarlingAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(ProductionAgentComponentProvider.create(request, context, signatureKeyPair));

        final AgentConfiguration<StarlingConfiguration> agentConfiguration =
                getAgentConfigurationController()
                        .getAgentCommonConfiguration(StarlingConfiguration.class);
        final StarlingConfiguration starlingConfiguration =
                agentConfiguration.getClientConfiguration();

        aisConfiguration = starlingConfiguration.getAisConfiguration();
        pisConfiguration = starlingConfiguration.getPisConfiguration();
        redirectUrl = agentConfiguration.getRedirectUrl();
        authenticator =
                new StarlingOAut2Authenticator(
                        persistentStorage, client, aisConfiguration, redirectUrl);
        apiClient = new StarlingApiClient(client, authenticator);
        transferDestinationRefreshController = constructTransferDestinationRefreshController();
        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return transactionalAccountRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return transactionalAccountRefreshController.fetchCheckingTransactions();
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return transactionalAccountRefreshController.fetchSavingsAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return transactionalAccountRefreshController.fetchSavingsTransactions();
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new StarlingTransactionalAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(
                                new StarlingTransactionFetcher(apiClient))));
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }

    private TransferDestinationRefreshController constructTransferDestinationRefreshController() {
        return new TransferDestinationRefreshController(
                metricRefreshController, new StarlingTransferDestinationFetcher(apiClient));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        SupplementalInformationProvider supplementalInformationProvider =
                new SupplementalInformationProviderImpl(supplementalRequester, request);
        return Optional.of(
                new TransferController(
                        null,
                        new StarlingTransferExecutor(
                                apiClient,
                                pisConfiguration,
                                redirectUrl,
                                credentials,
                                strongAuthenticationState,
                                supplementalInformationProvider.getSupplementalInformationHelper()),
                        null,
                        null));
    }

    @Override
    public OAuth2Authenticator getAuthenticator() {
        return authenticator;
    }
}
