package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.fetcher.transactionalaccount;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.EmptyFinalPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class IcaBankenTransactionFetcher implements TransactionDatePaginator<TransactionalAccount> {

    private static final int MAX_NUM_MONTHS_TO_FETCH = 18;
    private final IcaBankenApiClient apiClient;

    public IcaBankenTransactionFetcher(IcaBankenApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {

        final Date limitDate = getTransactionLimitDate();

        // Current bank limit of transaction history is 18 months
        if (fromDate.before(limitDate)) {
            return new EmptyFinalPaginatorResponse();
        } else if (toDate.before(limitDate)) {
            fromDate = limitDate;
        }

        try {
            return apiClient.fetchTransactionsForAccount(
                    account.getApiIdentifier(), fromDate, toDate);
        } catch (HttpResponseException e) {
            String exceptionMessage = e.getResponse().getBody(String.class);
            if (exceptionMessage.contains(
                    IcaBankenConstants.TransactionResponse.TRANSACTION_NOT_FOUND)) {
                return PaginatorResponseImpl.createEmpty(false);
            }
            throw e;
        }
    }

    private Date getTransactionLimitDate() {
        LocalDate limitDate = LocalDate.now().minusMonths(MAX_NUM_MONTHS_TO_FETCH);

        return Date.from(limitDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
