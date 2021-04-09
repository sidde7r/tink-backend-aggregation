package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.rpc.OtpResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.rpc.Result;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

@JsonObject
public class Movements {

    @JsonProperty("listaMovimiento")
    private List<CardTransactionEntity> cardTransactions;

    @JsonProperty("more90Days")
    private boolean hasTransactionsOlderThan90Days;

    private String mobilePhone;
    private OtpResponse otp;
    private Result result;

    public boolean canFetchTransactionsOlderThan90Days() {
        return hasTransactionsOlderThan90Days;
    }

    public OtpResponse getOtp() {
        return otp;
    }

    @JsonIgnore
    public List<AggregationTransaction> getTransactions() {
        return Optional.ofNullable(cardTransactions).orElseGet(Collections::emptyList).stream()
                .map(CardTransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
