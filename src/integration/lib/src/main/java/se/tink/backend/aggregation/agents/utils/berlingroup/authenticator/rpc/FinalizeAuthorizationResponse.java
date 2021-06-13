package se.tink.backend.aggregation.agents.utils.berlingroup.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.utils.berlingroup.authenticator.entities.FinalizeAuthorizationLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class FinalizeAuthorizationResponse {

    private String scaStatus;

    @JsonProperty("_links")
    private FinalizeAuthorizationLinksEntity links;

    private String psuMessage;
}
