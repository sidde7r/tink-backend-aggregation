package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
class OrderDTO {
    CredentialsDTO credentials;

    @JsonProperty("reference")
    String orderReference;

    boolean isError;
    String state;
    String sessionToken;
    GoalDTO goal;
}
