package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

public class ValidateSignatureRequest {
    private DeviceSignContainer pdeviceSignContainer;

    public ValidateSignatureRequest(String deviceInfo, String signature) {
        pdeviceSignContainer = new DeviceSignContainer(deviceInfo, signature);
    }

    public DeviceSignContainer getPdeviceSignContainer() {
        return pdeviceSignContainer;
    }

    public void setPdeviceSignContainer(DeviceSignContainer pdeviceSignContainer) {
        this.pdeviceSignContainer = pdeviceSignContainer;
    }
}
