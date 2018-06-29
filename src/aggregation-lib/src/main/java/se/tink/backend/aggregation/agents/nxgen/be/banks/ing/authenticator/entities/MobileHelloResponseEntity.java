package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entites.json.BaseMobileResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entites.json.RequestEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MobileHelloResponseEntity extends BaseMobileResponseEntity {
    private SessionDataEntity sessionData;
    private List<RequestEntity> requests;

    public SessionDataEntity getSessionData() {
        return sessionData;
    }

    public List<RequestEntity> getRequests() {
        return requests == null ? Collections.emptyList() : requests;
    }
}
