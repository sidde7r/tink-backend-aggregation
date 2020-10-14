package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper.IngMiscUtils;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.rpc.ProxyRequestMessage;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProxyGetAppAccessStatusRequest extends ProxyRequestMessage<Void> {

    public ProxyGetAppAccessStatusRequest() {
        super("/app-access/status", "GET", IngMiscUtils.constructInfoHeaders(), null, null);
    }
}
