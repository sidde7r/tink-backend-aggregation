package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.rpc.ProxyRequestMessage;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProxyIndividualsRequest extends ProxyRequestMessage<Void> {

    public ProxyIndividualsRequest() {
        super("/v4/individuals/me");
    }
}
