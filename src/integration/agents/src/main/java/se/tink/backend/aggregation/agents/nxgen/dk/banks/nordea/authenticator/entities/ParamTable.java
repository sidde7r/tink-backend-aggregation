package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.serializer.NordeaHashMapDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ParamTable {
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String key;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String val;

    public String getKey() {
        return key;
    }

    public String getVal() {
        return val;
    }
}
