package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.LoginResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public abstract class BelfiusBaseTest {

    protected LoginResponse getLoginResponse(String loginResponseString) {
        return SerializationUtils.deserializeFromString(loginResponseString, LoginResponse.class);
    }
}
