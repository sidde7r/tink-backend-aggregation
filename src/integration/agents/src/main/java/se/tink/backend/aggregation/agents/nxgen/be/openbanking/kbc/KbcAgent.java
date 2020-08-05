package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc;

import com.google.inject.Inject;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.configuration.KbcConfiguration;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.executor.payment.KbcPaymentAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.executor.payment.KbcPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.fetcher.KbcTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.BerlinGroupPaymentAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.BerlinGroupAccountFetcher;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationFlow;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;

public final class KbcAgent extends BerlinGroupAgent<KbcApiClient, KbcConfiguration> {

    @Inject
    public KbcAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);

        this.apiClient = createApiClient();
        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    protected KbcApiClient createApiClient() {
        return new KbcApiClient(
                client,
                getConfiguration().getProviderSpecificConfiguration(),
                getConfiguration().getRedirectUrl(),
                credentials,
                persistentStorage);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return OAuth2AuthenticationFlow.create(
                request,
                systemUpdater,
                persistentStorage,
                supplementalInformationHelper,
                new KbcAuthenticator(apiClient),
                credentials,
                strongAuthenticationState);
    }

    @Override
    protected Class<KbcConfiguration> getConfigurationClassDescription() {
        return KbcConfiguration.class;
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        BerlinGroupPaymentAuthenticator paymentAuthenticator =
                new BerlinGroupPaymentAuthenticator(
                        supplementalInformationHelper, strongAuthenticationState);

        KbcPaymentExecutor kbcPaymentExecutor =
                new KbcPaymentExecutor(
                        apiClient,
                        paymentAuthenticator,
                        getConfiguration().getProviderSpecificConfiguration(),
                        sessionStorage);

        return Optional.of(
                new KbcPaymentAuthenticationController(
                        kbcPaymentExecutor, supplementalInformationHelper, sessionStorage));
    }

    @Override
    protected TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final BerlinGroupAccountFetcher accountFetcher = new BerlinGroupAccountFetcher(apiClient);
        final KbcTransactionFetcher transactionFetcher = new KbcTransactionFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(transactionFetcher)));
    }
}
