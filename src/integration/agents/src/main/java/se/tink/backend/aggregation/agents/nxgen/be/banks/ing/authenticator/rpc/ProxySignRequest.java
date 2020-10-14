package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.SignRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.rpc.ProxyRequestMessage;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProxySignRequest extends ProxyRequestMessage<SignRequestEntity> {

    public ProxySignRequest(SignRequestEntity content) {
        super("/ucrmeans/sign", "POST", null, content, "application/json");
    }
}
