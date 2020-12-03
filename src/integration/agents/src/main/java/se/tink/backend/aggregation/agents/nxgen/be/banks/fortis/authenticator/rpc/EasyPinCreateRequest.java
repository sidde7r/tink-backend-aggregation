package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc;

import lombok.Builder;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.DeviceInfoEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Builder
@JsonObject
public class EasyPinCreateRequest {
    private final DeviceInfoEntity deviceInfo;
    private final String mobileNumber;
}
