package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.entities.PendingTransaction;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount.rpc.PaymentDetails;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.rpc.PendingTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.backend.core.transfer.Transfer;

public class HandelsbankenSEUpcomingTransactionFetcher
        implements UpcomingTransactionFetcher<TransactionalAccount> {

    private final HandelsbankenSEApiClient client;
    private final HandelsbankenSessionStorage sessionStorage;

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
}
