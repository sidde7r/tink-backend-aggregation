package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.authenticator.WizinkAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account.WizinkAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account.WizinkTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard.WizinkCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard.WizinkCreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.identitydata.WizinkIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.session.WizinkSessionHandler;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@Slf4j
@AgentCapabilities({CREDIT_CARDS, SAVINGS_ACCOUNTS, IDENTITY_DATA})
public class WizinkAgent extends SubsequentProgressiveGenerationAgent
        implements RefreshCreditCardAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshIdentityDataExecutor {

    private final WizinkStorage wizinkStorage;
    private final WizinkApiClient apiClient;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    protected WizinkAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.wizinkStorage = new WizinkStorage(persistentStorage, sessionStorage);
        this.apiClient = new WizinkApiClient(client, wizinkStorage, supplementalInformationHelper);
        creditCardRefreshController = constructCreditCardRefreshController();
        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new WizinkSessionHandler(apiClient);
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        return new WizinkAuthenticator(apiClient, wizinkStorage);
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

    private CreditCardRefreshController constructCreditCardRefreshController() {
        WizinkCreditCardFetcher creditCardFetcher =
                new WizinkCreditCardFetcher(apiClient, wizinkStorage);
        WizinkCreditCardTransactionFetcher transactionFetcher =
                new WizinkCreditCardTransactionFetcher(apiClient);

        return new CreditCardRefreshController(
                metricRefreshController, updateController, creditCardFetcher, transactionFetcher);
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        AccountFetcher<TransactionalAccount> accountFetcher =
                new WizinkAccountFetcher(apiClient, wizinkStorage);
        TransactionFetcher<TransactionalAccount> transactionFetcher =
                new WizinkTransactionFetcher(apiClient);
        return new TransactionalAccountRefreshController(
                metricRefreshController, updateController, accountFetcher, transactionFetcher);
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        final IdentityDataFetcher fetcher = new WizinkIdentityDataFetcher(wizinkStorage);
        return new FetchIdentityDataResponse(fetcher.fetchIdentityData());
    }
}
