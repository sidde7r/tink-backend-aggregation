package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.transactionalaccount;

import java.util.Collection;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.OpenbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.OpenbankConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.OpenbankConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.transactionalaccount.rpc.AccountTransactionsRequestQueryParams;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

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
        return apiClient.fetchAccounts().toTinkAccounts();
    }

    @Override
    public TransactionKeyPaginatorResponse<URL> getTransactionsFor(
            TransactionalAccount account, URL nextUrl) {
        try {
            if (nextUrl == null) {
                final AccountTransactionsRequestQueryParams queryParams =
                        new AccountTransactionsRequestQueryParams.Builder()
                                .withProductCode(
                                        account.getFromTemporaryStorage(
                                                OpenbankConstants.Storage.PRODUCT_CODE_OLD))
                                .withContractNumber(
                                        account.getFromTemporaryStorage(
                                                OpenbankConstants.Storage.CONTRACT_NUMBER_OLD))
                                .build();
                return apiClient.fetchTransactions(queryParams);
            } else {
                return apiClient.fetchTransactionsForNextUrl(nextUrl);
            }

        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_NOT_FOUND
                    && e.getResponse()
                            .getBody(ErrorResponse.class)
                            .hasErrorCode(ErrorCodes.NO_RECORDS_FOUND)) {
                return TransactionKeyPaginatorResponseImpl.createEmpty();
            }
            throw e;
        }
    }
}
