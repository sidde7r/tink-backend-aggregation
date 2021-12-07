package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ConsentResponse {
    private String scaStatus;
    private String authorisationId;
    private String consentId;
    private String authorisationCode;

    @JsonProperty("_links")
    private LinksEntity links;
}
