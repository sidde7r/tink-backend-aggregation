package se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel.fetchers.creditcard.pfm.CreditMutuelPfmCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel.fetchers.creditcard.pfm.CreditMutuelPfmCreditCardTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.creditcard.nopfm.EuroInformationNoPfmCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.creditcard.nopfm.EuroInformationNoPfmCreditCardTransactionsFetcher;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class CreditMutuelAgent extends EuroInformationAgent
        implements RefreshCreditCardAccountsExecutor {
    private final CreditCardRefreshController creditCardRefreshController;

    public CreditMutuelAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(
                request,
                context,
                signatureKeyPair,
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
