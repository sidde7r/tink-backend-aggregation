package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.CreateEnrollmentRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entities.ProxyRequestHeaders;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.rpc.ProxyRequestMessage;

public class ProxyCreateEnrollmentRequest
        extends ProxyRequestMessage<CreateEnrollmentRequestEntity> {

    public ProxyCreateEnrollmentRequest(CreateEnrollmentRequestEntity content) {
        super(
                "/security/means/secure-remote-password/enrollments",
                "POST",
                ProxyRequestHeaders.builder()
                        .secureRemotePasswordVersion("1")
                        .accept("application/json; charset=utf-8")
                        .build(),
                content,
                "application/json; charset=utf-8");
    }
}
