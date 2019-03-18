package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.BasicEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CategoryEntity extends BasicEntity {
    private String name;

    public String getName() {
        return name;
    }
}
