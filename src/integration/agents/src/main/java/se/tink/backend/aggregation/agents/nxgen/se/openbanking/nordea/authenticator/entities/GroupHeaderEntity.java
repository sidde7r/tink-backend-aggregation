package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GroupHeaderEntity {

    @JsonProperty("creation_date_time")
    private String creationDateTime;

    @JsonProperty("http_code")
    private Long httpCode;

    @JsonProperty("message_identification")
    private String messageIdentification;
}
