package se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.LclConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DeviceConfigurationRequest {
    @JsonProperty("model_version")
    private String modelVersion;
    private String appname;
    private String appversion;
    private String sdkversion;
    private String os;
    private String fuseau;
    @JsonProperty("connection_type")
    private String connectionType;
    private String manufacturer;
    private String lang;
    private String timezone;
    @JsonProperty("os_version")
    private String osVersion;
    private String model;
    private String name;

    private DeviceConfigurationRequest() {
        this.modelVersion = LclConstants.DeviceConfiguration.MODEL_VERSION;
        this.appname = LclConstants.DeviceConfiguration.APP_NAME;
        this.appversion = LclConstants.DeviceConfiguration.APP_VERSION;
        this.sdkversion = LclConstants.DeviceConfiguration.SDK_VERSION;
        this.os = LclConstants.DeviceConfiguration.OS;
        this.fuseau = LclConstants.DeviceConfiguration.FUSEAU;
        this.connectionType = LclConstants.DeviceConfiguration.CONNECTION_TYPE;
        this.manufacturer = LclConstants.DeviceConfiguration.MANUFACTURER;
        this.lang = LclConstants.DeviceConfiguration.LANG;
        this.timezone = LclConstants.DeviceConfiguration.TIMEZONE;
        this.osVersion = LclConstants.DeviceConfiguration.OS_VERSION;
        this.model = LclConstants.DeviceConfiguration.MODEL;
        this.name = LclConstants.DeviceConfiguration.NAME;
    }

    public static DeviceConfigurationRequest build() {
        return new DeviceConfigurationRequest();
    }
}
