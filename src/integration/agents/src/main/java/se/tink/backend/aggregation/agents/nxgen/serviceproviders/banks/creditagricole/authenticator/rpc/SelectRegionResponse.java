package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.rpc.DefaultResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SelectRegionResponse extends DefaultResponse {
    private boolean isResetPwdActive;

    public boolean isResetPwdActive() {
        return isResetPwdActive;
    }
}
