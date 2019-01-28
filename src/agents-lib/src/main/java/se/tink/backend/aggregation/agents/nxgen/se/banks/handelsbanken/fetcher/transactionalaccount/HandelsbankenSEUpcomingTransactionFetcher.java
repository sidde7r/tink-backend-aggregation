package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount;

import com.google.common.base.Objects;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.entities.PendingTransaction;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount.entities.HandelsbankenSETransaction;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount.rpc.PaymentDetails;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount.rpc.TransactionsSEResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.rpc.PendingTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.entities.HandelsbankenAccount;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.transfer.rpc.Transfer;

public class HandelsbankenSEUpcomingTransactionFetcher
        implements UpcomingTransactionFetcher<TransactionalAccount> {

    private final HandelsbankenSEApiClient client;
    private final HandelsbankenSessionStorage sessionStorage;
    private Map<String, List<Amount>> todaysTransactionAmountsByAccount = new HashMap<>();

    public HandelsbankenSEUpcomingTransactionFetcher(
            HandelsbankenSEApiClient client,
            HandelsbankenSessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(
            TransactionalAccount account) {

        return sessionStorage.applicationEntryPoint().map(applicationEntryPoint ->
                {
                    PendingTransactionsResponse pendingTransactions = client
                            .pendingTransactions(applicationEntryPoint);

                    return pendingTransactions.getPendingTransactionStream(account)
                            .filter(pendingTransaction ->
                                    filterUpcomingPresentAsTransaction(account, pendingTransaction))
                            .map(transaction -> transaction
                                    .toTinkTransaction(getTransferDetails(transaction)))
                            .collect(Collectors.toList());
                }
        ).orElse(Collections.emptyList());
    }

    private Transfer getTransferDetails(PendingTransaction transaction) {

        return client.paymentDetails(transaction)
                .filter(PaymentDetails::isChangeAllowed)
                .map(PaymentDetails::toTransfer)
                .orElse(null);
    }

    /*
     * Re-implementing code fron old agent:
     * Handelsbanken sends upcoming (pending) transactions as both "Upcoming" and
     * "Account" transactions for a brief period of time after they have been payed.
     * This method filters any transactions with due date 'today' from "Upcoming"
     * transactions.
     * They are only compared by amount (and date of course) which can cause false
     * positives, but that is accepted as it is only for a short period. After that the problem
     * will resolve itself.
     */
    private boolean filterUpcomingPresentAsTransaction(TransactionalAccount account,
            PendingTransaction upcomingTransaction) {

        List<Amount> todaysAmounts = fetchTodaysTransactionAmounts(account);
        return todaysAmounts.stream()
                .noneMatch(amount -> Objects.equal(amount, upcomingTransaction.getTinkAmount()));
    }

    private List<Amount> fetchTodaysTransactionAmounts(TransactionalAccount account) {

        if (todaysTransactionAmountsByAccount.containsKey(account.getAccountNumber())) {
            return todaysTransactionAmountsByAccount.get(account.getAccountNumber());
        }

        List<Amount> amounts = findAccount(account)
                .map(client::transactions)
                .map(TransactionsSEResponse::getTodaysTransactions)
                .orElse(Stream.empty())
                .map(HandelsbankenSETransaction::positiveAmount)
                .collect(Collectors.toList());

        todaysTransactionAmountsByAccount.put(account.getAccountNumber(), amounts);

        return amounts;
    }

    private Optional<? extends HandelsbankenAccount> findAccount(TransactionalAccount account) {
        return sessionStorage.accountList()
                .flatMap(accountList -> accountList.find(account));
    }
}
