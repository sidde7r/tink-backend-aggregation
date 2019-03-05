package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.transactional.entity;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SpecificAmountsItemEntity {
    private List<AmountsItemEntity> amounts;
    private String name;
    private String id;

    public List<AmountsItemEntity> getAmounts() {
        return amounts;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }
}
