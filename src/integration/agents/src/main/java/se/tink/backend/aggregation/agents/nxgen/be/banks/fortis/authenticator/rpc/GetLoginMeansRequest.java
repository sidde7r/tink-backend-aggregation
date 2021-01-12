package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc;

import lombok.Builder;
import se.tink.backend.aggregation.annotations.JsonObject;

@Builder
@JsonObject
public class GetLoginMeansRequest {

    private final String minimumDacLevel;
    private final String distributorId;
    private final String smid;
}
