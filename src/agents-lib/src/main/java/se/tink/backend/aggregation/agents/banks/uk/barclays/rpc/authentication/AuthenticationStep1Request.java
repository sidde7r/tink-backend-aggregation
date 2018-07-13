package se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.authentication;

import java.security.KeyPair;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.codec.binary.Hex;
import se.tink.backend.aggregation.agents.banks.uk.barclays.BarclaysConstants;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.Request;

public class AuthenticationStep1Request implements Request {
    /*
        appVersion=2.24
        deviceModelName=iPhone9,3
        deviceOSVersion=10.1.1
        deviceID=8005a4272e6f66fd059740364119a4a68593e31084f3558e737ab83142921e2d
        aid=5016475122020344
        deviceCompatibility=0
        deviceOSName=iOS
        applicationVersion=BMB_02240
        deviceIntegrityFlag=Y
        qea=3059301306072a8648ce3d020106082a8648ce3d030107034200041137fb8eb80e85d0f5beb0aee1c13a366b546592e63404049bc538b1da95d6690b7e0a8c9b58be9025a36d50ceaeb22967c8d0a6452c51da73a744b192ec1816
     */
    private String deviceId;
    private String aId;
    private String qea;

    public String getCommandId() {
        return BarclaysConstants.COMMAND.AUTH_STEP1;
    }

    public Map<String, String> getBody() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("appVersion", BarclaysConstants.APP_VERSION);
        m.put("deviceModelName", BarclaysConstants.DEVICE_MODEL_NAME);
        m.put("deviceOSVersion", BarclaysConstants.DEVICE_OS_VERSION);
        m.put("deviceID", deviceId);
        m.put("aid", aId);
        m.put("deviceCompatibility", "0");
        m.put("deviceOSName", BarclaysConstants.DEVICE_OS_NAME);
        m.put("applicationVersion", BarclaysConstants.APPLICATION_VERSION);
        m.put("deviceIntegrityFlag", "Y\n");
        m.put("qea", qea);
        return m;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setaId(String aId) {
        this.aId = aId;
    }

    public void setQea(KeyPair qea) {
        byte[] pubKey = qea.getPublic().getEncoded();
        this.qea = Hex.encodeHexString(pubKey);
    }
}
