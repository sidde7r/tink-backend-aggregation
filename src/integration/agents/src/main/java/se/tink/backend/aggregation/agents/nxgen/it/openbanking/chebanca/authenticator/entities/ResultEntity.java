package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ResultEntity {
    @JsonProperty("flushMessages")
    private Boolean flushMessages;

    @JsonProperty("messages")
    private List<String> messages;

    @JsonProperty("outcome")
    private String outcome;

    @JsonProperty("requestId")
    private String requestId;
}
