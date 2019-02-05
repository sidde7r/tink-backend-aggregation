package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingConstants;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

/**
 * Generic transaction paginator for ukob.
 *
 * @param <ResponseType> The transaction response entity
 * @param <AccountType>  The type of account to fetch transactions for. eg. TransactionalAccount, CreditCard, etc.
 */
public class UkOpenBankingTransactionPaginator<ResponseType, AccountType extends Account>
        implements TransactionKeyPaginator<AccountType, String> {
    private AggregationLogger logger = new AggregationLogger(UkOpenBankingTransactionPaginator.class);

    private static final int PAGINATION_LIMIT = 50; // Limits number of pages fetched in order to reduce loading.
    private static final int PAGINATION_GRACE_LIMIT = 5;

    private final UkOpenBankingApiClient apiClient;
    private final Class<ResponseType> responseType;
    private final TransactionConverter<ResponseType, AccountType> transactionConverter;

    private String lastAccount;
    private int paginationCount;

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

        updateAccountPaginationCount(account.getBankIdentifier());

        if (paginationCount > PAGINATION_LIMIT) {
            return TransactionKeyPaginatorResponseImpl.createEmpty();
        }

        if (key == null) {
            key = UkOpenBankingConstants.ApiServices.getInitialTransactionsPaginationKey(account.getBankIdentifier());
        }

        try {
            return transactionConverter
                    .toPaginatorResponse(apiClient.fetchAccountTransactions(key, responseType), account);
        } catch (HttpResponseException e) {

            // NatWest seems to have an bug where they will send us next links even though it goes out of range for how
            // many pages of transactions they actually can give us, causing an internal server error.
            // This code ignores http 500 error if we have already fetched several pages from the given account.
            if (paginationCount > PAGINATION_GRACE_LIMIT && e.getResponse().getStatus() == 500) {
                logger.warn("Ignoring http 500 (Internal server error) in pagination.", e);
                return TransactionKeyPaginatorResponseImpl.createEmpty();
            }

            throw e;
        }
    }

    private void updateAccountPaginationCount(String accountBankIdentifier) {

        if (!accountBankIdentifier.equalsIgnoreCase(lastAccount)) {
            paginationCount = 0;
        }

        lastAccount = accountBankIdentifier;
        paginationCount++;
    }
}
