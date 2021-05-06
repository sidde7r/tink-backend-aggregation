package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.device;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import lombok.Builder;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Builder
@JsonInclude(Include.NON_NULL)
public class ParametersEntity {
    private final Boolean isDeviceSlotAvailable;
    private final Boolean isIbRegistered;
    private final String magicWord;
    private final String tncAcceptedVersion;
    private final String tulipReference;
    private final String securityNumber;
    private final List<Integer> seedPositions;
    private final String appVersion;
    private final String bindPurpose;
    private final String password;
    private final String deviceId;
}
