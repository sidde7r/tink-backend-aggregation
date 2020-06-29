package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank;

import com.google.inject.Inject;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Provider.AccessType;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.ClusterIds;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.ClusterSpecificCallbacks;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.DemobankAppToAppAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.DemobankDkNemIdReAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.DemobankNoBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.DemobankPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.DemobankRedirectAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount.DemobankTransactionalAccountFetcher;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.bankid.BankIdAuthenticationControllerNO;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class DemobankAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    protected final DemobankApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final String callbackUri;

    @Inject
    public DemobankAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.callbackUri = getCallbackUri();
        apiClient = new DemobankApiClient(client, sessionStorage, callbackUri);
        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
    }

    private String getCallbackUri() {
        String callbackUri = request.getCallbackUri();
        if (callbackUri == null || callbackUri.trim().length() == 0) {
            switch (context.getClusterId()) {
                case ClusterIds.OXFORD_STAGING:
                    callbackUri = ClusterSpecificCallbacks.OXFORD_STAGING_CALLBACK;
                    break;
                case ClusterIds.OXFORD_PREPROD:
                    callbackUri = ClusterSpecificCallbacks.OXFORD_PREPROD_CALLBACK;
                    break;
                default:
                    callbackUri = ClusterSpecificCallbacks.OXFORD_PROD_CALLBACK;
            }
        }
        return callbackUri;
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        final DemobankTransactionalAccountFetcher demobankTransactionalAccountFetcher =
                new DemobankTransactionalAccountFetcher(apiClient, sessionStorage);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                demobankTransactionalAccountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(
                                demobankTransactionalAccountFetcher)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        if (CredentialsTypes.MOBILE_BANKID.equals(provider.getCredentialsType())
                && "NO".equals(provider.getMarket())) {

            return new BankIdAuthenticationControllerNO(
                    supplementalRequester, new DemobankNoBankIdAuthenticator(apiClient));

        } else if (CredentialsTypes.THIRD_PARTY_APP.equals(provider.getCredentialsType())
                && AccessType.OTHER.equals(provider.getAccessType())
                && "DK".equals(provider.getMarket())) {

            String username = credentials.getField(Field.Key.USERNAME);
            String password = credentials.getField(Field.Key.PASSWORD);

            DemobankDkNemIdReAuthenticator demobankNemIdAuthenticator =
                    new DemobankDkNemIdReAuthenticator(
                            apiClient, client, persistentStorage, username, password);

            return new AutoAuthenticationController(
                    request,
                    systemUpdater,
                    new ThirdPartyAppAuthenticationController<>(
                            demobankNemIdAuthenticator, supplementalInformationHelper),
                    demobankNemIdAuthenticator);

        } else if (CredentialsTypes.PASSWORD.equals(provider.getCredentialsType())) {
            return new PasswordAuthenticationController(
                    new DemobankPasswordAuthenticator(apiClient));
        } else if (provider.getName().endsWith("-app-to-app")) {
            DemobankAppToAppAuthenticator authenticator =
                    new DemobankAppToAppAuthenticator(
                            apiClient,
                            credentials.getField("username"),
                            credentials.getField("password"),
                            getCallbackUri(),
                            strongAuthenticationState.getState());
            return new AutoAuthenticationController(
                    request,
                    systemUpdater,
                    new ThirdPartyAppAuthenticationController<>(
                            authenticator, supplementalInformationHelper),
                    authenticator);
        } else if (provider.getName().endsWith("-redirect")) {
            DemobankRedirectAuthenticator demobankRedirectAuthenticator =
                    new DemobankRedirectAuthenticator(
                            apiClient, persistentStorage, credentials, callbackUri);

            final OAuth2AuthenticationController controller =
                    new OAuth2AuthenticationController(
                            persistentStorage,
                            supplementalInformationHelper,
                            demobankRedirectAuthenticator,
                            credentials,
                            strongAuthenticationState);

            return new AutoAuthenticationController(
                    request,
                    context,
                    new ThirdPartyAppAuthenticationController<>(
                            controller, supplementalInformationHelper),
                    controller);
        } else {
            throw new IllegalStateException("Invalid provider configuration");
        }
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
}
