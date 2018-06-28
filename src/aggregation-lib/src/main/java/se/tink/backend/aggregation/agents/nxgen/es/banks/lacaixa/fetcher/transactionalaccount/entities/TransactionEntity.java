package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionEntity {

    @JsonProperty("id")
    private int id;


    @JsonProperty("importe")
    private BalanceEntity amount;
}
