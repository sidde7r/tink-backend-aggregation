package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.PrepareEnrollResponseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PrepareEnrollResponse {
    private PrepareEnrollResponseEntity mobileResponse;

    public PrepareEnrollResponseEntity getMobileResponse() {
        Preconditions.checkNotNull(mobileResponse);
        mobileResponse.validateSession();
        return mobileResponse;
    }
}
