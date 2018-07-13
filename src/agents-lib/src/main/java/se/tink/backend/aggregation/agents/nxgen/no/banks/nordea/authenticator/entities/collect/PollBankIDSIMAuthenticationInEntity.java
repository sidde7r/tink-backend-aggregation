package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.entities.collect;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.serializer.NordeaHashMapSerializer;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PollBankIDSIMAuthenticationInEntity {
    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String id;

    public void setId(String id) {
        this.id = id;
    }
}
