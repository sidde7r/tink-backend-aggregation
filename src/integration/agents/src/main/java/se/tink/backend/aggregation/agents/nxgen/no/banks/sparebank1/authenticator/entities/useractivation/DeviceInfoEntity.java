package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.entities.useractivation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DeviceInfoEntity {
    private String manufacturer;
    private String model;

    @JsonIgnore
    public static DeviceInfoEntity create() {
        DeviceInfoEntity deviceInfoEntity = new DeviceInfoEntity();
        deviceInfoEntity.setManufacturer(Sparebank1Constants.DeviceValues.MANUFACTURER);
        deviceInfoEntity.setModel(Sparebank1Constants.DeviceValues.MODEL);

        return deviceInfoEntity;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
