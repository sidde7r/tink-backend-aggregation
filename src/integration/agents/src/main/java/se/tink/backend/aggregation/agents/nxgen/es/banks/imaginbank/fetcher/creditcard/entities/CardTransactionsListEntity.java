package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

@JsonObject
public class CardTransactionsListEntity {
    @JsonProperty("movimientos")
    private List<TransactionEntity> transactions;

    @JsonProperty("masDatos")
    private boolean moreData;

    @JsonIgnore
    public List<CreditCardTransaction> getTinkTransactions() {
        return Optional.ofNullable(transactions).orElse(Collections.emptyList()).stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public boolean canFetchMore() {
        return moreData;
    }
}
