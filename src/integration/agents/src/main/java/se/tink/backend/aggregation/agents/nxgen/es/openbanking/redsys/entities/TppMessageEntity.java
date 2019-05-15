package se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TppMessageEntity {
    @JsonProperty private String category;

    @JsonProperty private String code;

    @JsonProperty private String path;

    @JsonProperty private String text;
}
