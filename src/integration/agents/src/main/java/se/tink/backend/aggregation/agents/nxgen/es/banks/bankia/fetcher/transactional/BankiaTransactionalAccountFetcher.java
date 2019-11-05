package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.entities.PaginationDataEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class BankiaTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionKeyPaginator<TransactionalAccount, PaginationDataEntity> {

    private static final Logger log =
            LoggerFactory.getLogger(BankiaTransactionalAccountFetcher.class);
    private final BankiaApiClient apiClient;
    private final LocalDate nowInLocalDate;

    public BankiaTransactionalAccountFetcher(BankiaApiClient apiClient) {
        this.apiClient = apiClient;
        this.nowInLocalDate = LocalDate.now(BankiaConstants.ZONE_ID);
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.getAccounts().stream()
                .map(AccountEntity::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionKeyPaginatorResponse<PaginationDataEntity> getTransactionsFor(
            TransactionalAccount account, PaginationDataEntity key) {
        return apiClient.getTransactions(account, key);
    }
}
