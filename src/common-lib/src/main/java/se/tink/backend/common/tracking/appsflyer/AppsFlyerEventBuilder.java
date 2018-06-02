package se.tink.backend.common.tracking.appsflyer;

import com.google.common.collect.Maps;
import java.util.Map;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.application.ApplicationType;

public class AppsFlyerEventBuilder {

    private final String deviceType;
    private final String appsFlyerDeviceId;
    private final String ip;
    private String name;
    private Map<String, Object> attributes = Maps.newHashMap();

    private AppsFlyerEventBuilder(String deviceType, String appsFlyerDeviceId, String ip) {
        this.deviceType = deviceType;
        this.appsFlyerDeviceId = appsFlyerDeviceId;
        this.ip = ip;
    }

    public static AppsFlyerEventBuilder client(String deviceType, String appsFlyerDeviceId) {
        return new AppsFlyerEventBuilder(deviceType, appsFlyerDeviceId, null);
    }

    public static AppsFlyerEventBuilder client(String deviceType, String appsFlyerDeviceId, String ip) {
        return new AppsFlyerEventBuilder(deviceType, appsFlyerDeviceId, ip);
    }

    public AppsFlyerEventBuilder name(String name) {
        AppsFlyerEventBuilder builder = new AppsFlyerEventBuilder(deviceType, appsFlyerDeviceId, ip);
        builder.attributes = Maps.newHashMap(attributes);
        builder.name = name;
        return builder;
    }

    public AppsFlyerEventBuilder putAttribute(String key, Object value) {
        attributes.put(key, value);
        return this;
    }

    public AppsFlyerEventBuilder registered() {
        AppsFlyerEventBuilder builder = name("registered");
        builder.putAttribute(AppsFlyerEventAttributeKey.LEVEL, 1);
        return builder;
    }

    public AppsFlyerEventBuilder haveTransactions() {
        AppsFlyerEventBuilder builder = name("system-have-transactions");
        // Kept some space between this and `registered`, in case we want to add intermediate levels later.
        builder.putAttribute(AppsFlyerEventAttributeKey.LEVEL, 5);
        return builder;
    }
    
    public AppsFlyerEventBuilder signedApplication(ApplicationType type) {
        AppsFlyerEventBuilder builder = name("application-signed");
        builder.putAttribute(AppsFlyerEventAttributeKey.CONTENT_TYPE, type.toString());
        return builder;
    }

    public AppsFlyerEvent build() {
        if (attributes.isEmpty()) {
            return new AppsFlyerEvent(deviceType, appsFlyerDeviceId, ip, name);
        } else {
            return new AppsFlyerEvent(deviceType, appsFlyerDeviceId, ip, name,
                    SerializationUtils.serializeToString(attributes));
        }
    }
}
