package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.entity.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.entity.account.BalanceBaseEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class DeutscheBankTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount> {

    private final DeutscheBankApiClient apiClient;

    public DeutscheBankTransactionalAccountFetcher(DeutscheBankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccounts().getAccounts().stream()
                .map(this::transformAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<TransactionalAccount> transformAccount(AccountEntity accountEntity) {
        List<BalanceBaseEntity> balances = apiClient.fetchBalances(accountEntity).getBalances();
        return accountEntity.toTinkAccount(balances);
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {

        try {
            return apiClient.fetchTransactionsForAccount(account, fromDate, toDate);
        } catch (HttpResponseException e) {
            String exceptionMessage = e.getResponse().getBody(String.class);
            if (exceptionMessage.contains(
                    DeutscheBankConstants.FormValues.TRANSACTION_REQUEST_REJECTED)) {
                return PaginatorResponseImpl.createEmpty(false);
            }
            throw e;
        }
    }
}
