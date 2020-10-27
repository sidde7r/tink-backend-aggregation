package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class SessionEntity {
    private String loginContext;
    private String authLevel;
    private String authMethod;
    private String bankIdOrgId;
}
