package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.LIST_BENEFICIARIES;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.TRANSFERS;

import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;
import lombok.SneakyThrows;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshBeneficiariesExecutor;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.agentcapabilities.PisCapability;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.authenticator.BoursoramaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client.BoursoramaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client.BoursoramaGetRequestSignFilter;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client.BoursoramaPostRequestSignFilter;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client.BoursoramaResponseHandler;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client.BoursoramaSignatureHeaderGenerator;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.configuration.BoursoramaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.fetcher.BoursoramaAccountCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.fetcher.BoursoramaAccountTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.fetcher.BoursoramaHolderNamesExtractor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.fetcher.identity.BoursoramaIdentityFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.fetcher.transfer.BoursoramaTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.payment.BoursoramaPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.payment.BoursoramaPaymentDatePolicy;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.FrOpenBankingPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.FrOpenBankingRequestValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.validator.FrCreatePaymentRequestValidator;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.date.CountryDateHelper;

@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
@AgentCapabilities({CHECKING_ACCOUNTS, LIST_BENEFICIARIES, IDENTITY_DATA, TRANSFERS, CREDIT_CARDS})
@AgentPisCapability(
        capabilities = {
            PisCapability.SEPA_CREDIT_TRANSFER,
            PisCapability.SEPA_INSTANT_CREDIT_TRANSFER,
            PisCapability.PIS_FUTURE_DATE
        })
