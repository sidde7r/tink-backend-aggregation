package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GenericMovementWrapperListEntity {
    private List<TransactionEntity> movements;

    public List<TransactionEntity> getMovements() {
        return movements;
    }
}
