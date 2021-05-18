package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.rpc;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.entities.ConsentDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
public class ConsentRequest {
    private ConsentDataEntity data;
}
