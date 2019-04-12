package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ReferenceEntity {
    @JsonProperty("_type")
    private String type;

    private String value;

    public ReferenceEntity(String type, String value) {
        this.type = type;
        this.value = value;
    }
}
