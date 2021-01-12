package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Builder;
import se.tink.backend.aggregation.annotations.JsonObject;

@Builder
@JsonObject
@JsonInclude(Include.NON_NULL)
public class DeviceInfoEntity {
    private final String appIdentity;
    private final String appVersion;
    private final String deviceName;
    private final String deviceIdentity;
    private final String deviceBrand;
    private final String freeText;
    private final String deviceModel;
    private final String fingerPrint;
    private final String name;
    private final String tokenId;
}
