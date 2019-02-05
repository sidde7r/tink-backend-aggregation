package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.InitEnrollResponseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitEnrollResponse {
    private InitEnrollResponseEntity mobileResponse;

    public InitEnrollResponseEntity getMobileResponse() {
        return Preconditions.checkNotNull(mobileResponse);
    }
}
