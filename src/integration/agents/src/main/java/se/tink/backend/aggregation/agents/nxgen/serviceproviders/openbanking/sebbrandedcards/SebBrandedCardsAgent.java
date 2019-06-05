package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.LoopProofTransactionFetcherController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAbstractAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebCommonConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.SebCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.SebCardTransactionsFetcher;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionMonthPaginationController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SebBrandedCardsAgent extends SebAbstractAgent<SebBrandedCardsApiClient> {

    public SebBrandedCardsAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.apiClient = new SebBrandedCardsApiClient(client, sessionStorage);
    }

    @Override
    protected Optional<CreditCardRefreshController> constructCreditCardRefreshController() {
        return Optional.of(
                new CreditCardRefreshController(
                        metricRefreshController,
                        updateController,
                        new SebCardAccountFetcher(apiClient),
                        // TODO: restore TransactionFetcherController and remove
                        // LoopProofTransactionFetcherController
                        new LoopProofTransactionFetcherController<>(
                                transactionPaginationHelper,
                                new TransactionMonthPaginationController<>(
                                        new SebCardTransactionsFetcher(apiClient),
                                        SebCommonConstants.ZONE_ID))));
    }
}
