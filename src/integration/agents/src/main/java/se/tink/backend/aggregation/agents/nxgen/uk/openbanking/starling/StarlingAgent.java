package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling;

import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.configuration.StarlingConfiguration;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.configuration.entity.ClientConfigurationEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.transfer.StarlingTransferExecutor;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.StarlingTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.StarlingTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transfer.StarlingTransferDestinationFetcher;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.ProductionAgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.SupplementalInformationProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.SupplementalInformationProviderImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.oauth.EndpointSpecification;
import se.tink.backend.aggregation.nxgen.controllers.authentication.oauth.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.oauth.OAuth2AuthorizationSpecification;
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
    private final StarlingApiClient apiClient;
    private final TransferDestinationRefreshController transferDestinationRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    private ClientConfigurationEntity aisConfiguration;
    private ClientConfigurationEntity pisConfiguration;
    private String redirectUrl;
    private StatelessProgressiveAuthenticator authenticator;

    public StarlingAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(ProductionAgentComponentProvider.create(request, context, signatureKeyPair));
        apiClient = new StarlingApiClient(client, persistentStorage);

        transferDestinationRefreshController = constructTransferDestinationRefreshController();
        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();

        StarlingConfiguration starlingConfiguration =
                getAgentConfigurationController()
                        .getAgentConfiguration(StarlingConfiguration.class);

        aisConfiguration = starlingConfiguration.getAisConfiguration();
        pisConfiguration = starlingConfiguration.getPisConfiguration();
        redirectUrl = starlingConfiguration.getRedirectUrl();
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
    public StatelessProgressiveAuthenticator getAuthenticator() {
        if (authenticator == null) {
            authenticator = createAuthenticator();
        }
        return authenticator;
    }

    private StatelessProgressiveAuthenticator createAuthenticator() {
        final String[] scopes =
                new String[] {
                    "account-holder-type:read",
                    "customer:read",
                    "account-identifier:read",
                    "account:read",
                    "transaction:read",
                    "balance:read"
                };
        OAuth2AuthorizationSpecification authorizationSpecification =
                new OAuth2AuthorizationSpecification.Builder()
                        .withAuthenticationEndpoint(
                                new EndpointSpecification(StarlingConstants.Url.AUTH_STARLING)
                                        .withClientSpecificParameter(
                                                StarlingConstants.RequestKey.CLIENT_SECRET,
                                                aisConfiguration.getClientSecret()))
                        .withRedirectUrl(aisConfiguration.getRedirectUrl())
                        .withClientId(aisConfiguration.getClientId())
                        .withResponseTypeCode(
                                new EndpointSpecification(
                                        StarlingConstants.Url.GET_OAUTH2_TOKEN.toString()))
                        .withTokenRefreshEndpoint(
                                new EndpointSpecification(
                                                StarlingConstants.Url.GET_OAUTH2_TOKEN.toString())
                                        .withClientSpecificParameter(
                                                StarlingConstants.RequestKey.CLIENT_ID,
                                                aisConfiguration.getClientId())
                                        .withClientSpecificParameter(
                                                StarlingConstants.RequestKey.CLIENT_SECRET,
                                                aisConfiguration.getClientSecret()))
                        .withScopes(scopes)
                        .build();
        return new OAuth2Authenticator(authorizationSpecification, persistentStorage, client);
    }
}
