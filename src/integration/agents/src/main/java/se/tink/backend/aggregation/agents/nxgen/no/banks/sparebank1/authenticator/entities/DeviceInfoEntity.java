package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.DeviceValues;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class DeviceInfoEntity {
    private String manufacturer;
    private String model;

    @JsonIgnore
    public static DeviceInfoEntity create() {
        DeviceInfoEntity deviceInfoEntity = new DeviceInfoEntity();
        deviceInfoEntity.setManufacturer(DeviceValues.MANUFACTURER);
        deviceInfoEntity.setModel(DeviceValues.MODEL);

        return deviceInfoEntity;
    }
}