public final class BoursoramaAgent extends NextGenerationAgent
        implements RefreshIdentityDataExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshBeneficiariesExecutor,
                RefreshCreditCardAccountsExecutor {

    private final BoursoramaApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final BoursoramaAuthenticator authenticator;
    private final TransferDestinationRefreshController transferDestinationRefreshController;
    private final BoursoramaIdentityFetcher identityFetcher;
    private final CreditCardRefreshController creditCardRefreshController;
    private final LocalDateTimeSource localDateTimeSource;

    @Inject
    public BoursoramaAgent(AgentComponentProvider componentProvider, QsealcSigner qsealcSigner) {

        super(componentProvider);

        AgentConfiguration<BoursoramaConfiguration> agentConfiguration = getAgentConfiguration();
        this.localDateTimeSource = componentProvider.getLocalDateTimeSource();
        this.apiClient =
                constructApiClient(
                        qsealcSigner, agentConfiguration.getProviderSpecificConfiguration());
        this.authenticator =
                new BoursoramaAuthenticator(
                        this.apiClient, this.sessionStorage, agentConfiguration);
        BoursoramaHolderNamesExtractor boursoramaHolderNamesExtractor =
                new BoursoramaHolderNamesExtractor();
        this.transactionalAccountRefreshController =
                getTransactionalAccountRefreshController(
                        componentProvider.getLocalDateTimeSource(), boursoramaHolderNamesExtractor);

        this.transferDestinationRefreshController = constructTransferDestinationRefreshController();

        this.creditCardRefreshController =
                constructCreditCardRefreshController(
                        componentProvider.getLocalDateTimeSource(), boursoramaHolderNamesExtractor);

        this.identityFetcher = getIdentityFetcher();

        this.client.setResponseStatusHandler(new BoursoramaResponseHandler());
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
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        client.setEidasProxy(configuration.getEidasProxy());
    }

    @SneakyThrows
    private AgentConfiguration<BoursoramaConfiguration> getAgentConfiguration() {
        AgentConfiguration<BoursoramaConfiguration> agentConfiguration =
                getAgentConfigurationController()
                        .getAgentConfiguration(BoursoramaConfiguration.class);
        BoursoramaConfiguration configuration =
                agentConfiguration.getProviderSpecificConfiguration();

        Objects.requireNonNull(
                CertificateUtils.getOrganizationIdentifier(agentConfiguration.getQsealc()));
        Objects.requireNonNull(configuration.getQsealKeyUrl());
        Objects.requireNonNull(agentConfiguration.getRedirectUrl());

        return agentConfiguration;
    }

    private BoursoramaApiClient constructApiClient(
            QsealcSigner qsealcSigner, BoursoramaConfiguration agentConfiguration) {

        final BoursoramaSignatureHeaderGenerator signatureHeaderGenerator =
                new BoursoramaSignatureHeaderGenerator(
                        qsealcSigner, agentConfiguration.getQsealKeyUrl());
        final BoursoramaGetRequestSignFilter getRequestSignFilter =
                new BoursoramaGetRequestSignFilter(signatureHeaderGenerator);
        final BoursoramaPostRequestSignFilter postRequestSignFilter =
                new BoursoramaPostRequestSignFilter(signatureHeaderGenerator);

        client.addFilter(getRequestSignFilter);
        client.addFilter(postRequestSignFilter);

        return new BoursoramaApiClient(client, agentConfiguration, sessionStorage);
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController(
            LocalDateTimeSource localDateTimeSource,
            BoursoramaHolderNamesExtractor boursoramaHolderNamesExtractor) {
        BoursoramaAccountTransactionalAccountFetcher accountFetcher =
                new BoursoramaAccountTransactionalAccountFetcher(
                        apiClient, localDateTimeSource, boursoramaHolderNamesExtractor);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(accountFetcher)
                                .setLocalDateTimeSource(localDateTimeSource)
                                .build()));
    }

    private CreditCardRefreshController constructCreditCardRefreshController(
            LocalDateTimeSource localDateTimeSource,
            BoursoramaHolderNamesExtractor boursoramaHolderNamesExtractor) {
        BoursoramaAccountCreditCardFetcher boursoramaCreditCardFetcher =
                new BoursoramaAccountCreditCardFetcher(
                        apiClient, localDateTimeSource, boursoramaHolderNamesExtractor);

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                boursoramaCreditCardFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(
                                        boursoramaCreditCardFetcher)
                                .setLocalDateTimeSource(localDateTimeSource)
                                .build()));
    }

    private BoursoramaIdentityFetcher getIdentityFetcher() {
        return new BoursoramaIdentityFetcher(apiClient);
    }

    @Override
    protected Authenticator constructAuthenticator() {

        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        authenticator,
                        credentials,
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                context,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return identityFetcher.fetchIdentity();
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
    @SneakyThrows
    public Optional<PaymentController> constructPaymentController() {
        final AgentConfiguration<BoursoramaConfiguration> agentConfiguration =
                getAgentConfiguration();
        final String clientId =
                CertificateUtils.getOrganizationIdentifier(agentConfiguration.getQwac());

        FrOpenBankingPaymentExecutor paymentExecutor =
                new FrOpenBankingPaymentExecutor(
                        new BoursoramaPaymentApiClient(
                                client,
                                clientId,
                                new FrCreatePaymentRequestValidator(localDateTimeSource),
                                sessionStorage),
                        agentConfiguration.getRedirectUrl(),
                        sessionStorage,
                        strongAuthenticationState,
                        supplementalInformationHelper,
                        new BoursoramaPaymentDatePolicy(
                                getCountryDateHelper(), localDateTimeSource),
                        new FrOpenBankingRequestValidator(provider.getName()));

        return Optional.of(new PaymentController(paymentExecutor, paymentExecutor));
    }

    @Override
    public FetchTransferDestinationsResponse fetchBeneficiaries(List<Account> accounts) {
        return transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }

    private TransferDestinationRefreshController constructTransferDestinationRefreshController() {
        return new TransferDestinationRefreshController(
                metricRefreshController, new BoursoramaTransferDestinationFetcher(apiClient));
    }

    private CountryDateHelper getCountryDateHelper() {
        return new CountryDateHelper(
                BoursoramaConstants.DEFAULT_LOCALE,
                TimeZone.getTimeZone(BoursoramaConstants.DEFAULT_ZONE_ID));
    }
}
