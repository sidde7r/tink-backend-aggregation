package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;
import se.tink.agent.sdk.utils.signer.qsealc.QsealcSigner;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.authenticator.NorwegianAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.authenticator.NorwegianOAuth2AuthenticatorController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.client.NorwegianApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.client.NorwegianSigningFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.fetcher.account.NorwegianAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.fetcher.account.NorwegianCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.fetcher.account.NorwegianTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.fetcher.account.identitydata.NorwegianIdentityDataFetcher;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.cryptography.hash.Hash;

public class NorwegianBaseAgent extends NextGenerationAgent
        implements RefreshSavingsAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshIdentityDataExecutor {

    private final AgentConfiguration<NorwegianConfiguration> agentConfiguration;
    private final NorwegianApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final RandomValueGenerator randomValueGenerator;
    private final QsealcSigner qsealcSigner;

    public NorwegianBaseAgent(
            AgentComponentProvider componentProvider,
            NorwegianMarketConfiguration marketConfiguration) {
        super(componentProvider);
        Objects.requireNonNull(request);
        Objects.requireNonNull(context);
        this.qsealcSigner = componentProvider.getQsealcSigner();
        this.randomValueGenerator = componentProvider.getRandomValueGenerator();
        this.agentConfiguration = getAgentConfiguration();
        this.apiClient =
                new NorwegianApiClient(
                        client,
                        sessionStorage,
                        persistentStorage,
                        agentConfiguration,
                        userIp,
                        marketConfiguration,
                        componentProvider);

        this.transactionalAccountRefreshController =
                getTransactionalAccountRefreshController(
                        componentProvider.getLocalDateTimeSource());
        this.creditCardRefreshController =
                getCreditCardRefreshController(componentProvider.getLocalDateTimeSource());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        NorwegianAuthenticator norwegianAuthenticator =
                new NorwegianAuthenticator(
                        apiClient,
                        sessionStorage,
                        agentConfiguration.getProviderSpecificConfiguration(),
                        credentials,
                        randomValueGenerator);

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
            AgentConfiguration<NorwegianConfiguration> agentConfiguration) {

        try {
            Optional<X509Certificate> x509Certificate =
                    CertificateUtils.getRootX509CertificateFromBase64EncodedString(
                            agentConfiguration.getQsealc());
            if (x509Certificate.isPresent()) {
                String qsealcThumbprint = Hash.sha1AsHex(x509Certificate.get().getEncoded());
                return new NorwegianSigningFilter(qsealcThumbprint, qsealcSigner);
            }
            throw new IllegalStateException("Invalid QSealc certificate");
        } catch (CertificateException e) {
            throw new IllegalStateException(
                    "Could not create norwegian signing filter due to certificate parsing errors",
                    e);
        }
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController(
            LocalDateTimeSource localDateTimeSource) {
        NorwegianAccountFetcher accountFetcher = new NorwegianAccountFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(
                                        new NorwegianTransactionFetcher<TransactionalAccount>(
                                                apiClient,
                                                persistentStorage,
                                                sessionStorage,
                                                localDateTimeSource))
                                .setLocalDateTimeSource(localDateTimeSource)
                                .setZoneId(ZoneId.of("UTC"))
                                .build()));
    }

    private CreditCardRefreshController getCreditCardRefreshController(
            LocalDateTimeSource localDateTimeSource) {
        NorwegianCardFetcher cardFetcher = new NorwegianCardFetcher(apiClient);

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                cardFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(
                                        new NorwegianTransactionFetcher<CreditCardAccount>(
                                                apiClient,
                                                persistentStorage,
                                                sessionStorage,
                                                localDateTimeSource))
                                .setLocalDateTimeSource(localDateTimeSource)
                                .setZoneId(ZoneId.of("UTC"))
                                .build()));
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration agentsServiceConfiguration) {
        super.setConfiguration(agentsServiceConfiguration);
        Objects.requireNonNull(agentsServiceConfiguration);
        client.setEidasProxy(agentsServiceConfiguration.getEidasProxy());
        client.addFilter(createHttpClientSigningFilter(agentConfiguration));
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

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return new FetchIdentityDataResponse(
                new NorwegianIdentityDataFetcher(apiClient).fetchIdentityData());
    }
}
