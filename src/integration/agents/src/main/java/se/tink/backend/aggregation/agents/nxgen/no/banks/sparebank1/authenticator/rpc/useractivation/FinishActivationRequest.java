package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.useractivation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Identity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.entities.useractivation.DeviceInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.entities.useractivation.PinSrpDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FinishActivationRequest {
    private String deviceDescription;
    private String deviceId;
    private String base64EncodedPublicKey;
    private DeviceInfoEntity deviceInfo;
    private String type;
    private PinSrpDataEntity pinSrpData;

    @JsonIgnore
    public static FinishActivationRequest create(Sparebank1Identity identity) {
        FinishActivationRequest request = new FinishActivationRequest();

        request.setDeviceDescription(Sparebank1Constants.DeviceValues.DESCRIPTION);
        request.setDeviceId(identity.getDeviceId());
        request.setBase64EncodedPublicKey(identity.getUserName());
        request.setDeviceInfo(DeviceInfoEntity.create());
        request.setType(Sparebank1Constants.DeviceValues.STRONG);
        request.setPinSrpData(PinSrpDataEntity.create(identity));

        return request;
    }

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
