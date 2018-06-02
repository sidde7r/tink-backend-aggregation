package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entites.json;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ParamEntity {
    private String name;

    public String getName() {
        return name;
    }
}
