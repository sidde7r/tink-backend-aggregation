package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.entities;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entites.json.BaseMobileResponseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsResponseEntity extends BaseMobileResponseEntity {
    private List<TransactionEntity> movements;

    public List<TransactionEntity> getMovements() {
        return movements != null ? movements : Collections.emptyList();
    }
}
