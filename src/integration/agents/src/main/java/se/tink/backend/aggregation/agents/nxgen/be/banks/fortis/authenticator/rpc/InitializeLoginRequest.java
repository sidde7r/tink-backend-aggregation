package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Builder;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.DeviceInfoEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Builder
@JsonObject
@JsonInclude(value = Include.NON_NULL)
public class InitializeLoginRequest {
    private final String authenticationFactorId;
    private final String language;
    private final String smid;
    private final String minimumDacLevel;
    private final String distributorId;
    private final String requestedMeanId;

    private final String cardFrameId;
    private final DeviceInfoEntity deviceInfo;
}
