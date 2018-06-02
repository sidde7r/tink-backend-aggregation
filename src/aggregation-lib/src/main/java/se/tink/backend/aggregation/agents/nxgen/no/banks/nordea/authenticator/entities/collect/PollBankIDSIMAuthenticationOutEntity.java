package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.entities.collect;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.entities.AuthenticationTokenEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.serializer.NordeaHashMapDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PollBankIDSIMAuthenticationOutEntity {
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String progressStatus;

    private AuthenticationTokenEntity authenticationToken;

    public String getProgressStatus() {
        return progressStatus;
    }

    public AuthenticationTokenEntity getAuthenticationToken() {
        return authenticationToken;
    }
}
