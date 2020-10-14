package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@Builder
@JsonObject
public class ExtraEntity {

    private int requiredLevelOfAssurance;
    private String clientId;
    private String identifyeeType;
    private List<String> scopes;
}
