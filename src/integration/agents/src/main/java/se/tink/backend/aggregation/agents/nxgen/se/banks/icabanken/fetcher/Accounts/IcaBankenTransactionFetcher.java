package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.ListUtils;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants.Error;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.entities.ResponseStatusEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities.TransactionsBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities.UpcomingTransactionEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

/**
 * This class does the transaction fetching for both transactional accounts and credit card accounts
 * as it is carried out in the same way.
 */
public class IcaBankenTransactionFetcher {

    private final IcaBankenApiClient apiClient;
    private final LocalDate oldestAllowedFromDate;

    public IcaBankenTransactionFetcher(IcaBankenApiClient apiClient) {
        this.apiClient = apiClient;
        this.oldestAllowedFromDate = LocalDate.now(Clock.system(ZoneId.of("CET"))).minusMonths(17);
    }

    public TransactionKeyPaginatorResponse<LocalDate> fetchTransactions(
            Account account, LocalDate toDate) {

        if (toDate == null) {
            return fetchInitialAndReservedTransactions(account);
        }

        if (toDate.isEqual(oldestAllowedFromDate)) {
            return TransactionKeyPaginatorResponseImpl.createEmpty();
        }

        LocalDate fromDate = getFromDate(toDate);

        try {
            TransactionsBodyEntity transactionsBody =
                    apiClient.fetchTransactionsWithDate(account, fromDate, toDate);
            return new TransactionKeyPaginatorResponseImpl<>(
                    transactionsBody.toTinkTransactions(),
                    fromDate.isEqual(oldestAllowedFromDate) ? null : transactionsBody.getNextKey());
        } catch (HttpResponseException hre) {
            handleKnownTransactionFetchingErrors(hre);
            throw hre;
        }
    }

    public Collection<UpcomingTransaction> fetchUpcomingTransactions(TransactionalAccount account) {
        List<UpcomingTransactionEntity> upcomingTransactions =
                apiClient.fetchUpcomingTransactions();

        return ListUtils.emptyIfNull(upcomingTransactions).stream()
                .filter(
                        upcomingTransactionEntity ->
                                account.getApiIdentifier()
                                        .equalsIgnoreCase(
                                                upcomingTransactionEntity.getFromAccountId()))
                .map(UpcomingTransactionEntity::toUpcomingTransaction)
                .collect(Collectors.toList());
    }

    private TransactionKeyPaginatorResponse<LocalDate> fetchInitialAndReservedTransactions(
            Account account) {
        TransactionsBodyEntity transactionsBody = apiClient.fetchTransactions(account);

        Collection<Transaction> transactions = transactionsBody.toTinkTransactions();
        transactions.addAll(apiClient.fetchReservedTransactions(account).toTinkTransactions());

        return new TransactionKeyPaginatorResponseImpl<>(
                transactions, transactionsBody.getNextKey());
    }

    /**
     * ICA returns the field "NoMoreTransactions" but it's always set to false within their max
     * transaction history (17 months). If the calculated fromDate is before the oldest allowed
     * fromDate oldestAllowedFromDate is returned.
     */
    private LocalDate getFromDate(LocalDate toDate) {
        LocalDate fromDate = toDate.minusMonths(1);

        if (fromDate.isBefore(oldestAllowedFromDate)) {
            return oldestAllowedFromDate;
        }

        return fromDate;
    }

    private void handleKnownTransactionFetchingErrors(HttpResponseException hre) {
        if (hre.getResponse().getStatus() == HttpStatus.SC_FORBIDDEN) {

            ResponseStatusEntity error = hre.getResponse().getBody(ResponseStatusEntity.class);
            if (error.getCode() == Error.MULTIPLE_LOGIN_ERROR_CODE) {
                throw BankServiceError.MULTIPLE_LOGIN.exception(hre);
            }
        }
    }
}
