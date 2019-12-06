package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.entities.PsuDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class StartAuthorisationRequest {

    @JsonProperty("psuData")
    private PsuDataEntity psuDataEntity;

    public StartAuthorisationRequest(PsuDataEntity psuDataEntity) {
        this.psuDataEntity = psuDataEntity;
    }

    public String toData() {
        return SerializationUtils.serializeToString(this);
    }
}
