package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.entities;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.serializer.NordeaHashMapSerializer;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NemidAuthenticateUserEntity {
    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String loginType;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String nemIdSessionId;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String nemIdToken;

    public NemidAuthenticateUserEntity setLoginType(String loginType) {
        this.loginType = loginType;
        return this;
    }

    public NemidAuthenticateUserEntity setNemIdSessionId(String nemIdSessionId) {
        this.nemIdSessionId = nemIdSessionId;
        return this;
    }

    public NemidAuthenticateUserEntity setNemIdToken(String nemIdToken) {
        this.nemIdToken = nemIdToken;
        return this;
    }
}
