package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.hybrid;

import java.time.OffsetDateTime;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.TransactionConverter;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.hybrid.TransactionOffsetDateTimeKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;

@RequiredArgsConstructor
public class UkObTransactionPaginator<RESPONSE, ACCOUNT extends Account>
        implements TransactionOffsetDateTimeKeyPaginator<ACCOUNT, String> {

    private final UkOpenBankingApiClient apiClient;
    private final Class<RESPONSE> responseType;
    private final TransactionConverter<RESPONSE, ACCOUNT> transactionConverter;

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            ACCOUNT account, OffsetDateTime from, OffsetDateTime to) {
        return transactionConverter.toPaginatorResponse(
                apiClient.fetchAccountTransactions(
                        account.getApiIdentifier(), from, to, responseType),
                account);
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            ACCOUNT account, @Nullable String key) {
        return transactionConverter.toPaginatorResponse(
                apiClient.fetchAccountTransactions(key, responseType), account);
    }
}
