package se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.authenticator.NorwegianAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.fetcher.creditcard.NorwegianCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.fetcher.creditcard.NorwegianDatePaginationController;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.fetcher.identity.NorwegianIdentityFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.fetcher.savingsaccount.NorwegianSavingsAccountFetcher;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AgentCapabilities({SAVINGS_ACCOUNTS, CREDIT_CARDS, IDENTITY_DATA})
public final class NorwegianAgent extends NextGenerationAgent
        implements RefreshCreditCardAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshIdentityDataExecutor {

    private final NorwegianApiClient apiClient;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final NorwegianIdentityFetcher identityFetcher;

    @Inject
    public NorwegianAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        apiClient = new NorwegianApiClient(client);
        creditCardRefreshController =
                constructCreditCardRefreshController(componentProvider.getLocalDateTimeSource());
        transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController(
                        componentProvider.getLocalDateTimeSource());
        identityFetcher = new NorwegianIdentityFetcher(apiClient, request);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new BankIdAuthenticationController<>(
                supplementalInformationController,
                new NorwegianAuthenticator(apiClient),
                persistentStorage,
                request);
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController(
            LocalDateTimeSource localDateTimeSource) {
        final NorwegianSavingsAccountFetcher accountFetcher =
                new NorwegianSavingsAccountFetcher(apiClient);

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
            LocalDateTimeSource localDateTimeSource) {
        final NorwegianCreditCardFetcher creditCardFetcher =
                new NorwegianCreditCardFetcher(apiClient);

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                creditCardFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new NorwegianDatePaginationController(
                                creditCardFetcher, creditCardFetcher, localDateTimeSource)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new NorwegianSessionHandler(apiClient);
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
        return identityFetcher.fetchIdentityData();
    }
}
