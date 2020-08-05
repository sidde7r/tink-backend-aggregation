package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.BerlinGroupTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.configuration.SamlinkConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.payment.SamlinkPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.transactionalaccount.SamlinkTransactionFetcher;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationFlow;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;

public class SamlinkAgent extends BerlinGroupAgent<SamlinkApiClient, SamlinkConfiguration> {
    private final QsealcSigner qsealcSigner;

    public SamlinkAgent(AgentComponentProvider componentProvider, QsealcSigner qsealcSigner) {
        super(componentProvider);

        this.qsealcSigner = qsealcSigner;
        this.apiClient = createApiClient();
        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    protected SamlinkApiClient createApiClient() {
        return new SamlinkApiClient(
                client,
                persistentStorage,
                qsealcSigner,
                getConfiguration().getProviderSpecificConfiguration(),
                getConfiguration().getRedirectUrl());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return OAuth2AuthenticationFlow.create(
                request,
                systemUpdater,
                persistentStorage,
                supplementalInformationHelper,
                new SamlinkAuthenticator(apiClient),
                credentials,
                strongAuthenticationState);
    }

    @Override
    protected Class<SamlinkConfiguration> getConfigurationClassDescription() {
        return SamlinkConfiguration.class;
    }

    @Override
    protected BerlinGroupTransactionFetcher getTransactionFetcher() {
        return new SamlinkTransactionFetcher(
                apiClient, getConfiguration().getProviderSpecificConfiguration());
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        SamlinkPaymentExecutor executor = new SamlinkPaymentExecutor(apiClient);
        return Optional.of(new PaymentController(executor, executor));
    }
}
