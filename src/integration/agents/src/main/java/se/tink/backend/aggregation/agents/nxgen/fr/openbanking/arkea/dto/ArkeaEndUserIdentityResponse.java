package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.entity.ArkeaEndUserIdentityLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class ArkeaEndUserIdentityResponse {
    private String connectedPsu;

    @JsonProperty("_links")
    private ArkeaEndUserIdentityLinksEntity endUserIdentityLinksEntity;
}
