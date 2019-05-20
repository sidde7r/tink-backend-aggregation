package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.entities.CardLiquidationDataEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.entities.LiquidationsListEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardLiquidationsResponse {
    @JsonProperty("refValNumContrato")
    private String refValNumContract;

    @JsonProperty("ListaLiquidaciones")
    private LiquidationsListEntity liquidationsList;

    @JsonProperty("importeLimite")
    private double limitAmount;

    @JsonIgnore
    public CardLiquidationDataEntity getNextFutureLiquidation() {
        return liquidationsList.getNextFutureLiquidation();
    }

    public String getRefValNumContract() {
        return refValNumContract;
    }
}
