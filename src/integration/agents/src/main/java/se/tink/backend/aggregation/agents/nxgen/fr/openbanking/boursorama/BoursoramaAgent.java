package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama;

import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.BoursoramaConstants.ZONE_ID;

import com.google.inject.Inject;
import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshBeneficiariesExecutor;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.authenticator.BoursoramaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client.BoursoramaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client.BoursoramaGetRequestSignFilter;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client.BoursoramaPostRequestSignFilter;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client.BoursoramaSignatureHeaderGenerator;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.configuration.BoursoramaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.IdentityEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.fetcher.BoursoramaTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.fetcher.transfer.BoursoramaTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.payment.BoursoramaPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.FrOpenBankingPaymentExecutor;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.identitydata.IdentityData;

@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
public class BoursoramaAgent extends NextGenerationAgent
        implements RefreshIdentityDataExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshBeneficiariesExecutor {

    private final BoursoramaApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final BoursoramaAuthenticator authenticator;
    private final TransferDestinationRefreshController transferDestinationRefreshController;

    @Inject
    public BoursoramaAgent(AgentComponentProvider componentProvider, QsealcSigner qsealcSigner) {

        super(componentProvider);

        AgentConfiguration<BoursoramaConfiguration> agentConfiguration = getAgentConfiguration();

        this.apiClient =
                constructApiClient(
                        qsealcSigner, agentConfiguration.getProviderSpecificConfiguration());
        this.authenticator =
                new BoursoramaAuthenticator(
                        this.apiClient, this.sessionStorage, agentConfiguration);
        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();

        this.transferDestinationRefreshController = constructTransferDestinationRefreshController();
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        client.setEidasProxy(configuration.getEidasProxy());
    }

    private AgentConfiguration<BoursoramaConfiguration> getAgentConfiguration() {
        AgentConfiguration<BoursoramaConfiguration> agentConfiguration =
                getAgentConfigurationController()
                        .getAgentConfiguration(BoursoramaConfiguration.class);
        BoursoramaConfiguration configuration =
                agentConfiguration.getProviderSpecificConfiguration();

        Objects.requireNonNull(configuration.getBaseUrl());
        Objects.requireNonNull(configuration.getClientId());
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

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        BoursoramaTransactionalAccountFetcher accountFetcher =
                new BoursoramaTransactionalAccountFetcher(apiClient, Clock.system(ZONE_ID));

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(accountFetcher)));
    }

    @Override
    protected Authenticator constructAuthenticator() {

        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        authenticator,
                        credentials,
                        strongAuthenticationState,
                        request);

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
        IdentityEntity identityEntity = apiClient.fetchIdentityData();

        return new FetchIdentityDataResponse(
                IdentityData.builder()
                        .addFirstNameElement(null)
                        .addSurnameElement(identityEntity.getConnectedPsu())
                        .setDateOfBirth(null)
                        .build());
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
    public Optional<PaymentController> constructPaymentController() {
        final AgentConfiguration<BoursoramaConfiguration> agentConfiguration =
                getAgentConfiguration();

        FrOpenBankingPaymentExecutor paymentExecutor =
                new FrOpenBankingPaymentExecutor(
                        new BoursoramaPaymentApiClient(
                                client, agentConfiguration.getProviderSpecificConfiguration()),
                        agentConfiguration.getRedirectUrl(),
                        sessionStorage,
                        strongAuthenticationState,
                        supplementalInformationHelper);

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
}
