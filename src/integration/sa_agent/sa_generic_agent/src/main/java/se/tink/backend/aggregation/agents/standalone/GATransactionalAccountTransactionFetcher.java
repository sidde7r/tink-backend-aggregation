package se.tink.backend.aggregation.agents.standalone;

import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.standalone.grpc.CheckingService;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class GATransactionalAccountTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {

    private static final String ENCODED_SPACE = "%20";

    private final CheckingService checkingService;

    public GATransactionalAccountTransactionFetcher(CheckingService checkingService) {
        this.checkingService = checkingService;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {

        if (StringUtils.isNotEmpty(key)) {
            key = key.replaceAll(StringUtils.SPACE, ENCODED_SPACE);
        }

        TransactionKeyPaginatorResponse resp =
                checkingService.fetchCheckingTransactions(account, key);
        return resp;
    }
}
