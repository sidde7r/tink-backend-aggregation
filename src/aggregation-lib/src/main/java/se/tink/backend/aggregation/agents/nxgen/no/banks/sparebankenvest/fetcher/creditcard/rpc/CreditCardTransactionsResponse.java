package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.creditcard.entities.CrecitCardTransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

@JsonObject
public class CreditCardTransactionsResponse {
    private int totalCount;
    private int start;
    private int step;
    private List<CrecitCardTransactionEntity> list;

    @JsonIgnore
    public List<CreditCardTransaction> getTinkTransactions() {
        return Optional.ofNullable(list).orElse(Collections.emptyList()).stream()
                .map(CrecitCardTransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public boolean hasMoreTransactions() {
        return totalCount > start + step;
    }

    @JsonIgnore
    public int getNextStartOffset() {
        return start + step;
    }
}
