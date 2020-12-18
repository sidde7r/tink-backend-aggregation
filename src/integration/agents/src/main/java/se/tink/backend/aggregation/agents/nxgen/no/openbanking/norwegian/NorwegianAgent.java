package se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import java.security.cert.CertificateException;
import java.util.Objects;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.authenticator.NorwegianAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.authenticator.NorwegianOAuth2AuthenticatorController;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.client.NorwegianApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.client.NorwegianSigningFilter;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.account.NorwegianAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.account.NorwegianCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.account.NorwegianTransactionFetcher;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.QsealcSignerImpl;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

@AgentCapabilities({SAVINGS_ACCOUNTS, CREDIT_CARDS})
public final class NorwegianAgent extends NextGenerationAgent
        implements RefreshSavingsAccountsExecutor, RefreshCreditCardAccountsExecutor {

    private final AgentConfiguration<NorwegianConfiguration> agentConfiguration;
    private final NorwegianApiClient apiClient;

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;

    public NorwegianAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(request, context, agentsServiceConfiguration.getSignatureKeyPair());
        Objects.requireNonNull(request);
        Objects.requireNonNull(context);
        Objects.requireNonNull(agentsServiceConfiguration);
        this.agentConfiguration = getAgentConfiguration();

        client.setEidasProxy(agentsServiceConfiguration.getEidasProxy());
        client.addFilter(
                createHttpClientSigningFilter(agentsServiceConfiguration, agentConfiguration));

        this.apiClient =
                new NorwegianApiClient(
                        client, sessionStorage, persistentStorage, agentConfiguration, userIp);

        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        this.creditCardRefreshController = getCreditCardRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        NorwegianAuthenticator norwegianAuthenticator =
                new NorwegianAuthenticator(
                        apiClient,
                        sessionStorage,
                        agentConfiguration.getProviderSpecificConfiguration(),
                        credentials);

        OAuth2AuthenticationController oAuth2AuthenticationController =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        norwegianAuthenticator,
                        credentials,
                        strongAuthenticationState);

        NorwegianOAuth2AuthenticatorController norwegianOAuth2AuthenticatorController =
                new NorwegianOAuth2AuthenticatorController(
                        oAuth2AuthenticationController, norwegianAuthenticator);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        norwegianOAuth2AuthenticatorController, supplementalInformationHelper),
                norwegianOAuth2AuthenticatorController);
    }

    private AgentConfiguration<NorwegianConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController()
                .getAgentConfiguration(NorwegianConfiguration.class);
    }

    private NorwegianSigningFilter createHttpClientSigningFilter(
            AgentsServiceConfiguration serviceConfiguration,
            AgentConfiguration<NorwegianConfiguration> agentConfiguration) {

        try {
            String qsealcSerialNumberInHex =
                    CertificateUtils.getSerialNumber(agentConfiguration.getQsealc(), 16);
            String qsealcIssuerDN =
                    CertificateUtils.getCertificateIssuerDN(agentConfiguration.getQsealc());

            return new NorwegianSigningFilter(
                    qsealcSerialNumberInHex, qsealcIssuerDN, getQsealcSigner(serviceConfiguration));
        } catch (CertificateException e) {
            throw new IllegalStateException(
                    "Could not create norwegian signing filter due to certificate parsing errors");
        }
    }

    private QsealcSigner getQsealcSigner(AgentsServiceConfiguration serviceConfiguration) {
        return QsealcSignerImpl.build(
                serviceConfiguration.getEidasProxy().toInternalConfig(),
                QsealcAlg.EIDAS_RSA_SHA256,
                getEidasIdentity());
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        NorwegianAccountFetcher accountFetcher = new NorwegianAccountFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(
                                new NorwegianTransactionFetcher<>(
                                        apiClient, persistentStorage, sessionStorage))));
    }

    private CreditCardRefreshController getCreditCardRefreshController() {
        NorwegianCardFetcher cardFetcher = new NorwegianCardFetcher(apiClient);

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                cardFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(
                                new NorwegianTransactionFetcher<>(
                                        apiClient, persistentStorage, sessionStorage))));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
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
    public FetchAccountsResponse fetchSavingsAccounts() {
        return transactionalAccountRefreshController.fetchSavingsAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return transactionalAccountRefreshController.fetchSavingsTransactions();
    }
}
