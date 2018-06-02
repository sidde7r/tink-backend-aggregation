package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.entities;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.serializer.NordeaHashMapSerializer;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitialParametersRequestEntity {
    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String authLevel;
    private String RemeberUserId;

    public InitialParametersRequestEntity setAuthLevel(
            String authLevel) {
        this.authLevel = authLevel;
        return this;
    }

    public InitialParametersRequestEntity setRemeberUserId(
            String remeberUserId) {
        RemeberUserId = remeberUserId;
        return this;
    }
}
