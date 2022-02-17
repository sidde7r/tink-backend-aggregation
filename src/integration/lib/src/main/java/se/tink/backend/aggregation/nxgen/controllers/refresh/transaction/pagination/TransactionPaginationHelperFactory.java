package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.HasRefreshScope;
import se.tink.libraries.credentials.service.RefreshScope;

@Slf4j
public class TransactionPaginationHelperFactory {

    public TransactionPaginationHelper create(CredentialsRequest request) {
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

        int requestAccountsSize =
                Objects.nonNull(request.getAccounts()) ? request.getAccounts().size() : 0;

        log.info(
                "Using CertainDateTransactionPaginationHelper (request.getAccounts().size() == {})",
                requestAccountsSize);
        return new CertainDateTransactionPaginationHelper(request);
    }

    private static boolean isTransactionHistoryProductEnabled(RefreshScope refreshScope) {
        return refreshScope != null
                && refreshScope.getTransactions() != null
                && refreshScope.getTransactions().getTransactionBookedDateGte() != null;
    }
}
