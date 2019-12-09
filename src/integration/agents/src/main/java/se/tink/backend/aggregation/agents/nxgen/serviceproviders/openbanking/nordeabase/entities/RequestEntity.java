package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RequestEntity {
    @JsonProperty("_type")
    private String type;

    @JsonProperty("message_identifier")
    private String messageIdentifier;

    private String url;
}
