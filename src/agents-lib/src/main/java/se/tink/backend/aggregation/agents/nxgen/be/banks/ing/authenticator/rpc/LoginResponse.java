package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.LoginResponseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginResponse {
    private LoginResponseEntity mobileResponse;

    public LoginResponseEntity getMobileResponse() {
        return Preconditions.checkNotNull(mobileResponse);
    }
}
