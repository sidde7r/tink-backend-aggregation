package se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.transactionalaccount.rpc;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.HandelsbankenFIApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.handelsbanken.fetcher.transactionalaccount.entities.HandelsbankenFITransaction;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class TransactionsFIResponse extends TransactionsResponse<HandelsbankenFIApiClient> {

    private List<HandelsbankenFITransaction> transactions;

    @Override
    public List<AggregationTransaction> toTinkTransactions(Account account, HandelsbankenFIApiClient client,
            HandelsbankenSessionStorage sessionStorage) {
        return transactions.stream()
                .map(HandelsbankenFITransaction::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
