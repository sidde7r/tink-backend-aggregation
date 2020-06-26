package se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank;

import com.google.inject.Inject;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.authenticator.ErstebankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.configuration.ErstebankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.executor.payment.ErstebankPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.fetcher.transactionalaccount.ErstebankTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.BerlinGroupTransactionFetcher;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationFlow;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;

public final class ErstebankAgent
        extends BerlinGroupAgent<ErstebankApiClient, ErstebankConfiguration> {

    @Inject
    public ErstebankAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);

        this.apiClient = createApiClient();
        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    protected ErstebankApiClient createApiClient() {
        return new ErstebankApiClient(
                client,
                sessionStorage,
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
                new ErstebankAuthenticator(apiClient, sessionStorage),
                credentials,
                strongAuthenticationState);
    }

    @Override
    protected Class<ErstebankConfiguration> getConfigurationClassDescription() {
        return ErstebankConfiguration.class;
    }

    @Override
    protected BerlinGroupTransactionFetcher getTransactionFetcher() {
        return new ErstebankTransactionFetcher(apiClient);
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        ErstebankPaymentExecutor executor = new ErstebankPaymentExecutor(apiClient);
        return Optional.of(new PaymentController(executor, executor));
    }
}
