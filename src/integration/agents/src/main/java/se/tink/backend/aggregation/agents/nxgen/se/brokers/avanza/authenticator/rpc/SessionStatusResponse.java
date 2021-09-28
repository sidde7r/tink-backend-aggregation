package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator.entities.UserEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class SessionStatusResponse {
    private String invalidSessionId;
    private UserEntity user;
}
