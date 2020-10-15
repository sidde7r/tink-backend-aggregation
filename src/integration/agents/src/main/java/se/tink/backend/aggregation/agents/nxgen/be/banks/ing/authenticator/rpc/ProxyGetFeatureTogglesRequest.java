package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper.IngMiscUtils;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.rpc.ProxyRequestMessage;

public class ProxyGetFeatureTogglesRequest extends ProxyRequestMessage<Void> {

    public ProxyGetFeatureTogglesRequest() {
        super("/feature-toggles/status", "GET", IngMiscUtils.constructInfoHeaders(), null, null);
    }
}
