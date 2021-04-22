package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.rpc.SessionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

@Slf4j
@JsonObject
public class TransactionResponse extends BaseResponse {
    @JsonProperty("transactionList")
    private List<TransactionEntity> transactions;

    @JsonProperty("more90Days")
    private boolean hasTransactionsOlderThan90Days;

    @JsonProperty("otp")
    private SessionEntity sessionEntity;

    private boolean haveMore;
    private String mobilePhone;

    public boolean canFetchTransactionsOlderThan90Days() {
        return hasTransactionsOlderThan90Days;
    }

    public SessionEntity getSessionEntity() {
        return sessionEntity;
    }

    @JsonIgnore
    public List<AggregationTransaction> getTransactions() {
        if (haveMore) {
            log.warn("Fetched {} transactions. More available.", transactions.size());
        }
        return Optional.ofNullable(transactions).orElseGet(Collections::emptyList).stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
