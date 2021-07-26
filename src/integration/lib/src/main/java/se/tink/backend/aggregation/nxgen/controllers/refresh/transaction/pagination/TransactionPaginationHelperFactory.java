package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination;

import lombok.extern.slf4j.Slf4j;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.HasRefreshScope;
import se.tink.libraries.credentials.service.RefreshScope;
import se.tink.libraries.unleash.UnleashClient;
import se.tink.libraries.unleash.model.Toggle;

@Slf4j
public class TransactionPaginationHelperFactory {

    private final UnleashClient unleashClient;

    public TransactionPaginationHelperFactory(UnleashClient unleashClient) {
        this.unleashClient = unleashClient;
    }

    public TransactionPaginationHelper create(CredentialsRequest request) {
        if (isTransactionsRefreshScopeFeatureEnabled()) {
            log.debug(
                    "Unleash toggle \"TransactionsRefreshScope\" is enabled, {} implementation will be used",
                    RefreshScopeTransactionPaginationHelper.class);
            RefreshScope refreshScope = null;
            if (request instanceof HasRefreshScope) {
                refreshScope = ((HasRefreshScope) request).getRefreshScope();
            } else {
                log.debug(
                        "Request of type {} does not implement {}, pagination helper will always return that it needs another page",
                        request.getClass(),
                        HasRefreshScope.class);
            }

            if (isTransactionHistoryProductEnabled(refreshScope)) {
                log.info("Using RefreshScopeTransactionPaginationHelper");
                return new RefreshScopeTransactionPaginationHelper(refreshScope);
            }
        }

        log.info("Using CertainDateTransactionPaginationHelper");
        return new CertainDateTransactionPaginationHelper(request);
    }

    private boolean isTransactionsRefreshScopeFeatureEnabled() {
        try {
            Toggle transactionRefreshScopeFeatureToggle =
                    Toggle.of("TransactionsRefreshScope").build();
            return unleashClient.isToggleEnable(transactionRefreshScopeFeatureToggle);
        } catch (RuntimeException e) {
            log.warn("Couldn't get \"TransactionsRefreshScope\" feature flag", e);
            return false;
        }
    }

    private static boolean isTransactionHistoryProductEnabled(RefreshScope refreshScope) {
        return refreshScope != null
                && refreshScope.getTransactions() != null
                && refreshScope.getTransactions().getTransactionBookedDateGte() != null;
    }
}
