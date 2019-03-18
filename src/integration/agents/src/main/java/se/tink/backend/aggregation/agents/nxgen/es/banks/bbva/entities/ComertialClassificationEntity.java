package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ComertialClassificationEntity extends BasicEntity {
    private String name;

    public String getName() {
        return name;
    }
}
