package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.serializer.NordeaHashMapDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitialParametersResponseEntity {
    private InitialParameters initialParameters;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String sessionId;

    public String getSessionId() {
        return sessionId;
    }

    public InitialParameters getInitialParameters() {
        return initialParameters;
    }
}
