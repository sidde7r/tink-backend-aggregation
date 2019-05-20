package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardLiquidationsRequest {
    @JsonProperty("refNumeroContrato")
    private String contractId;

    @JsonProperty("inicio")
    private boolean start;

    public CardLiquidationsRequest(String contractId, boolean start) {
        this.contractId = contractId;
        this.start = start;
    }
}
