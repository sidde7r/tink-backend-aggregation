package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.handelsbanken.fetcher.rpc;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.Market;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.rpc.TransactionResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class FiTransactionResponse extends TransactionResponse {

    private List<FiTransactionsItemEntity> transactions;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.stream()
                .filter(FiTransactionsItemEntity::hasDate)
                .map(te -> te.toTinkTransaction(Market.FINLAND))
                .collect(Collectors.toList());
    }
}
