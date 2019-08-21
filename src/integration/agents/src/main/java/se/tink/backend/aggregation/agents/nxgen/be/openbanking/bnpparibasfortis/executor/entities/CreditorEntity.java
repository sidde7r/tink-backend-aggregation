package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditorEntity {
    private String name;

    @JsonCreator
    CreditorEntity(@JsonProperty("name") String name) {
        this.name = name;
    }
}
