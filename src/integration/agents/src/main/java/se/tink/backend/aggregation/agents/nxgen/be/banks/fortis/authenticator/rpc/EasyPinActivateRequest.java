package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc;

import lombok.Builder;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.OathEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Builder
@JsonObject
public class EasyPinActivateRequest {
    private final String smid;
    private final OathEntity oath;
    private final String gsn;
    private final String otp;
    private final String distributorId;
}
