package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants.Error;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.entities.ResponseStatusEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities.TransactionsBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities.UpcomingTransactionEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

/**
 * This class does the transaction fetching for both transactional accounts and credit card accounts
 * as it is carried out in the same way.
 */
public class IcaBankenTransactionFetcher {

    private final IcaBankenApiClient apiClient;

    public IcaBankenTransactionFetcher(IcaBankenApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public TransactionKeyPaginatorResponse<Date> fetchTransactions(Account account, Date key) {

        TransactionKeyPaginatorResponseImpl<Date> response =
                new TransactionKeyPaginatorResponseImpl<>();

        if (key == null) {
            TransactionsBodyEntity transactionsBody = apiClient.fetchTransactions(account);

            List<Transaction> transactions = parseTransactions(transactionsBody);
            transactions.addAll(parseTransactions(apiClient.fetchReservedTransactions(account)));

            response.setTransactions(transactions);
            response.setNext(transactionsBody.getNextKey());

            return response;
        }

        try {
            TransactionsBodyEntity transactionsBody =
                    apiClient.fetchTransactionsWithDate(account, key);

            response.setTransactions(parseTransactions(transactionsBody));
            response.setNext(transactionsBody.getNextKey());
        } catch (HttpResponseException hre) {
            if (hre.getResponse().getStatus() == HttpStatus.SC_FORBIDDEN) {
                ResponseStatusEntity error = hre.getResponse().getBody(ResponseStatusEntity.class);
                if (error.getCode() == Error.MULTIPLE_LOGIN_ERROR_CODE) {
                    throw BankServiceError.MULTIPLE_LOGIN.exception(hre);
                }
            }
        }

        return response;
    }

    private List<Transaction> parseTransactions(TransactionsBodyEntity transactionsBody) {
        return Optional.ofNullable(transactionsBody.getTransactions())
                .orElseGet(Collections::emptyList).stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    public Collection<UpcomingTransaction> fetchUpcomingTransactions(TransactionalAccount account) {
        List<UpcomingTransactionEntity> upcomingTransactions =
                apiClient.fetchUpcomingTransactions();

        return Optional.ofNullable(upcomingTransactions).orElseGet(Collections::emptyList).stream()
                .filter(
                        upcomingTransactionEntity ->
                                account.getBankIdentifier()
                                        .equalsIgnoreCase(
                                                upcomingTransactionEntity.getFromAccountId()))
                .map(UpcomingTransactionEntity::toUpcomingTransaction)
                .collect(Collectors.toList());
    }
}
