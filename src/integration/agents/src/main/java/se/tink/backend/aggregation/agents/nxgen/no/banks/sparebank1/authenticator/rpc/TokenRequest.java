package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.DeviceValues;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Identity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.entities.DeviceInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.entities.PinSrpDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class TokenRequest {
    private String deviceDescription;
    private String deviceId;
    private String base64EncodedPublicKey;
    private DeviceInfoEntity deviceInfo;
    private String type;
    private PinSrpDataEntity pinSrpData;

    @JsonIgnore
    public static TokenRequest create(Sparebank1Identity identity) {
        TokenRequest request = new TokenRequest();

        request.setDeviceDescription(DeviceValues.DESCRIPTION);
        request.setDeviceId(identity.getDeviceId());
        request.setBase64EncodedPublicKey(identity.getUserName());
        request.setDeviceInfo(DeviceInfoEntity.create());
        request.setType(DeviceValues.STRONG);
        request.setPinSrpData(PinSrpDataEntity.create(identity));

        return request;
    }
}
