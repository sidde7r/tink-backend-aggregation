package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.core.Amount;

@JsonObject
public class LiquidationDataEntity {

    @JsonProperty("saldoPrepago")
    private double prepaid;

    @JsonProperty("saldoDisponible")
    private double avaliableCredit;

    public Amount getPrepaidAmount(){
        return Amount.inEUR(prepaid);
    }


    public Amount getAvaliableCredit(){
        return Amount.inEUR(avaliableCredit);
    }
}
