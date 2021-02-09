package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank;

import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.Provider.AuthenticationFlow;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.SwedbankDecoupledAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.SwedbankPaymentAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.SwedbankRedirectAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.configuration.SwedbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.SwedbankBankIdSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.SwedbankPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.SwedbankPaymentSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.SwedbankTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.SwedbankTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transferdestinations.SwedbankTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.filter.SwedbankConsentLimitFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.filter.SwedbankMethodNotAllowedFilter;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.signing.multifactor.bankid.BankIdSigningController;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceInternalErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.ServiceUnavailableBankServiceErrorFilter;

public abstract class SwedbankBaseAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshTransferDestinationExecutor {

    private final SwedbankApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final TransferDestinationRefreshController transferDestinationRefreshController;

    public SwedbankBaseAgent(AgentComponentProvider componentProvider, QsealcSigner qsealcSigner) {
        super(componentProvider);
        client.addFilter(new SwedbankConsentLimitFilter());
        client.addFilter(new SwedbankMethodNotAllowedFilter());
        client.addFilter(new BankServiceInternalErrorFilter());
        client.addFilter(new ServiceUnavailableBankServiceErrorFilter());
        apiClient =
                new SwedbankApiClient(
                        client,
                        persistentStorage,
                        getAgentConfiguration(),
                        qsealcSigner,
                        componentProvider.getCredentialsRequest());

        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        transferDestinationRefreshController = constructTransferDestinationController();
    }

    private TransferDestinationRefreshController constructTransferDestinationController() {
        return new TransferDestinationRefreshController(
                metricRefreshController, new SwedbankTransferDestinationFetcher());
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration agentsServiceConfiguration) {
        super.setConfiguration(agentsServiceConfiguration);
        client.setEidasProxy(agentsServiceConfiguration.getEidasProxy());
    }

    private AgentConfiguration<SwedbankConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController().getAgentConfiguration(SwedbankConfiguration.class);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        AuthenticationFlow authenticationFlow = request.getProvider().getAuthenticationFlow();

        if (AuthenticationFlow.DECOUPLED.equals(authenticationFlow)) {
            return constructDecoupledAppAuthenticator();
        }

        if (AuthenticationFlow.REDIRECT.equals(authenticationFlow)) {
            return constructRedirectAuthenticator();
        }

        throw new NotImplementedException(
                String.format(
                        "Support for %s authentication flow has not been implemented.",
                        authenticationFlow));
    }

    private Authenticator constructDecoupledAppAuthenticator() {
        BankIdAuthenticationController<String> bankIdAuthenticationController =
                new BankIdAuthenticationController<>(
                        supplementalInformationController,
                        new SwedbankDecoupledAuthenticator(apiClient, isSwedbank()),
                        persistentStorage,
                        credentials);

        return new AutoAuthenticationController(
                request, context, bankIdAuthenticationController, bankIdAuthenticationController);
    }

    private Authenticator constructRedirectAuthenticator() {

        OAuth2AuthenticationController oAuth2AuthenticationController =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new SwedbankRedirectAuthenticator(apiClient, persistentStorage),
                        credentials,
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                context,
                new ThirdPartyAppAuthenticationController<>(
                        oAuth2AuthenticationController, supplementalInformationHelper),
                oAuth2AuthenticationController);
    }

    private boolean isSwedbank() {
        return SwedbankConstants.SWEDBANK_OB_PROVIDER_NAME.equals(credentials.getProviderName());
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
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

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new SwedbankTransactionalAccountFetcher(
                        apiClient, persistentStorage, sessionStorage, transactionPaginationHelper),
                new SwedbankTransactionFetcher(apiClient, sessionStorage));
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        final SwedbankPaymentSigner swedbankPaymentSigner = getSwedbankPaymentSigner();
        final SwedbankPaymentExecutor swedbankPaymentExecutor =
                new SwedbankPaymentExecutor(apiClient, swedbankPaymentSigner);

        return Optional.of(new PaymentController(swedbankPaymentExecutor, swedbankPaymentExecutor));
    }

    private SwedbankPaymentSigner getSwedbankPaymentSigner() {
        final SwedbankBankIdSigner swedbankBankIdSigner = new SwedbankBankIdSigner(apiClient);
        return new SwedbankPaymentSigner(
                apiClient,
                swedbankBankIdSigner,
                strongAuthenticationState,
                new BankIdSigningController<>(supplementalRequester, swedbankBankIdSigner),
                new SwedbankPaymentAuthenticator(supplementalInformationHelper));
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }
}
