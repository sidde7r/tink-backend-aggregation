package se.tink.backend.aggregation.agents.nxgen.se.business.handelsbanken.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class HandelsbankenSETransaction extends BaseResponse {
    @JsonProperty("data")
    private List<HandelsbankenSETransactionEntity> transactionsList;

    public List<Transaction> toTinkTransactions() {
        return transactionsList.stream()
                .map(HandelsbankenSETransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
