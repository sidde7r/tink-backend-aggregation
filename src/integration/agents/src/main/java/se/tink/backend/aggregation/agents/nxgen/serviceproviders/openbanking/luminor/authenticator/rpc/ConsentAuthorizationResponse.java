package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.authenticator.entities.ScaMethod;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentAuthorizationResponse {

    @Getter private String authorizationId;
    private String scaStatus;

    @JsonProperty("scaMethods")
    private List<ScaMethod> scaMethods;

    @JsonProperty("_links")
    private LinksEntity links;
}
