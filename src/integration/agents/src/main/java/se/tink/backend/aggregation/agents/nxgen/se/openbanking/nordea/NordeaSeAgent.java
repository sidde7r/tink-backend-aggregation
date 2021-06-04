package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.FetchTransferDestinationsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.NordeaSeDecoupledAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.NordeaSePaymentExecutorSelector;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.fetcher.creditcard.NordeaSeCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.fetcher.transactionalaccount.NordeaSeTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.creditcard.NordeaBaseCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.NordeaBaseTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.rpc.BaseGetTransactionResponse;
import se.tink.backend.aggregation.agents.utils.transfer.InferredTransferDestinations;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.account.enums.AccountIdentifierType;

@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS})
public final class NordeaSeAgent extends NordeaBaseAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshTransferDestinationExecutor,
                RefreshSavingsAccountsExecutor {

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;

    @Inject
    public NordeaSeAgent(AgentComponentProvider componentProvider, QsealcSigner qsealcSigner) {
        super(componentProvider);
        apiClient = new NordeaSeApiClient(client, persistentStorage, qsealcSigner);
        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        this.creditCardRefreshController = getCreditCardRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {

        BankIdAuthenticationController<String> bankIdAuthenticationController =
                new BankIdAuthenticationController<>(
                        supplementalInformationController,
                        new NordeaSeDecoupledAuthenticator((NordeaSeApiClient) apiClient),
                        persistentStorage,
                        request,
                        2000,
                        2000,
                        30);
        return new AutoAuthenticationController(
                request, context, bankIdAuthenticationController, bankIdAuthenticationController);
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
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        NordeaSePaymentExecutorSelector nordeaSePaymentExecutorSelector =
                new NordeaSePaymentExecutorSelector(apiClient, supplementalInformationController);

        return Optional.of(
                new PaymentController(
                        nordeaSePaymentExecutorSelector, nordeaSePaymentExecutorSelector));
    }

    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public FetchTransferDestinationsResponse fetchTransferDestinations(List<Account> accounts) {
        return InferredTransferDestinations.forPaymentAccounts(
                accounts,
                AccountIdentifierType.IBAN,
                AccountIdentifierType.SE,
                AccountIdentifierType.SE_BG,
                AccountIdentifierType.SE_PG);
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return transactionalAccountRefreshController.fetchSavingsAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return transactionalAccountRefreshController.fetchSavingsTransactions();
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        NordeaBaseTransactionalAccountFetcher<BaseGetTransactionResponse> accountFetcher =
                new NordeaSeTransactionalAccountFetcher<>(
                        apiClient, BaseGetTransactionResponse.class, providerMarket);

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
                new NordeaSeCreditCardFetcher(apiClient, provider.getCurrency());

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                creditFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(creditFetcher)));
    }
}
