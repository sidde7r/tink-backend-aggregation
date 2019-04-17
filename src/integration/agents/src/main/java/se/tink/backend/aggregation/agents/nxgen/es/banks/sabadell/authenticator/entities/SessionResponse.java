package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@SuppressWarnings("unused")
@JsonObject
public class SessionResponse {

    private UserEntity user;
    private boolean isInputTypeNumberSupported;

    public UserEntity getUser() {
        return user;
    }
}
