package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.rpc.ProxyRequestMessage;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProxyGetSignRequest extends ProxyRequestMessage<Void> {

    public ProxyGetSignRequest(String id) {
        super("/ucrmeans/sign/" + id);
    }
}
