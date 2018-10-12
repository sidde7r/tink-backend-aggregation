package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;

/**
 * Generic transaction paginator for ukob.
 *
 * @param <ResponseType> The transaction response entity
 * @param <AccountType>  The type of account to fetch transactions for. eg. TransactionalAccount, CreditCard, etc.
 */
public class UkOpenBankingTransactionPaginator<ResponseType, AccountType extends Account>
        implements TransactionKeyPaginator<AccountType, String> {

    private final UkOpenBankingApiClient apiClient;
    private final Class<ResponseType> responseType;
    private final TransactionConverter<ResponseType, AccountType> transactionConverter;

    /**
     * @param apiClient            Ukob api client
     * @param responseType         Class type of the account response entity
     * @param transactionConverter A method taking the TransactionEntity and a Tink account and converting it
     *                             to a key pagination response. See: {@link se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.TransactionConverter#toPaginatorResponse(Object, Account)}
     */
    public UkOpenBankingTransactionPaginator(
            UkOpenBankingApiClient apiClient,
            Class<ResponseType> responseType,
            TransactionConverter<ResponseType, AccountType> transactionConverter) {
        this.apiClient = apiClient;
        this.responseType = responseType;
        this.transactionConverter = transactionConverter;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(AccountType account, String key) {

        if (key == null) {
            key = UkOpenBankingConstants.ApiServices.getInitialTransactionsPaginationKey(account.getBankIdentifier());
        }

        return transactionConverter.toPaginatorResponse(apiClient.fetchAccountTransactions(key, responseType), account);
    }
}
