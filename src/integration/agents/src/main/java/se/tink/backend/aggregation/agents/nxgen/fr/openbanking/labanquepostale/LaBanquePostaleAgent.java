package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale;

import com.google.inject.Inject;
import java.util.Optional;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.authenticator.LaBanquePostaleAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.configuration.LaBanquePostaleConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.LaBanquePostalPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.LaBanquePostaleAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.LaBanquePostaleTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.converter.LaBanquePostaleAccountConverter;
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
import se.tink.libraries.mapper.PrioritizedValueExtractor;

@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
public final class LaBanquePostaleAgent
        extends BerlinGroupAgent<LaBanquePostaleApiClient, LaBanquePostaleConfiguration> {

    private final QsealcSigner qsealcSigner;

    @Inject
    public LaBanquePostaleAgent(
            AgentComponentProvider componentProvider, QsealcSigner qsealcSigner) {
        super(componentProvider);

        this.qsealcSigner = qsealcSigner;
        this.apiClient = createApiClient();
        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    protected LaBanquePostaleApiClient createApiClient() {
        return new LaBanquePostaleApiClient(
                client,
                sessionStorage,
                qsealcSigner,
                getConfiguration().getClientConfiguration(),
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
}
