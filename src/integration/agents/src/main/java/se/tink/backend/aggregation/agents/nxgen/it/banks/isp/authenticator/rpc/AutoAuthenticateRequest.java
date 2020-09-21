package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.rpc;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.entity.AutoAuthenticateRequestPayload;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
public class AutoAuthenticateRequest {

    private AutoAuthenticateRequestPayload payload;
}
