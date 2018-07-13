package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.MobileHelloResponseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MobileHelloResponse {
    private MobileHelloResponseEntity mobileResponse;

    public MobileHelloResponseEntity getMobileResponse() {
        return Preconditions.checkNotNull(mobileResponse);
    }
}
