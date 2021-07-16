package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.MORTGAGE_AGGREGATION;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Optional;
import se.tink.backend.aggregation.agents.FetchEInvoicesResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshEInvoiceExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.einvoice.SwedbankDefaultEinvoiceFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.SwedbankDefaultInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.transactional.SwedbankSETransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.SwedbankSELoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankAbstractAgent;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.configuration.SwedbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.utilities.SwedbankDateUtils;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.profile.SwedbankPrivateProfileSelector;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice.EInvoiceRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.MultiIpGateway;

@AgentCapabilities({
    CHECKING_ACCOUNTS,
    LOANS,
    CREDIT_CARDS,
    SAVINGS_ACCOUNTS,
    IDENTITY_DATA,
    INVESTMENTS,
    MORTGAGE_AGGREGATION
})
public class SwedbankSEAgent extends SwedbankAbstractAgent
        implements RefreshLoanAccountsExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshEInvoiceExecutor {

    private final LoanRefreshController loanRefreshController;
    private final InvestmentRefreshController investmentRefreshController;
    private EInvoiceRefreshController eInvoiceRefreshController;

    @Inject
    public SwedbankSEAgent(
            AgentComponentProvider componentProvider,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(
                componentProvider,
                new SwedbankConfiguration(
                        SwedbankSEConstants.PROFILE_PARAMETERS.get(
                                componentProvider
                                        .getCredentialsRequest()
                                        .getProvider()
                                        .getPayload()),
                        SwedbankSEConstants.HOST,
                        true),
                new SwedbankSEApiClientProvider(new SwedbankPrivateProfileSelector()),
                new SwedbankDateUtils(ZoneId.of("Europe/Stockholm"), new Locale("sv", "SE")));

        this.loanRefreshController =
                new LoanRefreshController(
                        metricRefreshController,
                        updateController,
                        new SwedbankSELoanFetcher((SwedbankSEApiClient) apiClient));

        this.eInvoiceRefreshController = null;

        SwedbankDefaultInvestmentFetcher investmentFetcher =
                new SwedbankDefaultInvestmentFetcher(
                        (SwedbankSEApiClient) apiClient,
                        request.getProvider().getCurrency(),
                        componentProvider.getCredentialsRequest(),
                        componentProvider.getContext());

        investmentRefreshController =
                new InvestmentRefreshController(
                        metricRefreshController, updateController, investmentFetcher);

        final MultiIpGateway gateway =
                new MultiIpGateway(client, credentials.getUserId(), credentials.getId());
        gateway.setMultiIpGateway(agentsServiceConfiguration.getIntegrations());
    }

    @Override
    protected TransactionalAccountRefreshController
            constructTransactionalAccountRefreshController() {
        SwedbankSETransactionalAccountFetcher transactionalFetcher =
                new SwedbankSETransactionalAccountFetcher(
                        (SwedbankSEApiClient) apiClient, persistentStorage);

        TransactionFetcherController<TransactionalAccount> transactionFetcherController =
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionalFetcher),
                        transactionalFetcher);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                transactionalFetcher,
                transactionFetcherController);
    }

    @Override
    public FetchLoanAccountsResponse fetchLoanAccounts() {
        return loanRefreshController.fetchLoanAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchLoanTransactions() {
        return loanRefreshController.fetchLoanTransactions();
    }

    @Override
    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
        return investmentRefreshController.fetchInvestmentAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchInvestmentTransactions() {
        return investmentRefreshController.fetchInvestmentTransactions();
    }

    @Override
    public FetchEInvoicesResponse fetchEInvoices() {
        eInvoiceRefreshController =
                Optional.ofNullable(eInvoiceRefreshController)
                        .orElseGet(
                                () ->
                                        new EInvoiceRefreshController(
                                                metricRefreshController,
                                                new SwedbankDefaultEinvoiceFetcher(
                                                        (SwedbankSEApiClient) apiClient)));
        return new FetchEInvoicesResponse(eInvoiceRefreshController.refreshEInvoices());
    }
}
