package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.HasRefreshScope;
import se.tink.libraries.credentials.service.RefreshScope;

@RequiredArgsConstructor
@Slf4j
public class TransactionPaginationHelperFactory {

    private final AgentsServiceConfiguration configuration;

    public TransactionPaginationHelper create(CredentialsRequest request) {
        boolean transactionsRefreshScopeFeatureEnabled =
                configuration != null && configuration.isFeatureEnabled("transactionsRefreshScope");
        log.info(
                "transactionsRefreshScope feature is enabled: {}, flags: {}",
                transactionsRefreshScopeFeatureEnabled,
                configuration == null ? "configuration is null" : configuration.getFeatureFlags());
        if (transactionsRefreshScopeFeatureEnabled) {
            RefreshScope refreshScope = null;
            if (request instanceof HasRefreshScope) {
                refreshScope = ((HasRefreshScope) request).getRefreshScope();
            } else {
                log.info(
                        "Request of type {} does not implement {}, pagination helper will always return that it needs another page",
                        request.getClass(),
                        HasRefreshScope.class);
            }
            return new RefreshScopeTransactionPaginationHelper(refreshScope);
        }
        return new CertainDateTransactionPaginationHelper(request);
    }
}
