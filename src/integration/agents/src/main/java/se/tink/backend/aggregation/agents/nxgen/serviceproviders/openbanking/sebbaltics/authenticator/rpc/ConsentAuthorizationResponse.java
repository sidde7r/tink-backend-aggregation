package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbaltics.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ConsentAuthorizationResponse {

    private String scaStatus;
    private String authorizationId;
    private ArrayList<String> scaMethods;

    @JsonProperty("_links")
    private LinksEntity links;
}
