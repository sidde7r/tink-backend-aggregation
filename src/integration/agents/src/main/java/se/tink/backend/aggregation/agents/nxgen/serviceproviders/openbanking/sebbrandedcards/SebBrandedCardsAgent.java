package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAbstractAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.SebCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.SebCardTransactionsFetcher;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionMonthPaginationController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SebBrandedCardsAgent extends SebAbstractAgent<SebBrandedCardsApiClient> {

    public SebBrandedCardsAgent(
            CredentialsRequest request,
            AgentContext context,
            SignatureKeyPair signatureKeyPair,
            String brandId) {
        super(request, context, signatureKeyPair);
        apiClient.setBrandId(brandId);
        creditCardRefreshController = getCreditCardRefreshController();
    }

    @Override
    protected SebBrandedCardsApiClient getApiClient() {
        if (this.apiClient == null) {
            this.apiClient = new SebBrandedCardsApiClient(client, sessionStorage);
        }
        return this.apiClient;
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    private CreditCardRefreshController getCreditCardRefreshController() {
        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                new SebCardAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionMonthPaginationController<>(
                                new SebCardTransactionsFetcher(apiClient),
                                SebCommonConstants.ZONE_ID)));
    }
}
