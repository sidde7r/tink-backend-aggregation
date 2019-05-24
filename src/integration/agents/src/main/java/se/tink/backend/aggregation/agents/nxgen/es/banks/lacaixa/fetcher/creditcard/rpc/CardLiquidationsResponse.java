package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.entities.CardLiquidationDataEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.entities.DateEntity;
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
    public Optional<String> getNextFutureLiquidationDate() {
        return liquidationsList
                .getNextFutureLiquidation()
                .map(CardLiquidationDataEntity::getEndDate)
                .map(DateEntity::getValue);
    }

    public String getRefValNumContract() {
        return refValNumContract;
    }
}
