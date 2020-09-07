package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize;

import lombok.Builder;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@Builder
@JsonObject
public class ValidationUnitPasswordDto implements ValidationUnitRequestItemBaseDto {

    private String id;

    private String password;

    @Builder.Default String type = "PASSWORD";
}
