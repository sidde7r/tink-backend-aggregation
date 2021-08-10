package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ConsentBaseLinksEntity implements AccessEntity {
    @JsonProperty("status")
    private ScaLinkEntity scaStatus;

    private ScaLinkEntity scaRedirect;
}
