package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;

@RequiredArgsConstructor
public class TransactionPaginationHelperFactory {

    private final AgentsServiceConfiguration configuration;

    public TransactionPaginationHelper create(CredentialsRequest request) {
        if (configuration.isFeatureEnabled("transactionsRefreshScope")) {
            return new RefreshScopeTransactionPaginationHelper(request.getRefreshScope());
        }
        return new CertainDateTransactionPaginationHelper(request);
    }
}
