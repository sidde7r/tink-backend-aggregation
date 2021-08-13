package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.entities.MovementEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

@JsonObject
public class CardTransactionsResponse implements PaginatorResponse {

    @JsonIgnore private CreditCardAccount account;

    @JsonProperty("movimiento")
    private List<MovementEntity> movement;

    @JsonProperty("masDatos")
    private boolean moreData;

    private int itemsTotales;

    @Override
    public List<CreditCardTransaction> getTinkTransactions() {
        return movement != null
                ? movement.stream()
                        .map(movementEntity -> movementEntity.toTinkTransaction(account))
                        .collect(Collectors.toList())
                : Collections.emptyList();
    }

    /**
     * If true, this simply indicates that the same request needs to be made again to get additional
     * data. The request object has a boolean that indicates if it's a new (first) request or a
     * continuation of a previous request.
     */
    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(moreData);
    }

    public CardTransactionsResponse setAccount(CreditCardAccount account) {
        this.account = account;
        return this;
    }
}
