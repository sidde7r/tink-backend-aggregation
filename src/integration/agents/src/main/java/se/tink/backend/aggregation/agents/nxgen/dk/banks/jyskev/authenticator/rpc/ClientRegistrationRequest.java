package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskev.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ClientRegistrationRequest {
    @JsonProperty("software_id")
    private String softwareId = "relationsbank_ressource_owner";
}
