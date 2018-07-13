package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.ResultEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.UserEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc.BbvaResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitiateSessionResponse extends BbvaResponse {

    private UserEntity user;


    public UserEntity getUser() {
        return user;
    }
}
