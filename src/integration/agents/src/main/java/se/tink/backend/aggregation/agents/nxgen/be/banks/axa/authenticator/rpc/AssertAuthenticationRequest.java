package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities.AssertAuthenticationRequestData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities.HeaderEntity;

public class AssertAuthenticationRequest extends BaseRequest<AssertAuthenticationRequestData> {

    public AssertAuthenticationRequest(
            AssertAuthenticationRequestData data, List<HeaderEntity> headers) {
        super(data, headers);
    }
}
