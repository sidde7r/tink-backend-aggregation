package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LiquidationDetailRequest {
    @JsonProperty("refValNumContrato")
    private String refValNumContract;

    @JsonProperty("fechaLiquidacion")
    private String liquidationDate;

    public LiquidationDetailRequest(String refValNumContract, String liquidationDate) {
        this.refValNumContract = refValNumContract;
        this.liquidationDate = liquidationDate;
    }
}
