package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LiquidationsListEntity {
    @JsonProperty("datoLiquidacion")
    private List<CardLiquidationDataEntity> liquidationData;

    @JsonProperty("masDatos")
    private boolean moreData;

    @JsonIgnore
    public Optional<CardLiquidationDataEntity> getNextFutureLiquidation() {
        // liquidationData with codSimulation = "S"
        return liquidationData.stream().filter(CardLiquidationDataEntity::isSimulation).findFirst();
    }
}
