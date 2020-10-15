package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities;

import lombok.Builder;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@Builder
@JsonObject
public class ClientInfoEntity {

    private String deviceModel;
    private String clientId;
}
