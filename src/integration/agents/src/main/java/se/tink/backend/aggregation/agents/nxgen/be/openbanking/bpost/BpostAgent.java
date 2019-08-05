package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bpost;

import java.time.temporal.ChronoUnit;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bpost.fetcher.transactionalaccount.BpostTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class BpostAgent extends Xs2aDevelopersAgent {

    public BpostAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }

    @Override
    protected String getIntegrationName() {
        return BpostConstants.INTEGRATION_NAME;
    }

    @Override
    protected TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final BpostTransactionalAccountFetcher accountFetcher =
                new BpostTransactionalAccountFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(
                                accountFetcher, 4, 90, ChronoUnit.DAYS)));
    }
}
