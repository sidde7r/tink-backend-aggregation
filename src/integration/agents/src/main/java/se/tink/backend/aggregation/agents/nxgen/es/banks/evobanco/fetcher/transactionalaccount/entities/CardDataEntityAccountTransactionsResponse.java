package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardDataEntityAccountTransactionsResponse {
    @JsonProperty("acuerdoTarjeta")
    private String agreementCard;
}
