package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.rpc.SessionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

@JsonObject
public class CardTransactionsResponse extends BaseResponse {

    @JsonProperty("listaMovimiento")
    private List<CardTransactionEntity> cardTransactions;

    @JsonProperty("more90Days")
    private boolean hasTransactionsOlderThan90Days;

    @JsonProperty("otp")
    private SessionEntity sessionEntity;

    private String mobilePhone;
    private boolean haveMore;

    public boolean canFetchTransactionsOlderThan90Days() {
        return hasTransactionsOlderThan90Days;
    }

    public SessionEntity getSessionEntity() {
        return sessionEntity;
    }

    public boolean isHaveMore() {
        return haveMore;
    }

    @JsonIgnore
    public List<AggregationTransaction> getTransactions() {
        return Optional.ofNullable(cardTransactions).orElseGet(Collections::emptyList).stream()
                .map(CardTransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
