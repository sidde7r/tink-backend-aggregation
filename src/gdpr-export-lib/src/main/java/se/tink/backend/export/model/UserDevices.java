package se.tink.backend.export.model;

import java.util.List;
import se.tink.backend.export.model.submodels.ExportDevice;

public class UserDevices {
    List<ExportDevice> devices;

    public UserDevices(List<ExportDevice> devices) {
        this.devices = devices;
    }

    public List<ExportDevice> getDevices() {
        return devices;
    }
}
