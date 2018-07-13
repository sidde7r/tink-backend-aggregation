package se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.deviceregistration;

import java.util.LinkedHashMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.banks.uk.barclays.BarclaysConstants;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.Request;

public class DeviceRegStep1Request implements Request {
    /*
    appVersion=2.24
    deviceModelName=iPhone9,3
    deviceOSVersion=10.1.1
    deviceID=a2fbe87b15193f44f4ed6448e632c64f4814f728b92affa2bd823d968b9a42ac
    deviceCompatibility=0
    deviceOSName=iOS
    applicationVersion=BMB_02240
    deviceIntegrityFlag=Y
    */
    private String deviceId;

    public String getCommandId() {
        return BarclaysConstants.COMMAND.DEVREG_STEP1;
    }

    public Map<String, String> getBody() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("appVersion", BarclaysConstants.APP_VERSION);
        m.put("deviceModelName", BarclaysConstants.DEVICE_MODEL_NAME);
        m.put("deviceOSVersion", BarclaysConstants.DEVICE_OS_VERSION);
        m.put("deviceID", deviceId);
        m.put("deviceCompatibility", "0");
        m.put("deviceOSName", BarclaysConstants.DEVICE_OS_NAME);
        m.put("applicationVersion", BarclaysConstants.APPLICATION_VERSION);
        m.put("deviceIntegrityFlag", "Y");
        return m;
    }
    public String getDeviceId() {
        return deviceId;
    }
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
