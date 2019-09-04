package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.entities.UserEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BaseUserResponse {
    private UserEntity user;

    public UserEntity getUser() {
        return user;
    }
}
