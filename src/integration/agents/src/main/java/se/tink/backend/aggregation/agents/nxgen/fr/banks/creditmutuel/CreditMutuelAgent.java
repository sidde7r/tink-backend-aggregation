package se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel.fetchers.creditcard.pfm.CreditMutuelPfmCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel.fetchers.creditcard.pfm.CreditMutuelPfmCreditCardTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.creditcard.nopfm.EuroInformationNoPfmCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.creditcard.nopfm.EuroInformationNoPfmCreditCardTransactionsFetcher;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS, INVESTMENTS})
public final class CreditMutuelAgent extends EuroInformationAgent
        implements RefreshCreditCardAccountsExecutor {
    private final CreditCardRefreshController creditCardRefreshController;

    @Inject
    public CreditMutuelAgent(AgentComponentProvider componentProvider) {
        super(
                componentProvider,
                new CreditMutuelConfiguration(),
                new CreditMutuelApiClientFactory());

        creditCardRefreshController = constructCreditCardRefreshController();
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
        if (this.sessionStorage
                .get(EuroInformationConstants.Tags.PFM_ENABLED, Boolean.class)
                .orElse(false)) {
            return new CreditCardRefreshController(
                    metricRefreshController,
                    updateController,
                    CreditMutuelPfmCreditCardFetcher.create(this.apiClient),
                    CreditMutuelPfmCreditCardTransactionsFetcher.create(this.apiClient));
        }
        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                EuroInformationNoPfmCreditCardFetcher.create(this.apiClient, this.sessionStorage),
                EuroInformationNoPfmCreditCardTransactionsFetcher.create(this.apiClient));
    }
}
