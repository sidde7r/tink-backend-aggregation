package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

public class ActivateProfileRequest {
    private DeviceSignContainer pdeviceSignContainer;
    private String pubkey;

    public ActivateProfileRequest(String deviceInfo, String signature) {
        pdeviceSignContainer = new DeviceSignContainer(deviceInfo, signature);
    }

    public DeviceSignContainer getPdeviceSignContainer() {
        return pdeviceSignContainer;
    }

    public void setPdeviceSignContainer(DeviceSignContainer pdeviceSignContainer) {
        this.pdeviceSignContainer = pdeviceSignContainer;
    }

    public String getPubkey() {
        return pubkey;
    }

    public void setPubkey(String pubkey) {
        this.pubkey = pubkey;
    }

}
