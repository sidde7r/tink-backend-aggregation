package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.entities.LiquidationCardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.entities.LiquidationPeriodEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LiquidationDetailResponse {
    @JsonProperty("fechaVencimiento")
    private String expirationDate;

    @JsonProperty("liquidacionPeriodo")
    private LiquidationPeriodEntity liquidationPeriod;

    @JsonProperty("tarjetas")
    private List<LiquidationCardEntity> cards;

    public LiquidationPeriodEntity getLiquidationPeriod() {
        return liquidationPeriod;
    }
}
