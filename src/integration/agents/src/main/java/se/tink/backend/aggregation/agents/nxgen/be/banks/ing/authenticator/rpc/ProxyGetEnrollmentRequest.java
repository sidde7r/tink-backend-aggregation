package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.rpc.ProxyRequestMessage;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProxyGetEnrollmentRequest extends ProxyRequestMessage<Void> {

    public ProxyGetEnrollmentRequest(String id) {
        super("/security/means/secure-remote-password/enrollments/" + id);
    }
}
