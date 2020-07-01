package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.identity.dto;

import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class EndUserIdentityResponseDto {

    private String connectedPsu;
}
