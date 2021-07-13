package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.authenticator.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ConsentBaseLinksEntity implements AccessEntity {
    @JsonProperty("status")
    private ScaLink scaStatus;

    private ScaLink scaRedirect;
}
