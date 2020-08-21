package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.identity;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class EndUserIdentityResponseDto {

    private String connectedPsu;
}
