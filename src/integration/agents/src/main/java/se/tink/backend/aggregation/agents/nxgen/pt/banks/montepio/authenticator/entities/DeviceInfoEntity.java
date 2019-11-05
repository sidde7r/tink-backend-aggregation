package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.MontepioConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DeviceInfoEntity {
    @JsonProperty("DeviceID")
    private String deviceID = UUID.randomUUID().toString().toUpperCase();

    @JsonProperty("DeviceModel")
    private String deviceModel = MontepioConstants.FieldValues.DEVICE_MODEL;
}
