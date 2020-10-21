package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.rpc;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.entities.PsuData;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
@AllArgsConstructor
public class StartAuthorisationRequest {

    private PsuData psuData;

    public String toData() {
        return SerializationUtils.serializeToString(this);
    }
}
