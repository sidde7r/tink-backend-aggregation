package se.tink.backend.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.protostuff.Tag;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceOriginDto {
    @Tag(1)
    private boolean organic;
    @Tag(2)
    private String serviceName;
    @Tag(3)
    private String externalServiceId;
    @Tag(4)
    private String mediaSource;
    @Tag(5)
    private String campaign;
    @Tag(6)
    private String agency;
    @Tag(7)
    private long clickTime;
    @Tag(8)
    private long installTime;
    @Tag(9)
    private AppsFlyerDeviceOriginDto appsFlyer;
    @Tag(10)
    private FacebookDeviceOriginDto facebook;

    public boolean isOrganic() {
        return organic;
    }

    public void setOrganic(boolean organic) {
        this.organic = organic;
    }

    public String getExternalServiceId() {
        return externalServiceId;
    }

    public void setExternalServiceId(String externalServiceId) {
        this.externalServiceId = externalServiceId;
    }

    public String getMediaSource() {
        return mediaSource;
    }

    public void setMediaSource(String mediaSource) {
        this.mediaSource = mediaSource;
    }

    public String getCampaign() {
        return campaign;
    }

    public void setCampaign(String campaign) {
        this.campaign = campaign;
    }

    public String getAgency() {
        return agency;
    }

    public void setAgency(String agency) {
        this.agency = agency;
    }

    public long getClickTime() {
        return clickTime;
    }

    public void setClickTime(long clickTime) {
        this.clickTime = clickTime;
    }

    public long getInstallTime() {
        return installTime;
    }

    public void setInstallTime(long installTime) {
        this.installTime = installTime;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public AppsFlyerDeviceOriginDto getAppsFlyer() {
        return appsFlyer;
    }

    public void setAppsFlyer(AppsFlyerDeviceOriginDto appsFlyer) {
        this.appsFlyer = appsFlyer;
    }

    public FacebookDeviceOriginDto getFacebook() {
        return facebook;
    }

    public void setFacebook(FacebookDeviceOriginDto facebook) {
        this.facebook = facebook;
    }
}
