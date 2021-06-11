package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.HasRefreshScope;
import se.tink.libraries.credentials.service.RefreshScope;

@RequiredArgsConstructor
public class TransactionPaginationHelperFactory {

    private final AgentsServiceConfiguration configuration;

    public TransactionPaginationHelper create(CredentialsRequest request) {
        if (configuration != null && configuration.isFeatureEnabled("transactionsRefreshScope")) {
            RefreshScope refreshScope = null;
            if (request instanceof HasRefreshScope) {
                refreshScope = ((HasRefreshScope) request).getRefreshScope();
            }
            return new RefreshScopeTransactionPaginationHelper(refreshScope);
        }
        return new CertainDateTransactionPaginationHelper(request);
    }
}
