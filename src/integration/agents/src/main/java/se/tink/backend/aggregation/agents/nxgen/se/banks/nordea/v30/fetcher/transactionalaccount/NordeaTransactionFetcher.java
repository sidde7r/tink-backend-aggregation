package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount;

import com.google.api.client.http.HttpStatusCodes;
import com.google.common.base.Predicates;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.einvoice.entities.PaymentEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class NordeaTransactionFetcher
        implements TransactionDatePaginator<TransactionalAccount>,
                UpcomingTransactionFetcher<TransactionalAccount> {
    private final NordeaSEApiClient apiClient;

    public NordeaTransactionFetcher(NordeaSEApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {
        return apiClient.fetchAccountTransactions(account, fromDate, toDate);
    }

    @Override
    public Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(
            TransactionalAccount account) {
        try {
            return apiClient.fetchPayments().getPayments().stream()
                    .filter(Predicates.or(PaymentEntity::isConfirmed, PaymentEntity::isInProgress))
                    .filter(
                            paymentEntity ->
                                    paymentEntity.getFrom().equals(account.getAccountNumber()))
                    .map(PaymentEntity::toUpcomingTransaction)
                    .collect(Collectors.toList());
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatusCodes.STATUS_CODE_SERVER_ERROR) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception();
            }
            throw e;
        }
    }
}
