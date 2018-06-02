package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.rpc;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entites.json.BaseMobileResponseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BaseResponse {
    private BaseMobileResponseEntity mobileResponse;

    public BaseMobileResponseEntity getMobileResponse() {
        return Preconditions.checkNotNull(mobileResponse);
    }
}
