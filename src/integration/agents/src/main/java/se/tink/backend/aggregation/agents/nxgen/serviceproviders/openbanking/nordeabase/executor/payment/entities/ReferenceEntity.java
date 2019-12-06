package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.payment.rpc.Reference;

@JsonObject
public class ReferenceEntity {
    @JsonProperty("_type")
    private String type;

    private String value;

    public ReferenceEntity(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public Reference toTinkReference() {
        return new Reference(type, value);
    }
}
