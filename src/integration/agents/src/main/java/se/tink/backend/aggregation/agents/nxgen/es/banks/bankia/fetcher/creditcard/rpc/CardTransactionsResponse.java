package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.creditcard.entities.CardTransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardTransactionsResponse {
    @JsonProperty("movimientosTarjeta")
    private List<CardTransactionEntity> transactions;

    @JsonProperty("indicadorMasMovimientos")
    private boolean indicateMoreTransactions;

    public List<CardTransactionEntity> getTransactions() {
        return transactions;
    }

    public boolean isIndicateMoreTransactions() {
        return indicateMoreTransactions;
    }
}
