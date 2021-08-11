package se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordea;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import java.util.Optional;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordea.authenticator.NordeaNoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordea.executor.payment.NordeaNoPaymentExecutorSelector;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordea.fetcher.creditcard.NordeaNoCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordea.fetcher.transactionalaccount.NordeaNoGetTransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordea.fetcher.transactionalaccount.NordeaNoTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.authenticator.NordeaBaseAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.creditcard.NordeaBaseCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.NordeaBaseTransactionalAccountFetcher;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS})
@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
public final class NordeaNoAgent extends NordeaBaseAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshCreditCardAccountsExecutor {

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;

    @Inject
    public NordeaNoAgent(AgentComponentProvider componentProvider, QsealcSigner qsealcSigner) {
        super(componentProvider);
        apiClient =
                new NordeaNoApiClient(componentProvider, client, persistentStorage, qsealcSigner);

        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        creditCardRefreshController = getCreditCardRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        NordeaBaseAuthenticator authenticator =
                new NordeaNoAuthenticator((NordeaNoApiClient) apiClient);
        OAuth2AuthenticationController oAuth2AuthenticationController =
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
                        oAuth2AuthenticationController, supplementalInformationHelper),
                oAuth2AuthenticationController);
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
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        NordeaNoPaymentExecutorSelector nordeaNoPaymentExecutorSelector =
                new NordeaNoPaymentExecutorSelector(apiClient, supplementalInformationController);

        return Optional.of(
                new PaymentController(
                        nordeaNoPaymentExecutorSelector, nordeaNoPaymentExecutorSelector));
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        NordeaBaseTransactionalAccountFetcher<NordeaNoGetTransactionResponse> accountFetcher =
                new NordeaNoTransactionalAccountFetcher<>(
                        apiClient, NordeaNoGetTransactionResponse.class, providerMarket);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(accountFetcher)));
    }

    private CreditCardRefreshController getCreditCardRefreshController() {
        NordeaBaseCreditCardFetcher creditFetcher =
                new NordeaNoCreditCardFetcher(apiClient, provider.getCurrency());

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                creditFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(creditFetcher)));
    }

    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
