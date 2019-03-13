package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.transactionalaccount;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.OpenbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.OpenbankConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.transactionalaccount.rpc.AccountTransactionsRequestQueryParams;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.URL;

@JsonObject
public class OpenbankTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionKeyPaginator<TransactionalAccount, URL> {
    private final OpenbankApiClient apiClient;

    public OpenbankTransactionalAccountFetcher(OpenbankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient
                .getAccounts()
                .filter(AccountEntity::isTransactionalAccount)
                .map(AccountEntity::toTinkAccount)
                .toJavaList();
    }

    @Override
    public TransactionKeyPaginatorResponse<URL> getTransactionsFor(
            TransactionalAccount account, URL nextUrl) {
        AccountTransactionsRequestQueryParams queryParams =
                new AccountTransactionsRequestQueryParams.Builder()
                        .withProductCode(
                                account.getFromTemporaryStorage(
                                        OpenbankConstants.Storage.PRODUCT_CODE_OLD))
                        .withContractNumber(
                                account.getFromTemporaryStorage(
                                        OpenbankConstants.Storage.CONTRACT_NUMBER_OLD))
                        .build();

        if (nextUrl == null) {
            return apiClient.getTransactions(queryParams);
        }

        return apiClient.getTransactionsForNextUrl(nextUrl);
    }
}
