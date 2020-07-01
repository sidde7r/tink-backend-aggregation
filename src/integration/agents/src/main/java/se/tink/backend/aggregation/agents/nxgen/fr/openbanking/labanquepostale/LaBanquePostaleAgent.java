package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale;

import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.authenticator.LaBanquePostaleAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.configuration.LaBanquePostaleConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.LaBanquePostalPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.identity.LaBanquePostaleIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.LaBanquePostaleAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.LaBanquePostaleTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.converter.LaBanquePostaleAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transfer.LaBanquePostaleTransferDestinationFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.BerlinGroupAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.BerlinGroupTransactionFetcher;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transfer.TransferDestinationRefreshController;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
public final class LaBanquePostaleAgent
        extends BerlinGroupAgent<LaBanquePostaleApiClient, LaBanquePostaleConfiguration>
        implements RefreshIdentityDataExecutor, RefreshTransferDestinationExecutor {

    private final QsealcSigner qsealcSigner;
    private final LaBanquePostaleIdentityDataFetcher identityDataFetcher;
    private final TransferDestinationRefreshController transferDestinationRefreshController;

    @Inject
    public LaBanquePostaleAgent(
            AgentComponentProvider componentProvider, QsealcSigner qsealcSigner) {
        super(componentProvider);

        this.qsealcSigner = qsealcSigner;
        this.apiClient = createApiClient();
        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        this.identityDataFetcher = new LaBanquePostaleIdentityDataFetcher(this.apiClient);
        this.transferDestinationRefreshController = constructTransferDestinationRefreshController();
    }

    @Override
    protected LaBanquePostaleApiClient createApiClient() {
        return new LaBanquePostaleApiClient(
                client,
                sessionStorage,
                qsealcSigner,
                getConfiguration().getProviderSpecificConfiguration(),
                getConfiguration().getRedirectUrl());
    }

    @Override
    protected Class<LaBanquePostaleConfiguration> getConfigurationClassDescription() {
        return LaBanquePostaleConfiguration.class;
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController oAuth2Authenticator =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new LaBanquePostaleAuthenticator(apiClient, sessionStorage),
                        credentials,
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                context,
                new ThirdPartyAppAuthenticationController<>(
                        oAuth2Authenticator, supplementalInformationHelper),
                oAuth2Authenticator);
    }

    @Override
    protected BerlinGroupAccountFetcher getAccountFetcher() {
        final PrioritizedValueExtractor prioritizedValueExtractor = new PrioritizedValueExtractor();
        final LaBanquePostaleAccountConverter accountConverter =
                new LaBanquePostaleAccountConverter(prioritizedValueExtractor);
        return new LaBanquePostaleAccountFetcher(apiClient, accountConverter);
    }

    @Override
    protected BerlinGroupTransactionFetcher getTransactionFetcher() {
        return new LaBanquePostaleTransactionFetcher(apiClient);
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        return Optional.of(new PaymentController(new LaBanquePostalPaymentExecutor(apiClient)));
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return identityDataFetcher.response();
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return transferDestinationRefreshController.fetchTransferDestinations(accounts);
    }

    private TransferDestinationRefreshController constructTransferDestinationRefreshController() {
        return new TransferDestinationRefreshController(
                metricRefreshController, new LaBanquePostaleTransferDestinationFetcher(apiClient));
    }
}
