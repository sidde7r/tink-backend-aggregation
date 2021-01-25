package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import lombok.ToString;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@ToString
public class TypeEntity extends BasicEntity {
    private String id;
    private String name;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
