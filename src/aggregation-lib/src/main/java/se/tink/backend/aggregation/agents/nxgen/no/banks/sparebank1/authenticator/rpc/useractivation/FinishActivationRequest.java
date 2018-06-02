package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.entities.useractivation.DeviceInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.entities.useractivation.PinSrpDataEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FinishActivationRequest {
    private String deviceDescription;
    private String deviceId;
    private String base64EncodedPublicKey;
    private DeviceInfoEntity deviceInfo;
    private String type;
    private PinSrpDataEntity pinSrpData;

    public String getDeviceDescription() {
        return deviceDescription;
    }

    public void setDeviceDescription(String deviceDescription) {
        this.deviceDescription = deviceDescription;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getBase64EncodedPublicKey() {
        return base64EncodedPublicKey;
    }

    public void setBase64EncodedPublicKey(String base64EncodedPublicKey) {
        this.base64EncodedPublicKey = base64EncodedPublicKey;
    }

    public DeviceInfoEntity getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfoEntity deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public PinSrpDataEntity getPinSrpData() {
        return pinSrpData;
    }

    public void setPinSrpData(PinSrpDataEntity pinSrpData) {
        this.pinSrpData = pinSrpData;
    }
}
