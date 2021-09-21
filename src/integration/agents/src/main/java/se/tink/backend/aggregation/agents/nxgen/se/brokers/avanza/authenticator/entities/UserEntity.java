package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class UserEntity {
    private boolean loggedIn;
}
