package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount.rpc;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.joda.time.DateTimeComparator;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.creditcard.entities.CardInvoiceInfo;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.entities.HandelsbankenSEAccount;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount.entities.HandelsbankenSETransaction;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.rpc.PendingTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

public class TransactionsSEResponse extends TransactionsResponse<HandelsbankenSEApiClient> {

    private HandelsbankenSEAccount account;
    private List<HandelsbankenSETransaction> transactions;
    private CardInvoiceInfo cardInvoiceInfo;

    public HandelsbankenSEAccount getAccount() {
        return account;
    }

    public CardInvoiceInfo getCardInvoiceInfo() {
        return cardInvoiceInfo;
    }

    @Override
    public List<AggregationTransaction> toTinkTransactions(Account account, HandelsbankenSEApiClient client,
            HandelsbankenSessionStorage sessionStorage) {
        List<AggregationTransaction> transactions = this.transactions.stream()
                .map(HandelsbankenSETransaction::toTinkTransaction)
                .collect(Collectors.toList());

        transactions.addAll(sessionStorage.applicationEntryPoint().map(applicationEntryPoint ->
                {
                    PendingTransactionsResponse pendingTransactions = client.pendingTransactions(applicationEntryPoint);
                    if (pendingTransactions.hasTransactionsFor(account)) {
                        List<AggregationTransaction> todaysTransactions = transactions.stream()
                                .filter(TransactionsSEResponse::isToday)
                                .collect(Collectors.toList());
                        return pendingTransactions.toTinkTransactions(account, client)
                                .filter(pendingTinkTransaction ->
                                        !(isToday(pendingTinkTransaction) && todaysTransactions.stream()
                                                .anyMatch(todaysTransaction ->
                                                        isSameAmount(todaysTransaction, pendingTinkTransaction)
                                                )))
                                .collect(Collectors.toList());
                    }
                    return Collections.<Transaction>emptyList();
                }
                ).orElse(Collections.emptyList())
        );
        return transactions;
    }

    private static boolean isToday(AggregationTransaction transaction) {
        return DateTimeComparator.getDateOnlyInstance()
                .compare(new Date(), transaction.getDate()) == 0;
    }

    private static boolean isSameAmount(AggregationTransaction todaysTransaction, UpcomingTransaction pendingTinkTransaction) {
        double todaysTransactionAmount = todaysTransaction.getAmount().getValue();
        double pendingTinkTransactionAmount = pendingTinkTransaction.getAmount().getValue();
        return todaysTransactionAmount == pendingTinkTransactionAmount ||
                -1 * todaysTransactionAmount == pendingTinkTransactionAmount;
    }
}
