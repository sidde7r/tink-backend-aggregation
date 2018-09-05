package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MovementWrapperListEntity {
    private List<MovementEntity> movements;

    public List<MovementEntity> getMovements() {
        return movements;
    }
}
