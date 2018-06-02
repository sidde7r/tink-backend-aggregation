package se.tink.backend.common.tracking.appsflyer;

public class AppsFlyerEvent {
    private final String deviceType;
    private final String appsFlyerDeviceId;
    private final String name;
    private final String value;
    private final String ip;

    public AppsFlyerEvent(String deviceType, String appsFlyerDeviceId, String ip, String name) {
        this(deviceType, appsFlyerDeviceId, ip, name, null);
    }

    public AppsFlyerEvent(String deviceType, String appsFlyerDeviceId, String ip, String name, String value) {
        this.deviceType = deviceType;
        this.appsFlyerDeviceId = appsFlyerDeviceId;
        this.name = name;
        this.value = value;
        this.ip = ip;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getAppsFlyerDeviceId() {
        return appsFlyerDeviceId;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getIp() {
        return ip;
    }
}
