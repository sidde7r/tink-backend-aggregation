package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.ing;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.TRANSFERS;

import com.google.inject.Inject;
import java.time.LocalDate;
import se.tink.agent.sdk.operation.Provider;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.agentcapabilities.PisCapability;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.IngBaseCardTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.IngBaseCreditCardsFetcher;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.http.client.LoggingStrategy;

@AgentCapabilities({CHECKING_ACCOUNTS, CREDIT_CARDS, TRANSFERS})
@AgentPisCapability(
        capabilities = {PisCapability.SEPA_CREDIT_TRANSFER, PisCapability.PIS_FUTURE_DATE})
public final class IngAgent extends IngBaseAgent implements RefreshCreditCardAccountsExecutor {

    private final CreditCardRefreshController creditCardRefreshController;
    private final Provider provider;

    @Inject
    public IngAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);

        setJsonHttpTrafficLogsEnabled(true);
        client.setLoggingStrategy(LoggingStrategy.EXPERIMENTAL);

        provider = agentComponentProvider.getProvider();
        creditCardRefreshController = constructCreditCardRefreshController();
    }

    @Override
    public LocalDate earliestTransactionHistoryDate() {
        // All transaction information since the payment account was opened
        return LocalDate.now().minusYears(7);
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    private CreditCardRefreshController constructCreditCardRefreshController() {

        IngBaseCreditCardsFetcher ingBaseCreditCardsFetcher =
                new IngBaseCreditCardsFetcher(apiClient, provider.getCurrency().toUpperCase());
        IngBaseCardTransactionsFetcher ingBaseCardTransactionsFetcher =
                new IngBaseCardTransactionsFetcher(apiClient, this::getTransactionsFromDate);

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                ingBaseCreditCardsFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(ingBaseCardTransactionsFetcher)));
    }
}
