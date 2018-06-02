package se.tink.backend.rpc;

import io.swagger.annotations.ApiModelProperty;
import se.tink.backend.core.Device;

import java.util.List;

public class DeviceListResponse {
    @ApiModelProperty(name = "devices", value="The list of devices", required = true)
    private List<Device> devices;

    public List<Device> getDevices() {
        return devices;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }
}
