package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.CreateEnrollmentResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.rpc.ProxyResponseMessage;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProxyCreateEnrollmentsResponse
        extends ProxyResponseMessage<CreateEnrollmentResponseEntity> {}
