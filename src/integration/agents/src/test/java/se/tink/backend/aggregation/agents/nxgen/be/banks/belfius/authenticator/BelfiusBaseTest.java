package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator;

import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.LoginResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public abstract class BelfiusBaseTest {

    protected LoginResponse getLoginResponse(String loginResponseString) {
        return SerializationUtils.deserializeFromString(loginResponseString, LoginResponse.class);
    }
}
