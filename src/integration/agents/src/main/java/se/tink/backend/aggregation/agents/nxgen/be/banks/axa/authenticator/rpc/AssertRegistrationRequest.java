package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities.HeaderEntity;

public class AssertRegistrationRequest extends BaseRequest<Object> {

    public AssertRegistrationRequest(Object data, List<HeaderEntity> headers) {
        super(data, headers);
    }
}
