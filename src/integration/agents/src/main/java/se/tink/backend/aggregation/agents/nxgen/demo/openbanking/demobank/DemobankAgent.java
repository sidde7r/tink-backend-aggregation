package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Provider.AccessType;
import se.tink.backend.agents.rpc.Provider.AuthenticationFlow;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.ClusterIds;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.ClusterSpecificCallbacks;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.DemobankAppToAppAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.DemobankAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.DemobankDecoupledAppAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.DemobankMockDkNemIdReAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.DemobankMockNoBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.DemobankMultiRedirectAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.DemobankPasswordAndOtpAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.DemobankPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount.DemobankCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount.DemobankIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount.DemobankTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.filters.AuthenticationErrorFilter;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.bankid.BankIdAuthenticationControllerNO;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.NemIdCodeAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceInternalErrorFilter;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS, IDENTITY_DATA})
public final class DemobankAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshIdentityDataExecutor {
    protected final DemobankApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final String callbackUri;

    @Inject
    public DemobankAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.callbackUri = getCallbackUri();
        apiClient = new DemobankApiClient(client, sessionStorage, callbackUri);
        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
        creditCardRefreshController = constructCreditCardRefreshController();
        client.addFilter(new BankServiceInternalErrorFilter());
        client.addFilter(new AuthenticationErrorFilter());
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
                new DemobankTransactionalAccountFetcher(apiClient, sessionStorage, provider);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                demobankTransactionalAccountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(
                                demobankTransactionalAccountFetcher)));
    }

    private CreditCardRefreshController constructCreditCardRefreshController() {
        final DemobankCreditCardFetcher demobankCreditCardFetcher =
                new DemobankCreditCardFetcher(apiClient, provider);

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                demobankCreditCardFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(demobankCreditCardFetcher)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new DemobankSessionHandler(sessionStorage);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        if (CredentialsTypes.MOBILE_BANKID.equals(provider.getCredentialsType())
                && "NO".equals(provider.getMarket())) {
            return new BankIdAuthenticationControllerNO(
                    supplementalRequester,
                    new DemobankMockNoBankIdAuthenticator(apiClient),
                    catalog);
        } else if (CredentialsTypes.THIRD_PARTY_APP.equals(provider.getCredentialsType())
                && AccessType.OTHER.equals(provider.getAccessType())
                && "DK".equals(provider.getMarket())) {
            return constructMockNemIdAuthenticator();
        } else if (AccessType.OPEN_BANKING.equals(provider.getAccessType())
                && AuthenticationFlow.DECOUPLED.equals(provider.getAuthenticationFlow())) {
            return constructDecoupledAppAuthenticator();
        } else if (CredentialsTypes.PASSWORD.equals(provider.getCredentialsType())
                && AccessType.OPEN_BANKING.equals(provider.getAccessType())
                && AuthenticationFlow.EMBEDDED.equals(provider.getAuthenticationFlow())) {
            return constructPasswordAndOtpAuthenticator();
        } else if (CredentialsTypes.PASSWORD.equals(provider.getCredentialsType())) {
            return new PasswordAuthenticationController(
                    new DemobankPasswordAuthenticator(apiClient));
        } else if (provider.getName().endsWith("-app-to-app")) {
            return constructApptToAppAuthenticator();
        } else if (provider.getName().endsWith("-redirect")) {
            return constructRedirectAuthenticator();
        } else {
            throw new IllegalStateException("Invalid provider configuration");
        }
    }

    private Authenticator constructPasswordAndOtpAuthenticator() {
        DemobankAutoAuthenticator autoAuthenticator =
                new DemobankAutoAuthenticator(sessionStorage, apiClient);
        DemobankPasswordAndOtpAuthenticator authenticator =
                new DemobankPasswordAndOtpAuthenticator(
                        apiClient, supplementalInformationController);
        return new AutoAuthenticationController(request, context, authenticator, autoAuthenticator);
    }

    private Authenticator constructMockNemIdAuthenticator() {
        String username = credentials.getField(Field.Key.USERNAME);
        String password = credentials.getField(Field.Key.PASSWORD);

        DemobankMockDkNemIdReAuthenticator demobankNemIdAuthenticator =
                new DemobankMockDkNemIdReAuthenticator(
                        apiClient, client, persistentStorage, username, password);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new NemIdCodeAppAuthenticationController(
                        demobankNemIdAuthenticator, credentials, supplementalRequester, catalog),
                demobankNemIdAuthenticator);
    }

    private Authenticator constructDecoupledAppAuthenticator() {
        DemobankDecoupledAppAuthenticator demobankDecoupledAppAuthenticator =
                new DemobankDecoupledAppAuthenticator(apiClient, supplementalRequester);
        DemobankAutoAuthenticator autoAuthenticator =
                new DemobankAutoAuthenticator(sessionStorage, apiClient);
        return new AutoAuthenticationController(
                request, systemUpdater, demobankDecoupledAppAuthenticator, autoAuthenticator);
    }

    private Authenticator constructApptToAppAuthenticator() {
        DemobankAutoAuthenticator autoAuthenticator =
                new DemobankAutoAuthenticator(sessionStorage, apiClient);
        DemobankAppToAppAuthenticator authenticator =
                new DemobankAppToAppAuthenticator(
                        apiClient,
                        credentials,
                        getCallbackUri(),
                        strongAuthenticationState.getState());
        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        authenticator, supplementalInformationHelper),
                autoAuthenticator);
    }

    private Authenticator constructRedirectAuthenticator() {
        DemobankMultiRedirectAuthenticator multiRedirectAuthenticator =
                new DemobankMultiRedirectAuthenticator(
                        apiClient,
                        persistentStorage,
                        callbackUri,
                        supplementalInformationHelper,
                        request,
                        strongAuthenticationState);

        DemobankAutoAuthenticator autoAuthenticator =
                new DemobankAutoAuthenticator(sessionStorage, apiClient);

        return new AutoAuthenticationController(
                request, context, multiRedirectAuthenticator, autoAuthenticator);
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

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        final IdentityDataFetcher fetcher = new DemobankIdentityDataFetcher(apiClient);
        return new FetchIdentityDataResponse(fetcher.fetchIdentityData());
    }
}
